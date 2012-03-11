
uniform vec2 u_texcoord;
uniform sampler2D textureID;
uniform sampler2D u_bump_map;
uniform vec3 light1;

varying vec3 v_vertex;
varying vec3 v_normal;
varying vec2 v_texCoord;

void main() {	
	float x_gradient = (texture2D(u_bump_map, v_texCoord + vec2(-1.0, 0.0)) - texture2D(u_bump_map, v_texCoord + vec2(1.0, 0.0))).x;
	float y_gradient = (texture2D(u_bump_map, v_texCoord + vec2(0.0, -1.0)) - texture2D(u_bump_map, v_texCoord + vec2(0.0, 1.0))).x;
	vec3 b_normal = v_normal + vec3(1000.0, 0.0, 0.0) * x_gradient + vec3(0.0, 1000.0, 0.0) * y_gradient;

	float intensity = max(dot(normalize(v_normal.xyz), normalize(light1 - v_vertex)), 0.0);
	intensity = clamp(intensity, 0.0, 1.0);
	
	vec2 texc = vec2(u_texcoord.x, 1.0 - u_texcoord.y);
	gl_FragColor = vec4(texture2D(textureID, texc)) * intensity;
}
