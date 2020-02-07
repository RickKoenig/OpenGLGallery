package com.mperpetuo.openglgallery.engatest;

import android.util.Log;

import com.mperpetuo.openglgallery.enga.Main3D;
import com.mperpetuo.openglgallery.enga.Model;
import com.mperpetuo.openglgallery.enga.ModelUtil;
import com.mperpetuo.openglgallery.enga.NuMath;
import com.mperpetuo.openglgallery.enga.State;
import com.mperpetuo.openglgallery.enga.StateMan;
import com.mperpetuo.openglgallery.enga.Tree;
import com.mperpetuo.openglgallery.enga.Utils;
import com.mperpetuo.openglgallery.enga.ViewPort;
import com.mperpetuo.openglgallery.input.Input;
import com.mperpetuo.openglgallery.input.InputState;
import com.mperpetuo.openglgallery.input.SimpleUI;

import java.util.ArrayList;
import java.util.concurrent.RunnableFuture;

import static com.mperpetuo.openglgallery.enga.GLUtil.logrc;

public class Physics2d extends State {
    final String TAG = "Physics2D";

    Tree backgroundtree; // top level tree
    Tree roottree;
    Tree helpertree,mastertree;
    Tree bt;
    int nhelper;
    int nmaxhelper;
    //Tree atree; // a child tree with a model
    final float BX = 600;//20+(float)(Math.random()*100);//300;
    final float BY = 500;//20+(float)(Math.random()*100);//600;
    final float BASP = BX / BY;//Main3D.viewAsp;//1;
    float SX = Main3D.viewWidth;
    float SY = Main3D.viewHeight;
    float SASP = SX/SY;
    float mul = 0; // more if held down for awhile
    final float aback = .5f; // move everything back to see

    // global time
    float ptime = 0;
    float timestep = 1;
    float timemul = 3;

// global physics parameters

    //float[] littleg = new float[2]; // no gravity
    float[] littleg = {0,-.125f}; // some gravity
    //float norot = true; // make moi infinite
    float vdamp = .995f;//.95; // velocity damp
    float rvdamp = .995f; // rot velocity damp

    float elast = .65f;//1;//.7;//1;//.5; //1;//.95; //1;
    float ustatic = .3f;
    float udynamic = .25f;
    //float elastfric = true; // conservative friction
    boolean elastfric = false;
    boolean norot = false;

    // total energies
    float penergy = 0;
    float kenergy = 0;
    float renergy = 0;
    float tenergy = 0;

    ArrayList<Shape> shapes = new ArrayList<>();
    static final int maxshapes = 100;
    int minshapes;
    static final int NRECTPOINTS = 4;

    static float[] cn = new float[2];
    static float[] cp = new float[2];
    static float[] cp2 = new float[2];
    static float[] rveltrans = new float[2];
    static float[] rvelk = new float[2];
    static float[] rvelf = new float[2];
    static float[] ra = new float[2];
    static float[] rva = new float[2];
    static float[] rb = new float[2];
    static float[] rvb = new float[2];
    static float[] tva = new float[2];
    static float[] tvb = new float[2];
    static float[] tang = new float[2];
    static float[] tvat = new float[2];
    static float[] tvbt = new float[2];
    static float[] tda = new float[2];
    static float[] tdb = new float[2];
    static float[] del = new float[2];
    static float[] nrm = new float[2];
    static float[] sum = new float[2];
    static float[] bestnrm = new float[2];
    static float[] pd = new float[2];
    static float[][][] arr = new float[NRECTPOINTS][NRECTPOINTS][2];
    static int[][] warr = new int[NRECTPOINTS + NRECTPOINTS][2];
    static int[] wloc = new int[2];
    static int move[][] = {{0,1},{0,-1},{1,0},{-1,0}};
    static float[] paccum = new float[2];
    static float[] is = new float[2];
    static float[][] vs = new float[NRECTPOINTS][2];


    void initpoints() {
        helpertree = new Tree("helpertree");
        helpertree.trans = new float[] {0,0,aback};
        mastertree = ModelUtil.buildplanexy("mastertree",1,1,"ball1.png","tex");
        mastertree.name = "mastertree";
        mastertree.mod.flags |= Model.FLAG_NOZBUFFER; // turn off zbuffer
    }

    void setupcircle(Tree t,float x,float y,float rot,float rad) {
/*        t->rot.z = rot;
        t->trans.x = x/WY - .5f*WX/WY;
        t->trans.y = y/WY - .5f;
        t->scale.x = rad/WY*2;
        t->scale.y = rad/WY*2; */
        t.rot = new float[] {0, 0, rot};
        t.trans = new float[] {x/BY - .5f*BX/BY, y/BY - .5f, 0};
        t.scale = new float[] {rad/BY, rad/BY, 1};
    }

    void drawpoint(float[] pos,float rad) {
        //float tbr = 2*rad;
        Tree ht = new Tree(mastertree);
        //circle1->trans.x = -.5f;
        setupcircle(ht,pos[0],pos[1],(float)(Math.PI*(1.0f/16.0f)),rad);
        //		6);
        helpertree.linkchild(ht);
    }

    void resetpoints() {
        helpertree.glFree();
        helpertree = new Tree("helpertree");
        helpertree.trans = new float[] {0,0,aback}; // move this in line with background
        nhelper = 0;
    }

    void exitpoints() {
        helpertree.glFree();
        helpertree = null;
        mastertree.glFree();
        mastertree = null;
        nhelper = 0;
        nmaxhelper = 0;
    }

    enum Kind {Wall,Plank,Ball,num};

    // collision results
    class ci {
        float[] cn; // normal, direction to apply force
        float[] cp; // center of collision
        float penm; // amount of penetration
        ci() {
            cn = new float[2];
            cp = new float[2];
        }
    };
    ci collinfo = new ci();

    // handy math
    void vmul2sv(float[] out,float s,float[] vin) {
        if (out == null)
            Log.e(TAG,"null out array !!!");
        if (vin == null)
            Log.e(TAG,"null vin array !!!");
        out[0] = s*vin[0];
        out[1] = s*vin[1];
    }

    void vadd2vv(float[] out,float[] a,float[] b) {
        out[0] = a[0] + b[0];
        out[1] = a[1] + b[1];
    }

