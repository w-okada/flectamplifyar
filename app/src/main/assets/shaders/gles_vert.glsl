uniform highp int u_EnableShading;
uniform highp int u_EnableTexture;
uniform vec4 u_ObjectColor;
uniform vec4 u_LightAmbient;
uniform vec4 u_LightDiffuse;
uniform vec4 u_LightSpecular;
uniform vec4 u_LightPos;
uniform vec4 u_MaterialAmbient;
uniform vec4 u_MaterialDiffuse;
uniform vec4 u_MaterialSpecular;
uniform float u_MaterialShininess;
uniform mat4 u_MMatrix;
uniform mat4 u_PMMatrix;

attribute vec4 a_Position;
attribute vec3 a_Normal;
attribute vec2 a_Texcoord;

varying vec4 v_Color;
varying vec2 v_Texcoord;

// For Occulusion
varying vec3 v_ViewPosition;
varying vec3 v_ViewNormal;
varying vec3 v_ScreenSpacePosition;




void main(){
    if (u_EnableShading==1) {
        vec4 ambient=u_LightAmbient*u_MaterialAmbient;
        vec3 P=vec3(u_MMatrix*a_Position);
        vec3 L=normalize(vec3(u_LightPos)-P);
        vec3 N=normalize(mat3(u_MMatrix)*a_Normal);
        float dotLN=max(dot(L,N),0.0);
        vec4 diffuseP=vec4(dotLN);
        vec4 diffuse=diffuseP*u_LightDiffuse*u_MaterialDiffuse;
        vec3 V=normalize(-P);
        float dotNLEffect=ceil(dotLN);
        vec3 R=2.*dotLN*N-L;
        float specularP=pow(max(dot(R,V),0.0),u_MaterialShininess)*dotNLEffect;
        vec4 specular=specularP*u_LightSpecular*u_MaterialSpecular;
        v_Color=ambient+diffuse+specular;
    } else {
        v_Color=u_ObjectColor;
        v_ViewPosition = (u_MMatrix * a_Position).xyz;
        v_ScreenSpacePosition = gl_Position.xyz / gl_Position.w;
    }
    gl_Position=u_PMMatrix*a_Position;
    if (u_EnableTexture==1) {
        v_Texcoord = a_Texcoord;

        v_ViewPosition = (u_MMatrix * a_Position).xyz;
        v_ViewNormal = normalize((u_MMatrix * vec4(a_Normal, 0.0)).xyz);
        v_ScreenSpacePosition = gl_Position.xyz / gl_Position.w;
    }
}


