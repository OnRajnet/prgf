#version 150
in vec2 inPosition; // input from the vertex buffer

uniform mat4 view;
uniform mat4 projection;
uniform float type; // type == 1 -> elipsoid; type == 0 -> stěna
uniform float time;

const float PI = 3.1415;

float getZ(vec2 vec) {
	return sin(time + vec.y * PI * 2);
}

vec3 getSphere(vec2 vec) {
	float az = vec.x * PI; // <-1;1> -> <-PI;PI>
	float ze = vec.y * PI / 2.0; // <-1;1> -> <-PI/2;PI/2>
	float r = 1.0;

	float x = r * cos(az) * cos(ze);
	float y = 2 * r * sin(az) * cos(ze);
	float z = 0.5 * r * sin(ze);
	return vec3(x, y, z);
}

vec3 getPlane(vec2 vec) {
	return vec3(vec * 2.5, -1);
}

void main() {
	// grid máme od 0 do 1 a chceme od -1 od 1
	vec2 position = inPosition * 2.0 - 1.0;
//	vec4 pos4 = vec4(position, getZ(position), 1.0);
	vec4 pos4;
	if (type == 1.0) {
		pos4 = vec4(getSphere(position), 1.0);
	} else {
		pos4 = vec4(getPlane(position), 1.0);
	}

	gl_Position = projection * view * pos4;
} 