    void vsub2vv(float[] out,float[] a,float[] b) {
        out[0] = a[0] - b[0];
        out[1] = a[1] - b[1];
    }

    float sdot2vv(float[] a,float[] b) {
        return a[0]*b[0] + a[1]*b[1];
    }

    void vcross2zv(float[] res,float z,float[] b) {
        float bx = b[0];
        float by = b[1];
        res[0] = -z*by;
        res[1] = z*bx;
    }

    float scross2vv(float[] a,float[] b) {
        return a[0]*b[1] - a[1]*b[0];
    }

    void fixupcp(float[] cp,float[] nrm,float penm) {
        cp[0] += nrm[0]*penm;
        cp[1] += nrm[1]*penm;
    }

    // rotate an array of points
    void rotpoints2d(float p[][],float pr[][],float ang,int np) {
        int i;
        float fs=(float)Math.sin(ang);
        float fc=(float)Math.cos(ang);
        for (i=0;i<np;++i) {
            pr[i][0] = fc*p[i][0] - fs*p[i][1];
            pr[i][1] = fc*p[i][1] + fs*p[i][0];
        }
    }

    void nanerr(String mess,float val) {
        if (Float.isNaN(val))
            Log.e(TAG,"NAN ERROR " + mess + " " + val);
    }

    // in a,out b, returns length
    float normalize2d(float[] a,float[] b) {
        float r,ir;
        r = a[0]*a[0] + a[1]*a[1];
        r = (float)Math.sqrt(r);
        if (r < NuMath.EPSILON)
            return 0;
        if (Float.isNaN(r))
            return 0;
        ir = 1.0f/r;
        b[0] = ir*a[0];
        b[1] = ir*a[1];
        nanerr("normalize3d",r);
        return r;
    }


    // assume not same point, return 0 to almost 4
    float cheapatan2delta(float[] from,float[] to) {
        float dx = to[0] - from[0];
        float dy = to[1] - from[1];
        float ax = Math.abs(dx);
        float ay = Math.abs(dy);
        float ang = dy/(ax+ay);
        if (dx<0)
            ang = 2 - ang;
        else if (dy<0)
            ang = 4 + ang;
        return ang;
    }

    // intersection of 2 lines
    boolean getintersection2d(float[] la,float[] lb,float[] lc,float[] ld,float[] i0) {
        float e = lb[0] - la[0];
        float f = lc[0] - ld[0];
        float g = lc[0] - la[0];
        float h = lb[1] - la[1];
        float j = lc[1] - ld[1];
        float k = lc[1] - la[1];
        float det = e*j - f*h;
        if (det == 0)
            return false;
        det = 1/det;
        float t0 = (g*j - f*k)*det;
        float t1 = -(g*h - e*k)*det;
        if (t0>=0 && t0<=1 && t1>=0 && t1<=1) {
            if (i0 != null) {
                i0[0] = la[0] + (lb[0] - la[0])*t0;
                i0[1] = la[1] + (lb[1] - la[1])*t0;
            }
            return true;
        }
        return false;
    }

    boolean util_point2plank(float[] p,float[] pr[])
    {
        //return false;
        int i;
        int sgn = 0;
        for (i=0;i<NRECTPOINTS;++i) {
            vsub2vv(vs[i],pr[i],p);
        }
        for (i=0;i<NRECTPOINTS;++i) {
            float c = scross2vv(vs[i],vs[(i + 1)%NRECTPOINTS]);
            if (sgn == 0) {
                if (c >= 0) {
                    sgn = 1;
                } else {
                    sgn = -1;
                }
            } else {
                if (sgn == 1 && c < 0)
                    return false;
                if (sgn == -1 && c >= 0)
                    return false;
            }
        }
        return true;
    }

// end handy math

    abstract class Shape {
        Kind kind;
        float invmass;
        float invmoi;
        float[] pos = new float[2];
        float rot;
        float[] vel = new float[2];
        float rotvel;
        Tree t;
        Shape() {
            Log.i(TAG,"default shape cons\n");
            shapes.add(this); // keep track of this shape
        }
        abstract void draw();
/*        void free() {
            Log.i(TAG,"destroying shape '" + kind.toString() + "'");
            if (t != null) {
                t.glFree();
                t.unlinkchild();
            }
        } */
    }

    class Wall extends Shape {
        float[] norm;
        float d;
        Wall(float xa,float ya,float nxa, float nya) {
            kind = kind.Wall;
            //if (shapes.size() == maxshapes)
            //	return;
            float[] ps = {xa,ya};
            norm = new float[] {nxa,nya};
            //normalize2d(&norm,&norm);
            vel = new float[2];
            invmass = 0;
            invmoi = 0;
            norm = norm;
            d = sdot2vv(ps,norm);
        }

        @Override
        void draw() {

        }
    }
    class Ball extends Shape {
        float rad;
        Ball(float ma,float xa,float ya,float ra,float vxa,float vya,float vra,float rada) {
            super();
            //if (shapes.size() == maxshapes)
            //	return;
            if (ma != 0)
                invmass = 1/ma;
            else
                invmass = ma;
            if (invmass != 0) {
                invmoi = 2*invmass/(rada*rada);
            }
            if (norot)
                invmoi = 0;
            pos = new float[] {xa,ya};
            rot = ra;
            vel = new float[] {vxa,vya};
            rotvel = vra;
            rad = rada;
            kind = kind.Ball;
            //this.show = ballshow;
            //this.draw = balldraw;

            t = ModelUtil.buildplanexy("ball",1,1,"ball5.png","tex");
            t.name = "circle1";
            t.mod.flags |= Model.FLAG_NOZBUFFER; // turn off zbuffer
            setupball();
            roottree.linkchild(t);
        }

        void setupball()
        {
            t.rot = new float[] {0, 0, rot};
            t.trans = new float[] {pos[0]/BY - .5f*BX/BY, pos[1]/BY - .5f, 0};
            t.scale = new float[] {rad/BY, rad/BY, 1};
        }

