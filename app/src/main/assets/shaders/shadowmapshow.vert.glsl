attribute vec3 vertexPositionAttribute;
attribute vec2 textureCoordAttribute;

uniform mat4 mvMatrixUniform;
uniform mat4 pMatrixUniform;

varying highp vec2 vTextureCoord;

void main(void) {
	gl_Position = pMatrixUniform * (mvMatrixUniform * vec4(vertexPositionAttribute, 1.0));
	vTextureCoord = vec2(textureCoordAttribute.s,1.0-textureCoordAttribute.t);
}
