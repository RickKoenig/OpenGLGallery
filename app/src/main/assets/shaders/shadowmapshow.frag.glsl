#define INVERT

uniform sampler2D uSampler0;
uniform highp float alphacutoff;      
     
varying highp vec2 vTextureCoord;

highp float fact = 50.0;

#ifdef INVERT
void main(void) {
	gl_FragColor = texture2D(uSampler0,vTextureCoord,-1.0) * fact;
}


#else

void main(void) {
	gl_FragColor = texture2D(uSampler0,vTextureCoord,-1.0);
	gl_FragColor.rgb = vec3(1.0 - (1.0 - gl_FragColor.r)*fact);
	gl_FragColor.a = 1.0;
	//if (gl_FragColor.r < 1.0)
	//	gl_FragColor.r = 0.0;
	//if (gl_FragColor.a < alphacutoff)
	//	discard;
}

#endif