        @Override
        void draw() {
            setupball();
        }
    }
    class Plank extends Shape {
        float w,h;
        //float[] p = new float[3];
        float[][] p = new float[4][2];
        float[][] pr = new float[4][2];
        Plank(float ma,
              float xa,float ya,float ra,
              float vxa,float vya,float vra,
              float wa,float ha) {
            super();
            //if (shapes.size() == maxshapes)
            //	return;
            if (ma != 0)
                invmass = 1/ma;
            else
                invmass = ma;
            if (invmass != 0) {
                invmoi = 12*invmass/(wa*wa + ha*ha);
            }
            if (norot)
                invmoi = 0;
            pos = new float[] {xa,ya};
            rot = ra;
            vel = new float[] {vxa,vya};
            rotvel = vra;
            w = wa;
            h = ha;
            kind = kind.Plank;
            //this.show = ballshow;
            //this.draw = balldraw;

            t = ModelUtil.buildplanexy("plank",1,1,"plank3.png","tex");
            t.name = "plank1";
            t.mod.flags |= Model.FLAG_NOZBUFFER; // turn off zbuffer
            setupplank();
            roottree.linkchild(t);

        }

        void setupplank() {
            t.rot = new float[] {0,0,rot};
            t.trans = new float[] {pos[0]/BY - .5f*BX/BY, pos[1]/BY - .5f, 0};
            t.scale = new float[] {w/BY/2,h/BY/2,1};
        }

        void calcpr()
        {
            p[0][0] = -.5f*w;
            p[0][1] =  .5f*h;
            p[1][0] =  .5f*w;
            p[1][1] =  .5f*h;
            p[2][0] =  .5f*w;
            p[2][1] = -.5f*h;
            p[3][0] = -.5f*w;
            p[3][1] = -.5f*h;
            rotpoints2d(p,pr,rot,NRECTPOINTS);
            int i;
            for (i=0;i<NRECTPOINTS;++i) {
                vadd2vv(pr[i],pr[i],pos);
            }
        }
        @Override
        void draw() {
            setupplank();
        }
    }

    void freeshapes() {
        shapes.clear();
    }
    
    void scaleOrient(Tree at) {
        SX = Main3D.viewWidth;
        SY = Main3D.viewHeight;
        SASP = SX/SY;
        if (SX > SY) { // landscape
            if (BASP > SASP) { // too wide to fit screen
                //Log.e(TAG,"case 1");
                float scl = SASP/BASP;
                at.scale = new float[]{scl, scl, scl};
            } else { // fits, no modifications
                //Log.e(TAG,"case 2");
                at.scale = new float[]{1,1,1};
            }
        } else { // portrait
            if (BASP > SASP) { // too wide to fit screen
                //Log.e(TAG,"case 3");
                float scl = 1.0f/BASP;
                at.scale = new float[]{scl, scl, scl};
            } else {
                //Log.e(TAG,"case 4");
                float scl = 1.0f/SASP;
                at.scale = new float[]{scl, scl, scl};
            }
        }
    }


