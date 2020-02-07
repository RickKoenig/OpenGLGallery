uniform sampler2D uSampler0;
uniform mediump float alphacutoff;

varying mediump vec2 vTextureCoord;

void main(void) {
	gl_FragColor = texture2D(uSampler0,vTextureCoord,-1.0);
    //gl_FragColor = vec4(0.,1.,0.,1.);
	//gl_FragColor.r = 1.0;
	if (gl_FragColor.a < alphacutoff)
		discard;
}
