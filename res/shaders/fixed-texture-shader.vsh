
attribute vec4 vertex;
attribute vec4 normal;
attribute vec2 texture;

uniform mat4 projection_matrix;
uniform mat4 modelview_matrix;
uniform mat3 normal_matrix;

varying vec3 v_vertex;
varying vec3 v_normal;
varying vec2 v_texCoord;

void main() {
	gl_Position = projection_matrix * modelview_matrix * vertex;
	
	v_texCoord = texture;
	v_normal = normalize(normal_matrix * normalize(normal.xyz));
	v_vertex = (modelview_matrix * vertex).xyz;
}