    // test function arrays
    Runnable sumi = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG,"run sum runnable");
        }
    };

    Runnable prodi = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG,"run prod runnable");
        }
    };

    Runnable diffi = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG,"run diff runnable");
        }
    };

    class Consi implements Runnable {
        Consi() {
            Log.e(TAG,"cons cons runnable!!");
        }
        @Override
        public void run() {
            Log.e(TAG,"run cons runnable");
        }
    }
    Consi consi = new Consi();

    Runnable[][] collmatrixTest = {
            {sumi,consi,sumi},
            {prodi,diffi},
            {sumi,diffi,prodi}
    };

    private void testFunctionArrays() {
        //sum.run();
        //prod.run();
        for (int j=0;j<collmatrix.length;++j) {
            for (int i=0;i<collmatrix[j].length;++i)
                collmatrixTest[j][i].run();
        }
    }
    // end test function arrays

    abstract class CollFunc {
        abstract boolean collide(Shape a, Shape b);
    }

    CollFunc wall2wall = new CollFunc() {
        @Override
        boolean collide(Shape a, Shape b) {
            Wall wa = (Wall) a;
            Wall wb = (Wall) b;
            return false;
        }
    };
    CollFunc plank2wall = new CollFunc() {
        @Override
        boolean collide(Shape sp, Shape sw) {
            Plank p = (Plank) sp;
            Wall w = (Wall) sw;
            //logger("P2W\n");
            //return false;
            //logger_str += "(plank2wall )";
            //	boolean newcp = true;
            //	if (newcp) { // new way, better with deeper penetration
            int i;
            sum[0] = 0;sum[1]= 0;
            int npnt = 0;
            for (i=0;i<NRECTPOINTS;++i) { // take all the points that are penetrating and find the deepest one
			float[] vert = p.pr[i];
                //var vert = vadd2vv(sp.pr[i],sp.pos);
                float penm = w.d - sdot2vv(vert,w.norm);
                if (penm > 0) {
                    // vert COLLIDING
                    vadd2vv(sum,sum,vert);
                    ++npnt;
                }
            }
            if (npnt != 0) {
                // run through all the intersections
/*			var is = new Point2();
			for (i=0;i<NRECTPOINTS;++i) { // take all the points that are penetrating and find the deepest one
				var la0 = sp.pr[i];
				var la1 = sp.pr[(i + 1)%NRECTPOINTS];
				if (getintersection2dplane(la0,la1,sw.norm,sw.d,is)) {
					sum = vadd2vv(sum,is);
					++npnt;
				}
			}	*/ // seems better if commented out
                collinfo.cn = w.norm;
                float finpnt = 1.0f / npnt;
                sum[0] *= finpnt;
                sum[1] *= finpnt;
                float penm = w.d - sdot2vv(sum,w.norm);
                collinfo.penm = penm;
                collinfo.cp[0] = sum[0];collinfo.cp[1] = sum[1];//pointf2x(sum[0],sum[1]);
                return true;
            }
            return false;
//	} else { // old way
/*		var i;
		var sum = new Point2(0,0);
		var bestvert;
		var bestpenm = 0;
		for (i=0;i<NRECTPOINTS;++i) { // take all the points that are penetrating and find the deepest one
			var vert = sp.pr[i];
			//var vert = vadd2vv(sp.pr[i],sp.pos);
			var penm = sw.d - sdot2vv(vert,sw.norm);
			if (penm > 0) {
				// vert COLLIDING
				if (penm > bestpenm) {
					bestpenm = penm;
					bestvert = vert;
				}
			}
		}
		if (bestpenm > 0) {
			collinfo.cn = sw.norm;
			collinfo.penm = bestpenm;
			collinfo.cp = new Point2(bestvert[0],bestvert[1]);
			fixupcp(collinfo.cp,sw.norm,bestpenm);
			return true;
		}
		return false; */
//	}
        }
    };

    CollFunc plank2plank = new CollFunc() {
        @Override
        boolean collide(Shape sa, Shape sb) {
            Plank a = (Plank) sa;
            Plank b = (Plank) sb;
            //logger("P2P\n");
            boolean pass = true;
            if (!pass)
                return false;
            //logger_str += "(plank2plank )";
            int  i,j;
            // build 2d array of differences
            // TODO need an early out (AABB)
            for (j=0;j<NRECTPOINTS;++j) {
                for (i=0;i<NRECTPOINTS;++i) {
//			var new Point2()
//			pointf2x diff(a.pr[i][0]-b.pr[j][0],a.pr[i][1]-b.pr[j][1]);
                    //arr[j][i] = vsub2vv(a.pr[i],b.pr[j]);
                    vsub2vv(arr[j][i],a.pr[i],b.pr[j]);
                }
            }
            int wi = 0; // walk
            int wj = 0;
            float[] wp = arr[0][0];
            // find lowest y value, then lowest x value (incase of 2 or more lowest y values)
            for (j=0;j<NRECTPOINTS;++j) {
                for (i=0;i<NRECTPOINTS;++i) {
			        float[] lop = arr[j][i];
                    if (lop[1] < wp[1] || (lop[1] == wp[1] && lop[0] < wp[0])) { // there should be no points at the same place
                        wi = i;
                        wj = j;
                        wp = lop;
                    }
                }
            }

            wloc[0] = wi; wloc[1] = wj;//pointi2 wloc = pointi2x(wi,wj); //pointi2x wloc(wi,wj);
            int widx = 0;
            // boolean hilits[NRECTPOINTS+NRECTPOINTS]; // used just for drawing
            // ::fill(hilits,hilits+NRECTPOINTS+NRECTPOINTS,false);
            warr[widx++] = wloc;
            float wang = 0;
            // walk thru the points, doing gift wrapping
            while(widx < NRECTPOINTS+NRECTPOINTS) {
                // try the 4 'nearest' points (by connection, not distance)
                int k;
                int bestk=0;
                int nwi,nwj;
                float bestang = 5.0f; // bigger than any angle 0-4 from cheapatan2delta
                for (k=0;k<NRECTPOINTS;++k) { // use the one with the lowest angle
                    nwi = (wi + move[k][0] + NRECTPOINTS)%NRECTPOINTS;
                    nwj = (wj + move[k][1] + NRECTPOINTS)%NRECTPOINTS;
			        float[] pdest = arr[nwj][nwi];
                    //logger("wp = %f,%f pdest = %f,%f\n",wp[0],wp[1],pdest[0],pdest[1]);
                    float ang = cheapatan2delta(wp,pdest);
                    //logger("k = %d, ang = %f, wang = %f, bestang = %f\n",k,ang,wang,bestang);
                    if (ang <= bestang && ang >= wang) {
                        bestk = k;
                        bestang = ang;
                        //logger("new best k = %d, best ang = %f\n",bestk,bestang);
                    }
                }
                nwi = (wi + move[bestk][0] + NRECTPOINTS)%NRECTPOINTS;
                nwj = (wj + move[bestk][1] + NRECTPOINTS)%NRECTPOINTS;
                //logger("warr[%d] = %d,%d\n",widx,nwi,nwj);
                int[] warrdr = warr[widx++];
                warrdr[0] = nwi; warrdr[1] = nwj;
                //warr[widx++] = pointi2x(nwi,nwj);
                wi = nwi;
                wj = nwj;
                wp = arr[wj][wi];
                wang = bestang;
            }
            float bestpen = 1e20f;
            int bestidx = 0;
            boolean coll = false;
            //pointf2 bestnrm;

            // got 8 points, find if inside and if so find closest line with 2 points
            for (i=0;i<NRECTPOINTS+NRECTPOINTS;++i) {
                j = (i+1)%(NRECTPOINTS+NRECTPOINTS);
                float[] p0 = arr[warr[i][1]][warr[i][0]];
                float[] p1 = arr[warr[j][1]][warr[j][0]];
                vsub2vv(pd,p1,p0);
                nrm[0] = pd[1]; nrm[1] = -pd[0];//pointf2 nrm = pointf2x(pd[1],-pd[0]);
                normalize2d(nrm,nrm);
                float d = sdot2vv(nrm,p0);
                if (d <= 0) { // no collision
                    coll = false;
                    break;
                }
                float d1 = sdot2vv(p0,pd);
                float d2 = sdot2vv(p1,pd);
                if (d < bestpen && ((d1 >= 0 && d2<= 0) || (d1 <= 0 && d2 >= 0))) {
                    // left of line segment and a line from point intersects line segment at 90 degrees
                    bestpen = d;
                    bestidx = i;
                    bestnrm[0] = nrm[0]; bestnrm[1] = nrm[1];
                    coll = true;
                }
            }
            // find line segment and point
            if (coll) {
                i = bestidx;
                j = (i+1)%(NRECTPOINTS+NRECTPOINTS);
                bestnrm[0] = -bestnrm[0];
                bestnrm[1] = -bestnrm[1];
                collinfo.cn = bestnrm;
                float pen = bestpen;
                cp[0] = 0; cp[1] = 0;
                boolean newcp = true;
                if (newcp) {
                    // better for deeper penetrations
                    // pick a more central collision point
                    paccum[0] = 0; paccum[1] = 0; //pointf2 paccum = pointf2x();
                    int pcnt = 0;
                    //cp = point
                    // use all points inside and intersections
                    for (i=0;i<NRECTPOINTS;++i) {
                        if (util_point2plank(b.pr[i],a.pr)) {
                            paccum[0] += b.pr[i][0];
                            paccum[1] += b.pr[i][1];
                            ++pcnt;
                        }
                    }
                    for (i=0;i<NRECTPOINTS;++i) {
                        if (util_point2plank(a.pr[i],b.pr)) {
                            paccum[0] += a.pr[i][0];
                            paccum[1] += a.pr[i][1];
                            ++pcnt;
                        }
                    }
                    //pointf2 is = pointf2x();
                    for (i=0;i<NRECTPOINTS;++i) {
				float[] la0 = a.pr[i];
				float[] la1 = a.pr[(i + 1)%NRECTPOINTS];
                        for (j=0;j<NRECTPOINTS;++j) {
					float[] lb0 = b.pr[j];
					float[] lb1 = b.pr[(j + 1)%NRECTPOINTS];
                            if (getintersection2d(la0,la1,lb0,lb1,is)) {
                                paccum[0] += is[0];
                                paccum[1] += is[1];
                                ++pcnt;
                            }
                        }
                    }
                    if (pcnt == 0) {
                        //errorexit("pcnt == 0");
                        Log.e(TAG,"pcnt == 0 !!!!! ");
                    }
                    float fpcnt = 1.0f / pcnt;
                    cp[0] = paccum[0] * fpcnt;
                    cp[1] = paccum[1] * fpcnt;
                    collinfo.cp = cp;
                } else {
                    if (warr[i][0] == warr[j][0]) { // same point in a
                        float[] cp2r = a.pr[warr[i][0]];
                        cp2[0] = cp2r[0]; cp2[1] = cp2r[1];
                        cp[0] = cp2[0]; cp[1] = cp2[1];
                    } else if (warr[i][1] == warr[j][1]) { // same point in b
                        float[] cp2r = a.pr[warr[i][0]];
                        cp2 = b.pr[warr[i][1]];
                        cp[0] = cp2[0]; cp[1] = cp2[1];
                        cp[0] -= pen * bestnrm[0];
                        cp[1] -= pen * bestnrm[1];
                    } else { // what ??
                        //cp = pointf2x(3,3);
                        //errorexit("what");
                        Log.e(TAG,"WHAT ???");
                    }
                    collinfo.cp = cp;
                    fixupcp(collinfo.cp,bestnrm,pen);
                }
                collinfo.penm = pen;
            }
            return coll;
            //return false;
        }
    };

    CollFunc ball2wall = new CollFunc() {
        @Override
        boolean collide(Shape sb, Shape sw) {
            Ball b = (Ball) sb;
            Wall w = (Wall) sw;
            //logger("B2W\n");
            //return false;
            //logger_str += "(ball2wall )";
            float penm = w.d + b.rad - sdot2vv(b.pos,w.norm);
            if (penm <= 0)
                return false;
            // COLLIDING
            collinfo.cn = w.norm; // normal
            collinfo.penm = penm;
            vmul2sv(collinfo.cp,-b.rad,w.norm);
            vadd2vv(collinfo.cp,collinfo.cp,sb.pos);
            fixupcp(collinfo.cp,w.norm,penm);
            return true;
        }
    };

    CollFunc ball2plank = new CollFunc() {
        @Override
        boolean collide(Shape sb, Shape sp) {
            Ball b = (Ball) sb;
            Plank p = (Plank) sp;
            //return false;
            //logger("B2P\n");
            //return false;
            //logger_str += "(ball2plank )";
// TODO AABB early out
            int bestidx = 0;
            boolean coll = false;
            bestnrm[0] = 0;bestnrm[1] = 0;
            float bestpen = 1e20f;
            int i,j;
//	see if ball is close to edge
            for (i=0;i<NRECTPOINTS;++i) {
                j = (i+1)%(NRECTPOINTS);
                float[] p0 = p.pr[i];
                float[] p1 = p.pr[j];
                vsub2vv(pd,p1,p0);
                nrm[0] = pd[1];nrm[1] = -pd[0];//pointf2 nrm = pointf2x(pd[1],-pd[0]);
                normalize2d(nrm,nrm);
                float d = sdot2vv(nrm,p0); // line in d,nrm  format
                float pen = sdot2vv(nrm,b.pos) - d + b.rad;
                if (pen <= 0) {
                    coll = false;
                    break; // too far away, no collision
                }
                // now work 90 degrees from nrm
                float d1 = sdot2vv(p0,pd);
                float d2 = sdot2vv(p1,pd);
                float dp = sdot2vv(b.pos,pd);
//		if (pen < bestpen) {
                if (pen < bestpen && ((d1 >= dp && d2<= dp) || (d1 <= dp && d2 >= dp))) {
                    // left of line segment and a line from point intersects line segment at 90 degrees
                    bestpen = pen;
                    bestidx = i;
                    bestnrm[0] = nrm[0]; bestnrm[1] = nrm[1];
                    coll = true;
                }
            }
            //if (coll)
            //	if (bestpen > 20)
            //		var q = 3.1;
            if (!coll && i == NRECTPOINTS) { // check corners
                float bestdist2 = 1e20f;
                for (i=0;i<NRECTPOINTS;++i) {
                    vsub2vv(del,p.pr[i],b.pos);
                    float dist2 = del[0]*del[0] + del[1]*del[1];
                    if (dist2 >= b.rad*b.rad)
                        continue;
                    if (dist2 < bestdist2) {
                        bestdist2 = dist2;
                        bestidx = i;
                        coll = true;
                    }
                }
                if (coll) {
                    vsub2vv(bestnrm,p.pr[bestidx],b.pos);
                    normalize2d(bestnrm,bestnrm); // this might be wrong, could be 0
                    bestpen = b.rad - (float)Math.sqrt(bestdist2);
                    //if (bestpen > 20)
                    //	var q = 3.1;
                }
            }
            if (coll) {
                collinfo.penm = bestpen;
                vmul2sv(collinfo.cp,b.rad,bestnrm);
                vadd2vv(collinfo.cp,collinfo.cp,b.pos);
                bestnrm[0] = -bestnrm[0];
                bestnrm[1] = -bestnrm[1];
                collinfo.cn = bestnrm;
                fixupcp(collinfo.cp,bestnrm,bestpen);
            }
            return coll;
//	return false;
        }
    };

    CollFunc ball2ball = new CollFunc() {
        @Override
        boolean collide(Shape sa, Shape sb) {
            Ball a = (Ball) sa;
            Ball b = (Ball) sb;
            //logger_str += "(ball2ball )";
            //return false;
            vsub2vv(del,a.pos,b.pos);
            float dist2 = del[0]*del[0] + del[1]*del[1];
            float rsum = a.rad + b.rad;
            if (dist2 >= rsum*rsum)
                return false;
            float d = (float)(Math.sqrt(dist2));
            //var nrm = del;
            //normalize2d(nrm);
            vmul2sv(nrm,1/d,del);
            vmul2sv(cp,-a.rad,nrm);
            vadd2vv(cp,cp,a.pos);
            float penm = rsum - (float)(Math.sqrt(dist2));
            collinfo.penm = penm;
            collinfo.cn = nrm;
            collinfo.cp[0] = cp[0];collinfo.cp[1] = cp[1];
            fixupcp(collinfo.cp,nrm,penm);
            return true;
        }
    };

    CollFunc[][] collmatrix = {
        {wall2wall},
        {plank2wall,plank2plank},
        {ball2wall,ball2plank,ball2ball}
    };


    void collide(Shape sa,Shape sb) {
        //return;

        float tim = sa.invmass + sb.invmass;
        if (tim <= 0)
            return;
        // switch objects if necessary
        Kind satype = sa.kind;
        Kind sbtype = sb.kind;
        if (satype.ordinal() < sbtype.ordinal()) {
            Shape t = sa;
            sa = sb;
            sb = t;
            Kind tk = satype;
            satype = sbtype;
            sbtype = tk;
        }

        // do the collision
/*
        boolean res = collmatrix[satype][sbtype](sa,sb);
        if (!res) // no collision
            return;
*/
/*
        for (int j=0;j<collmatrix.length;++j) {
            for (int i=0;i<collmatrix[j].length;++i)
                collmatrix[j][i].collide(sa,sb);
        }
*/
        int i = satype.ordinal();
        int j = sbtype.ordinal();
        boolean res = collmatrix[i][j].collide(sa,sb);
        if (!res)
            return;


        cn = collinfo.cn; // normal of impulse from b to a
        cp = collinfo.cp; // where the collision took place
        float penm = collinfo.penm; // how deep the collision was
        // display collision info
        vmul2sv(cp2,.5f*penm,cn);
        vadd2vv(cp2,cp2,cp);
        //if (laststep) {
        drawpoint(cp,6);
        drawpoint(cp2,3);
        //}

        // velocity update very long
        // calc rel vel
        vsub2vv(rveltrans,sa.vel,sb.vel); // rel vel, trans part, a rel to b
        System.arraycopy(rveltrans,0,rvelk,0,2);
        //pointf2 ra,rva,rb,rvb;
        float racn=0,rbcn=0;
        if (sa.invmoi != 0) {
            vsub2vv(ra,cp,sa.pos);
            vcross2zv(rva,sa.rotvel,ra);
            vadd2vv(rvelk,rvelk,rva);
        }
        if (sb.invmoi != 0) {
            vsub2vv(rb,cp,sb.pos);
            vcross2zv(rvb,sb.rotvel,rb);
            vsub2vv(rvelk,rvelk,rvb);
        }

        // calc k, the impulse
        float rvelm = -sdot2vv(rvelk,cn); // vel rel to -normal, should be positive
        if (rvelm <= 0)  // pen velocity
            return; // already moving away
        //  impulse formula
        float timm = tim;
        if (sa.invmoi != 0) {
            racn = scross2vv(ra,cn);
            timm += racn*racn*sa.invmoi;
        }
        if (sb.invmoi != 0) {
            rbcn = scross2vv(rb,cn);
            timm += rbcn*rbcn*sb.invmoi;
        }
        float k = (1+elast)*rvelm/timm;

        // apply impulse maybe do later
        if (sa.invmass != 0) {
            float dva = k*sa.invmass;
            vmul2sv(tva,dva,cn);
            vadd2vv(sa.vel,sa.vel,tva);
        }
        if (sa.invmoi != 0) {
            sa.rotvel += k*racn*sa.invmoi;
        }
        if (sb.invmass != 0) {
            float dvb = k*sb.invmass;
            vmul2sv(tvb,dvb,cn);
            vsub2vv(sb.vel,sb.vel,tvb);
        }
        if (sb.invmoi != 0) {
            sb.rotvel -= k*rbcn*sb.invmoi;
        }
        // new friction
        float f = 0;
        float racnt = 0,rbcnt = 0;
        if (ustatic > 0) {
            // calc a new rvel
            System.arraycopy(rveltrans,0,rvelf,0,2);
            //pointf2 rvelf = rveltrans;
            if (sa.invmoi != 0) {
                vsub2vv(ra,cp,sa.pos);
                vcross2zv(rva,sa.rotvel,ra);
                vadd2vv(rvelf,rvelf,rva);
            }
            if (sb.invmoi != 0) {
                vsub2vv(rb,cp,sb.pos);
                vcross2zv(rvb,sb.rotvel,rb);
                vsub2vv(rvelf,rvelf,rvb);
            }
            // try a new direction of force here
            //tang = pointf2x(cn[1],-cn[0]); // 90 degrees to normal
            tang[0] = cn[1];
            tang[1] = -cn[0];
            float rvelt = -sdot2vv(rvelf,tang);
            if (rvelt < 0) { // make sure force is opposite the rvelf
                rvelt = -rvelt;
                tang[0] = -tang[0];
                tang[1] = -tang[1];
            }
            if (rvelt > 0) {
                float timt = tim;
                if (sa.invmoi != 0) {
                    racnt = scross2vv(ra,tang);
                    timt += racnt*racnt*sa.invmoi;
                }
                if (sb.invmoi != 0) {
                    rbcnt = scross2vv(rb,tang);
                    timt += rbcnt*rbcnt*sb.invmoi;
                }
                if (elastfric) {
                    f = 2*rvelt/timt; // this f will bounce it back
                } else {
                    f = rvelt/timt; // this f will stop objects
                    float fs = k * ustatic;
                    if (f > fs) { // then slip
                        float fd = k * udynamic;
                        f = fd;
                    }
                }
            }
        }

        // apply new friction impulse
        if (f != 0) {
            if (sa.invmass != 0) {
                float dvat = f*sa.invmass;
                vmul2sv(tvat,dvat,tang);
                vadd2vv(sa.vel,sa.vel,tvat);
            }
            if (sa.invmoi != 0) {
                sa.rotvel += f*racnt*sa.invmoi;
            }
            if (sb.invmass != 0) {
                float dvbt = f*sb.invmass;
                vmul2sv(tvbt,dvbt,tang);
                vsub2vv(sb.vel,sb.vel,tvbt);
            }
            if (sb.invmoi != 0) {
                sb.rotvel -= f*rbcnt*sb.invmoi;
            }
        }
        // position update due to penetration, maybe do sooner
        float resolvepen = .1f; // 0 to 1, 0 never, 1 instant
        if (sa.invmass != 0) {
            float pena = resolvepen*penm*sa.invmass/tim;
            vmul2sv(tda,pena,cn);
            vadd2vv(sa.pos,sa.pos,tda);
        }
        if (sb.invmass != 0) {
            float penb = resolvepen*penm*sb.invmass/tim;
            vmul2sv(tdb,penb,cn);
            vsub2vv(sb.pos,sb.pos,tdb);
        }

    }

    // move an object given it's position,rotation,velocity,and rotation velocity using littleg and timestep
    static float[] at2 = new float[2];
    static float[] vt = new float[2];
    static float[] at = new float[2];
    static float resrot;
    static float resrotvel;
    void moveobj(float[] p,float r,float[] pv,float rv,float ts)
    {
        // air friction
        vmul2sv(pv,vdamp,pv); // beware, should be damp^ts, we'll see
        rv *= rvdamp;
        // integrator
        // p1 = p0 + v0t + 1/2at^2
        vmul2sv(at2,.5f*ts*ts,littleg);
		vmul2sv(vt,ts,pv);
        //logger("vt = %f %f, at2 = %f %f\n",vt[0],vt[1],at2[0],at2[1]);
        // this code doesn't compile correctly in code blocks unless you use buggy variable, objects will move real slow in release version of code blocks
        vadd2vv(p,p,vt);
        vadd2vv(p,p,at2); // this order matters for gnu compilers for some reason, buggy, O o1 and o3 worked, none, o2 didn't
        //p[0] += .1;
        // v1 = v0 + at
        vmul2sv(at,ts,littleg);
        //logger("at = %f\n",at); // bad
        //logger("at = %f %f\n",at[0],at[1]); // good
        vadd2vv(pv,pv,at);
        // rotate
        if (rv != 0) {
            r += ts * rv;
            r = NuMath.normalangrad(r);
        }
        //logger("svel = %f %f\n",pv[0],pv[1]);
        resrot = r;
        resrotvel = rv;
    }

    // run the physics etc
    void procshapes(float ts) {
        //logger("procshapes %f\n",ts);
        boolean dontdo = false;
        if (dontdo)
            return;
        penergy = 0;
        kenergy = 0;
        renergy = 0;
        int i,j;
        // move all objects
        for (i=0;i<shapes.size();++i) {
            Shape s = shapes.get(i);;
            //logger("shape %3d, pos %f, %f vel %f, %f\n",i,s.pos[0],s.pos[1],s.vel[0],s.vel[1]);
            // preprocess planks
            if (s.kind == Kind.Plank) {
                Plank p = (Plank)s;
                p.calcpr();
            }
            // move
            if (s.invmass <= 0)
                continue;	// can't move walls etc.
            moveobj(s.pos,s.rot,s.vel,s.rotvel,ts);
            s.rot = resrot;
            s.rotvel = resrotvel;
        }
        // do shape to shape collisions
        for (i=0;i<shapes.size();++i) {
            Shape sa = shapes.get(i);
            for (j=i+1;j<shapes.size();++j) {
                Shape sb = shapes.get(j);
                collide(sa,sb);
            }
        }
        // update stats
        for (i=0;i<shapes.size();++i) {
            Shape s = shapes.get(i);
            if (s.invmass != 0) {
                float m = 1/s.invmass;
                penergy -= m*sdot2vv(s.pos,littleg); // littleg is <0
                kenergy += m*.5f*sdot2vv(s.vel,s.vel);
            }
            if (s.invmoi != 0) {
                renergy += .5f*s.rotvel*s.rotvel/s.invmoi;
            }
        }
        tenergy = penergy + kenergy + renergy;
    }


    // move physics data to tree graphic hierarchy
    void drawshapes() {
        int i;
        //ps = "shapes: ";
        for (i=0;i<shapes.size();++i) {
            Shape s = shapes.get(i);
//		s.show(); // text
            s.draw(); // update graphic data with physics data
        }
        //drawpoint(new Point2(100,200),8);
    }

    // remove some shapes from the end
    void removeshapes(int rem) {
        int b = shapes.size() - rem;
        int e = shapes.size();
        for (int c=e-1;c>=b;--c) {
            Tree killTree = shapes.get(c).t;
            if (killTree != null) {
                killTree.glFree();
                killTree.unlinkchild();
            }
            shapes.remove(c);//(shapes[c]);
        }
        //shapes.resize(shapes.size() - rem);
        //for (i=shapes.size() -1;
    }

    void resetmul() {
        mul = 0;
    }

    Runnable runResetPhysics2D = new Runnable() {
        @Override
        public void run() {
            StateMan.changeState("Physics2d");
        }
    };

    // input events
