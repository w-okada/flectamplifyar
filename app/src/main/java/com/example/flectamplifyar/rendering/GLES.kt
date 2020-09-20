package com.example.flectamplifyar.rendering

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix

/**
 * Created by tommy on 2015/06/18.
 */
//シェーダ操作
object GLES {
    private val TAG = GLES::class.java.simpleName
    private const val GLES_VERTEX_SHADER_NAME             = "shaders/gles_vert.glsl"
    private const val GLES_FRAGMENT_SHADER_NAME           = "shaders/gles_frag.glsl"

    //システム
    var enableShadingHandle = 0 //shadingを行うflagのハンドル = 0
    var objectColorHandle = 0 //shadingを行わない時に使う単色ハンドル = 0
    var enableTextureHandle = 0//Textureを使うflagのハンドル

    //光源のハンドル
    var lightAmbientHandle = 0 //光源の環境光色ハンドル = 0
    var lightDiffuseHandle  = 0//光源の拡散光色ハンドル = 0
    var lightSpecularHandle  = 0//光源の鏡面光色ハンドル = 0
    var lightPosHandle  = 0//光源の位置ハンドル = 0

    //マテリアルのハンドル
    var materialAmbientHandle  = 0//マテリアルの環境光色ハンドル = 0
    var materialDiffuseHandle  = 0//マテリアルの拡散光色ハンドル = 0
    var materialSpecularHandle  = 0//マテリアルの鏡面光色ハンドル = 0
    var materialShininessHandle  = 0//マテリアルの鏡面指数ハンドル = 0

    //行列のハンドル
    var mMatrixHandle  = 0//モデルビュー行列ハンドル（カメラビュー行列×モデル変換行列） = 0
    var pmMatrixHandle  = 0//(射影行列×モデルビュー行列)ハンドル = 0

    //頂点のハンドル
    var positionHandle  = 0//位置ハンドル = 0
    var normalHandle  = 0//法線ハンドル = 0

    //テクスチャのハンドル
    var texcoordHandle = 0//テクスチャコードハンドル
    var textureHandle = 0//テクスチャハンドル

    //行列
    private var cMatrix = FloatArray(16) //カメラビュー変換行列
    var mvMatrix = FloatArray(16) //モデルビュー変換行列
    private var pMatrix = FloatArray(16) //プロジェクション行列（射影行列）
    var pmMatrix = FloatArray(16) //pMatrix*mvMatrix

    //光源
    private val LightPos = FloatArray(4) //光源の座標　x,y,z　（ワールド座標）
    private val CVLightPos = FloatArray(4) //光源の座標　x,y,z　（カメラビュー座標）

    private val LightAmb = FloatArray(4) //光源の環境光
    private val LightDif = FloatArray(4) //光源の乱反射光
    private val LightSpc = FloatArray(4) //光源の鏡面反射反射光


    // Occlusion
    // Shader location: depth texture.
    var depthTextureUniform = 0
    // Shader location: transform to depth uvs.
    var depthUvTransformUniform = 0
    // Shader location: the aspect ratio of the depth texture.
    var depthAspectRatioUniform = 0

    // Depth-for-Occlusion parameters.
    var depthAspectRatio = 0.0f
    var uvTransform: FloatArray? = null
    var depthTextureId = 0

    fun setUvTransformMatrix(transform: FloatArray) {
        uvTransform = transform
    }
    fun setDepthTexture(textureId: Int, width: Int, height: Int) {
        depthTextureId = textureId
        depthAspectRatio = width.toFloat() / height.toFloat()
    }



    private var myProgram: Int=0 //プログラムオブジェクト

    fun useProgram(){
        GLES20.glUseProgram(myProgram)
    }
    //プログラムの生成
    fun makeProgram(context: Context): Boolean {

        val vertexShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, GLES_VERTEX_SHADER_NAME)
        val fragmentShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, GLES_FRAGMENT_SHADER_NAME)


        //プログラムオブジェクトの生成
        myProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(myProgram, vertexShader)
        GLES20.glAttachShader(myProgram, fragmentShader)
        GLES20.glLinkProgram(myProgram)
        GLES20.glUseProgram(myProgram)
        ShaderUtil.checkGLError(TAG, "program")
//        // リンクエラーチェック
//        val linked = IntArray(1)
//        GLES20.glGetProgramiv(myProgram, GLES20.GL_LINK_STATUS, linked, 0)
//        if (linked[0] <= 0) {
//            Log.e(" makeProgram", "Failed in Linking")
//            Log.e(" makeProgram", GLES20.glGetProgramInfoLog(myProgram))
//            return false
//        }

        //shading可否ハンドルの取得
        enableShadingHandle = GLES20.glGetUniformLocation(myProgram, "u_EnableShading")
        //texture可否ハンドルの取得
        enableTextureHandle = GLES20.glGetUniformLocation(myProgram, "u_EnableTexture")


        //光源のハンドルの取得
        lightAmbientHandle = GLES20.glGetUniformLocation(myProgram, "u_LightAmbient")
        lightDiffuseHandle = GLES20.glGetUniformLocation(myProgram, "u_LightDiffuse")
        lightSpecularHandle = GLES20.glGetUniformLocation(myProgram, "u_LightSpecular")
        lightPosHandle = GLES20.glGetUniformLocation(myProgram, "u_LightPos")

        //マテリアルのハンドルの取得
        materialAmbientHandle = GLES20.glGetUniformLocation(myProgram, "u_MaterialAmbient")
        materialDiffuseHandle = GLES20.glGetUniformLocation(myProgram, "u_MaterialDiffuse")
        materialSpecularHandle = GLES20.glGetUniformLocation(myProgram, "u_MaterialSpecular")
        materialShininessHandle = GLES20.glGetUniformLocation(myProgram, "u_MaterialShininess")
        //光源を使わない時のマテリアルの色のハンドルの取得
        objectColorHandle = GLES20.glGetUniformLocation(myProgram, "u_ObjectColor")

        //行列のハンドルの取得
        mMatrixHandle = GLES20.glGetUniformLocation(myProgram, "u_MMatrix")
        pmMatrixHandle = GLES20.glGetUniformLocation(myProgram, "u_PMMatrix")

        //頂点とその法線ベクトルのハンドルの取得
        positionHandle = GLES20.glGetAttribLocation(myProgram, "a_Position")
        normalHandle = GLES20.glGetAttribLocation(myProgram, "a_Normal")

        //テクスチャのハンドルの取得
        texcoordHandle = GLES20.glGetAttribLocation(myProgram, "a_Texcoord")
        textureHandle = GLES20.glGetUniformLocation(myProgram, "u_Texture")

        // Occlusion

        depthTextureUniform = GLES20.glGetUniformLocation(myProgram, "u_DepthTexture")
        depthUvTransformUniform = GLES20.glGetUniformLocation(myProgram, "u_DepthUvTransform")
        depthAspectRatioUniform = GLES20.glGetUniformLocation(myProgram, "u_DepthAspectRatio")




        //プログラムオブジェクトの利用開始
        GLES20.glUseProgram(myProgram)
        enableShading()
        return true
    }
