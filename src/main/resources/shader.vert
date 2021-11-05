#version 460

// Adapted from http://www.cs.toronto.edu/~jacobson/phong-demo/

layout(location = 0) attribute vec3 position;
layout(location = 1) attribute vec4 color;
layout(location = 2) attribute vec3 normal;
layout(location = 3) attribute vec2 texCoord;

layout(location = 0) uniform mat4 projectionMatrix;
layout(location = 1) uniform mat4 viewMatrix;
layout(location = 2) uniform mat4 modelMatrix;
layout(location = 3) uniform mat4 textureMatrix;
uniform vec3 lightPosition;// Light position


varying Data {
    vec3 position;
    vec4 color;
    vec3 normal;
    vec2 texCoord;
    vec3 lightPosition;
} Output;

void main() {
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0);
    Output.position = vec3(viewMatrix * modelMatrix * vec4(position, 1.0));
    Output.color = color;
    mat3 normalMatrix = mat3(transpose(inverse(viewMatrix * modelMatrix)));
    Output.normal = normalMatrix * normal;
    Output.texCoord = (textureMatrix * vec4(texCoord, 0.0, 1.0)).st;
    Output.lightPosition = vec3(viewMatrix * vec4(lightPosition, 1.0));
}
