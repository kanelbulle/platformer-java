
uniform sampler2D textureID;
uniform vec3 light1;
uniform float u_min_light;

varying vec3 v_vertex;
varying vec3 v_normal;
varying vec2 v_texCoord;

void main() {
	float intensity = max(dot(normalize(v_normal.xyz), normalize(light1 - v_vertex)), 0.0);
	intensity = clamp(intensity, u_min_light, 1.0);
    gl_FragColor = vec4(texture2D(textureID, v_texCoord)) * intensity;
}
