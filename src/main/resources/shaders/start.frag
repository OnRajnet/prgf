#version 150

in vec3 normal;
in vec3 light;
in vec3 viewDirection;

in vec4 depthTextureCoord;
in vec2 texCoord;

uniform sampler2D depthTexture;
uniform sampler2D textureMosaic;

out vec4 outColor; // (vždy jediný) výstup z fragment shaderu

void main() {
	// ambientní složka
	vec4 ambient = vec4(vec3(0.3), 1.0);

	// difuzní složka
	float NdotL = max(0, dot(normalize(normal), normalize(light)));
	vec4 diffuse = vec4(NdotL * vec3(0.3), 1);

	// odrazová složka
	vec3 halfVector = normalize(normalize(light) + normalize(viewDirection));
	float NdotH = dot(normalize(normal), halfVector);
	vec4 specular = vec4(pow(NdotH, 16) * vec3(1.0), 1);

	vec4 finalColor = ambient + diffuse + specular;
	vec4 textureColor = texture(textureMosaic, texCoord);

	// "z" hodnota z textury
	// R, G, i B složky jsou stejné
	float zLight = texture(depthTexture, depthTextureCoord.xy).r;

	// 0.001 - bias na odstranění tzv.akné
	// lze vyzkoušet různé hodnoty (0.01, 0.001, 0.0001, 0.00001)
	float zActual = depthTextureCoord.z - 0.001;

	bool shadow = zLight < zActual;

	if (shadow) {
		// outColor = vec4(0, 0, 1, 1);
		outColor = textureColor * ambient;
	} else {
		// outColor = vec4(1, 1, 0, 1);
		outColor = textureColor * finalColor;
	}

} 
