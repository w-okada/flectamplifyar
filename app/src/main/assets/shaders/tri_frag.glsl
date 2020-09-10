precision mediump float;
uniform highp int u_EnableShading;
uniform highp int u_EnableTexture;
uniform sampler2D u_Texture;


uniform sampler2D u_DepthTexture;
uniform mat3 u_DepthUvTransform;
uniform float u_DepthAspectRatio;

varying vec2 v_Texcoord;
varying vec4 v_Color;




// for occlusion

varying vec3 v_ViewPosition;
varying vec3 v_ViewNormal;
//varying vec2 v_TexCoord;
varying vec3 v_ScreenSpacePosition;
uniform vec4 u_ObjColor;
//


float DepthGetMillimeters(in sampler2D depth_texture, in vec2 depth_uv) {
    vec3 packedDepthAndVisibility = texture2D(depth_texture, depth_uv).xyz;
    return dot(packedDepthAndVisibility.xy, vec2(255.0, 256.0 * 255.0));
}

float DepthInverseLerp(in float value, in float min_bound, in float max_bound) {
    return clamp((value - min_bound) / (max_bound - min_bound), 0.0, 1.0);
}

float DepthGetVisibility(in sampler2D depth_texture, in vec2 depth_uv,
in float asset_depth_mm) {
    float depth_mm = DepthGetMillimeters(depth_texture, depth_uv);
    const float kDepthTolerancePerMm = 0.015;
    float visibility_occlusion = clamp(0.5 * (depth_mm - asset_depth_mm) /
    (kDepthTolerancePerMm * asset_depth_mm) + 0.5, 0.0, 1.0);
    float visibility_depth_near = 1.0 - DepthInverseLerp(
    depth_mm, /*min_depth_mm=*/150.0, /*max_depth_mm=*/200.0);
    float visibility_depth_far = DepthInverseLerp(
    depth_mm, /*min_depth_mm=*/7500.0, /*max_depth_mm=*/8000.0);
    const float kOcclusionAlpha = 0.0;
    float visibility =
    max(max(visibility_occlusion, kOcclusionAlpha),
    max(visibility_depth_near, visibility_depth_far));
    return visibility;
}

float DepthGetBlurredVisibilityAroundUV(in sampler2D depth_texture, in vec2 uv,in float asset_depth_mm) {
    // Kernel used:
    // 0   4   7   4   0
    // 4   16  26  16  4
    // 7   26  41  26  7
    // 4   16  26  16  4
    // 0   4   7   4   0
    const float kKernelTotalWeights = 269.0;
    float sum = 0.0;

    const float kOcclusionBlurAmount = 0.01;
    vec2 blurriness = vec2(kOcclusionBlurAmount,
    kOcclusionBlurAmount * u_DepthAspectRatio);

    float current = 0.0;

    current += DepthGetVisibility(depth_texture, uv + vec2(-1.0, -2.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(+1.0, -2.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(-1.0, +2.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(+1.0, +2.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(-2.0, +1.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(+2.0, +1.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(-2.0, -1.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(+2.0, -1.0) * blurriness, asset_depth_mm);
    sum += current * 4.0;

    current = 0.0;
    current += DepthGetVisibility(depth_texture, uv + vec2(-2.0, -0.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(+2.0, +0.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(+0.0, +2.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(-0.0, -2.0) * blurriness, asset_depth_mm);
    sum += current * 7.0;

    current = 0.0;
    current += DepthGetVisibility(depth_texture, uv + vec2(-1.0, -1.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(+1.0, -1.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(-1.0, +1.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(+1.0, +1.0) * blurriness, asset_depth_mm);
    sum += current * 16.0;

    current = 0.0;
    current += DepthGetVisibility(depth_texture, uv + vec2(+0.0, +1.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(-0.0, -1.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(-1.0, -0.0) * blurriness, asset_depth_mm);
    current += DepthGetVisibility(depth_texture, uv + vec2(+1.0, +0.0) * blurriness, asset_depth_mm);
    sum += current * 26.0;

    sum += DepthGetVisibility(depth_texture, uv , asset_depth_mm) * 41.0;

    return sum / kKernelTotalWeights;
    // return 1.0f;
}

void main() {
    if (u_EnableTexture==1) {
        if (u_EnableShading==1) {
            gl_FragColor = v_Color*texture2D(u_Texture, v_Texcoord);
        } else {
            gl_FragColor = texture2D(u_Texture, v_Texcoord);
        }
        const float kMetersToMillimeters = 1000.0;
        float asset_depth_mm = v_ViewPosition.z * kMetersToMillimeters * -1.;
        vec2 depth_uvs = (u_DepthUvTransform * vec3(v_ScreenSpacePosition.xy, 1)).xy;

//        gl_FragColor *= DepthGetBlurredVisibilityAroundUV(u_DepthTexture, depth_uvs, asset_depth_mm);
    } else {
        gl_FragColor=v_Color;
    }
}