//
//    //シェーダーオブジェクトの生成
//    private fun loadShader(type: Int, shaderCode: String): Int {
//        val shader = GLES20.glCreateShader(type)
//        GLES20.glShaderSource(shader, shaderCode)
//        GLES20.glCompileShader(shader)
//        // コンパイルチェック
//        val compiled = IntArray(1)
//        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
//        if (compiled[0] == 0) {
//            Log.e(" loadShader", "Failed in Compilation")
//            Log.e(" loadShader", GLES20.glGetShaderInfoLog(shader))
//            return -1
//        }
//        return shader
//    }

    //透視変換の指定
    fun gluPerspective(pm: FloatArray?, angle: Float, aspect: Float, near: Float, far: Float) {
        val top: Float
        val bottom: Float
        val left: Float
        val right: Float
        if (aspect < 1f) {
            top = near * Math.tan(angle * (Math.PI / 360.0)).toFloat()
            bottom = -top
            left = bottom * aspect
            right = -left
        } else {
            right =
                1.1f * near * Math.tan(angle * (Math.PI / 360.0)).toFloat()
            left = -right
            bottom = left / aspect
            top = -bottom
        }
        Matrix.frustumM(pm, 0, left, right, bottom, top, near, far)
    }

    //ワールド座標系のLightPosを受け取る
    fun setLightPosition(lp: FloatArray) {
        System.arraycopy(lp, 0, LightPos, 0, 4)
    }

    fun putLightAttribute(amb: FloatArray?, dif: FloatArray?, spc: FloatArray?) {
        System.arraycopy(amb, 0, LightAmb, 0, 4)
        System.arraycopy(dif, 0, LightDif, 0, 4)
        System.arraycopy(spc, 0, LightSpc, 0, 4)
    }

    //プロジェクション行列（射影行列）を受け取る
    fun setPMatrix(pm: FloatArray) {
        System.arraycopy(pm, 0, pMatrix, 0, 16)
    }

    //カメラビュー変換行列を受け取る
    fun setCMatrix(cm: FloatArray) {
        System.arraycopy(cm, 0, cMatrix, 0, 16)
    }

    //カメラビュー変換行列×モデル変換行列 = モデルビュー行列をシェーダに指定
    fun updateMatrix(mm: FloatArray, anchorMatrix: FloatArray) {

        Matrix.multiplyMM(mvMatrix, 0, anchorMatrix, 0, mm, 0) //mvMatrix = cMatrix * mm
        Matrix.multiplyMM(mvMatrix, 0, cMatrix, 0, mvMatrix, 0) //mvMatrix = cMatrix * mm


//        Matrix.multiplyMM(mvMatrix, 0, cMatrix, 0, mm, 0) //mvMatrix = cMatrix * mm
        Matrix.multiplyMM(pmMatrix, 0, pMatrix, 0, mvMatrix, 0) //pmMatrix = pMatrix * mvMatrix
        //モデルビュー行列をシェーダに指定
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mvMatrix, 0)

        //プロジェクション行列（射影行列）×モデルビュー行列をシェーダに指定
        GLES20.glUniformMatrix4fv(pmMatrixHandle, 1, false, pmMatrix, 0)

        //シェーダはカメラビュー座標系の光源位置を使う
        //ワールド座標系のLightPosを，カメラビュー座標系に変換してシェーダに送る
        Matrix.multiplyMV(CVLightPos, 0, cMatrix, 0, LightPos, 0)
        GLES20.glUniform4f(lightPosHandle, CVLightPos[0], CVLightPos[1], CVLightPos[2], 1.0f)
    }

    fun transformPCM(result: FloatArray, source: FloatArray?) {
        Matrix.multiplyMV(result, 0, pmMatrix, 0, source, 0)
        result[0] /= result[3]
        result[1] /= result[3]
        result[2] /= result[3]
        //result[3]にはsourceのz要素が符号反転されて入っている
    }

    fun enableShading() {
        GLES20.glUniform1i(enableShadingHandle, 1)
    }

    fun disableShading() {
        GLES20.glUniform1i(enableShadingHandle, 0)
    }


    fun enableTexture() {
        GLES20.glUniform1i(enableTextureHandle, 1)
    }

    fun disableTexture() {
        GLES20.glUniform1i(enableTextureHandle, 0)
    }
}