// add or remove shapes depending on the (mouse) button value
    void moreLessShapes(int v) {// v is increase or decrease in number of shapes
        Log.i(TAG,"(morelessshapes " + v);
        float ds = v*(float)Math.floor(Math.exp(mul/16));
        if (ds > 0) { // add new shapes
            int i;
            for (i=0;i<ds;++i) {
                if (shapes.size() >= maxshapes)
                    return;
                if ((shapes.size() % 2) == 1) {
                    new Ball(1,
                            200,200,0,
                            5,20*(float)Math.random(),0,
                            50*(float)Math.random()+25
                    );
                } else {
                    float w = 125*(float)Math.random()+20;
                    float h = 125*(float)Math.random()+20;
                    new Plank(1,
                            200,200,0,
//					5,15,0,
                            5,20*(float)Math.random(),0,
                            w,h
                    );
                }
            }
        } else if (ds < 0) { // remove shapes
            if (minshapes > shapes.size() + ds) {
                ds = (float)(minshapes - shapes.size());
            }
            removeshapes((int)-ds); // remove from end
        }
        ++mul;
    }

    Runnable moreShapes = new Runnable() {
        @Override
        public void run() {
            moreLessShapes(1);
        }
    };

    Runnable lessShapes = new Runnable() {
        @Override
        public void run() {
            moreLessShapes(-1);
        }
    };

    @Override
    public void init() {
        testFunctionArrays();
        Utils.pushandsetdir("physics2d");
        Log.i(TAG, "entering physics2d");
        // main scene
        roottree = new Tree("roottree");
        roottree.trans = new float[] {0,0,aback};
        initpoints();

        backgroundtree = new Tree("backgroundtree");
        backgroundtree.trans = new float[] {0,0,aback};
        // build a prism
        Log.e(TAG, "Screen (" + SX + "," + SY + ") asp = " + SASP + ", Background (" + BX + "," + BY + ") asp = " + BASP);

        // make width wider, landscape
        //Utils.pushandsetdir("common");
        //atree =  ModelUtil.buildplanexy("aplane",.5f* BASP,.5f, "caution.png", "tex"); // name, size, texture, generic texture shader
        bt =  ModelUtil.buildplanexy("aplane",.5f*BASP,.5f, "take0005.jpg", "tex"); // name, size, texture, generic texture shader
        bt.mod.flags |= Model.FLAG_NOZBUFFER;
        //Utils.popdir();
        //if (true) {
        scaleOrient(backgroundtree);
        scaleOrient(roottree);
        scaleOrient(helpertree);
        //atree.trans = new float[] {0,0,aback};
        backgroundtree.linkchild(bt); // link to and pass ownership to backgroundtree


// load up physics2d objects
        //shapes = loadshapes();

        // TODO: should share models (refcount) not unique
        if (shapes.size() <= 4) {
            shapes.clear();
            boolean setim = true;
            if (setim) {
                // immovable objects
                new Wall(0, 0,
                        1, 0
                );
                new Wall(BX, 0,
                        -1, 0
                );
                new Wall(0, 0,
                        0, 1
                );
                new Wall(0, BY,
                        0, -1
                );
                new Plank(0,
                        400, 200, (float) Math.PI * (3.0f / 2.0f),
                        0, 0, 0,
                        150, 50
                );
            }
            minshapes = shapes.size();

            // movable objects
            boolean set1 = true;
            if (set1) {
                new Plank(1,
                        260,480,(float)Math.PI*(1/16.0f),
                        0,-.5f,0,
                        150,50
                );
                new Plank(1,
                        200,380,(float)Math.PI*(1.0f/16.0f),
                        5,-.5f,0,
                        150,50
                );
                new Ball(1,
                        200,80,(float)Math.PI*(1.0f/16.0f),
                        2,-.5f,-.8f,
                        50
                );
                new Ball(1,
                        200,180,(float)Math.PI*(5.0f/16.0f),
                        2,.5f,0,
                        20
                );
            }
            boolean set2 = false;
            if (set2) {
                new Plank(0,
                        200, 180, 0,
                        0, 0, 0,
                        150, 50
                );
                new Ball(1,
                        200, 240, 0,
                        0, -3, 0,
                        50
                );
            }
            boolean set3 = false;
            if (set3) {
                new Plank(1,
                        260, 360, (float)Math.PI * (3.0f / 16.0f),
                        0, -2, 0,
                        150, 50
                );
                new Plank(1,
                        200, 280, (float)Math.PI * (0.0f / 16.0f),
                        0, 0, 0,
                        150, 50
                );
            }
        }
        // setup camera, reset on exit, move back some LHC (left handed coords) to view plane
        ViewPort.mainvp.trans = new float[] {0,0,0};
        // ui
        SimpleUI.setbutsname("physics2d");
        SimpleUI.makeabut("Reset", runResetPhysics2D);
        SimpleUI.makeabut("More Shapes", moreShapes);
        SimpleUI.makeabut("Less Shapes", lessShapes);
        //ViewPort.mainvp.setupViewportUI(1.0f/512.0f); // create some UI under 'viewport'
    }



    @Override
    public void proc() {
        // get input
        InputState ir = Input.getResult();
        // proc
        resetpoints();
        scaleOrient(backgroundtree);
        scaleOrient(roottree);
        scaleOrient(helpertree);
        backgroundtree.proc(); // do animation and user proc if any
        roottree.proc(); // do animation and user proc if any
        helpertree.proc(); // do animation and user proc if any
        /*if (wininfo.mleftclicks)
            morelessshapes(1);
        if (wininfo.mrightclicks)
            morelessshapes(-1); */
        resetmul();
        int i;
        float ts = timestep / timemul;
        //for (i=0;i<timemul;++i) {
            //laststep = i == timemul - 1;
            procshapes(ts);
        //}
        // draw
        drawshapes();
        roottree.proc();
        ViewPort.mainvp.doflycam(ir);
    }

    @Override
    public void draw() {
        // setup camera
        ViewPort.mainvp.beginscene();
        // draw scene from camera
        backgroundtree.draw();
        roottree.draw();
        helpertree.draw();
    }

    @Override
    public void exit() {
        SimpleUI.clearbuts("viewport"); // remove viewport UI
        SimpleUI.clearbuts("physics2d"); // remove physics2d UI
        // reset main ViewPort to default
        ViewPort.mainvp = new ViewPort();
        // show current usage
        Log.i(TAG,"exiting physics2d");
        Log.i(TAG, "logging roottree");
        roottree.log();
        Log.i(TAG, "logging backgroundtree");
        backgroundtree.log();
        Log.i(TAG, "logging helpertree");
        helpertree.log();
        Log.i(TAG, "logging mastertree");
        mastertree.log();
        Log.i(TAG,"logging reference lists\n");
        logrc(); // show all allocated resources
        // cleanup
        freeshapes(); // maybe free tree's, if not, let delete roottree do that
        exitpoints();
        roottree.glFree();
        roottree = null;
        backgroundtree.glFree();
        backgroundtree = null;
        // show usage after cleanup
        Log.i(TAG, "logging reference lists after free");
        logrc(); // show all allocated resources, should be clean
        Utils.popdir();
    }

}
