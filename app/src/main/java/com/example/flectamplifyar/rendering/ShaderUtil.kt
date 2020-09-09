package com.example.flectamplifyar.rendering

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

object ShaderUtil{
    fun loadGLShader(tag: String, context: Context, type: Int, filename: String, defineValuesMap: Map<String, Int>): Int {
        var code: String = readShaderFileFromAssets(context, filename)

        // Prepend any #define values specified during this run.
        var defines = ""
        for ((key, value) in defineValuesMap) {
            defines += """#define $key $value""" + "\n"
        }
        code = defines + code

        // Compiles shader code.
        var shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)

        // Get the compilation status.
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(tag, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader))
            Log.e(tag, "Error compiling shader:F: " + filename)
            GLES20.glDeleteShader(shader)
            shader = 0
        }else{
            Log.e(tag, "Success compiling shader:F: " + filename)
        }
        if (shader == 0) {
            throw RuntimeException("Error creating shader.")
        }
        return shader
    }

    fun loadGLShader(tag: String, context: Context, type: Int, filename: String): Int {
        val emptyDefineValuesMap: Map<String, Int> = TreeMap()
        return loadGLShader(tag, context, type, filename, emptyDefineValuesMap)
    }


    fun loadGLShader(tag: String, context: Context, type: Int, resId: Int): Int {
        val code: String = readRawTextFile(context, resId)
        var shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)

        // Get the compilation status.
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(tag, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader))
            GLES20.glDeleteShader(shader)
            shader = 0
        }
        if (shader == 0) {
            throw java.lang.RuntimeException("Error creating shader.")
        }
        return shader
    }


    fun readRawTextFile(context: Context, resId: Int): String {
        val inputStream = context.resources.openRawResource(resId)
        try {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = java.lang.StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            reader.close()
            return sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }



    fun checkGLError(tag: String?, label: String) {
        var lastError = GLES20.GL_NO_ERROR
        // Drain the queue of all errors.
        var error: Int
        while (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
            Log.e(tag, "$label: glError $error")
            lastError = error
        }
        if (lastError != GLES20.GL_NO_ERROR) {
            throw java.lang.RuntimeException("$label: glError $lastError")
        }
    }

    @Throws(IOException::class)
    private fun readShaderFileFromAssets(context: Context, filename: String): String {
        context.assets.open(filename).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                val sb = StringBuilder()
                var line: String
//                    while (reader.readLine().also { line = it } != null) {
                val iter = reader.lineSequence().iterator()
                while (iter.hasNext()) {
                    line = iter.next()

//                        val tokens = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val tokens = line.split(" ").toTypedArray()
                    if (tokens[0] == "#include") {
                        var includeFilename = tokens[1]
                        includeFilename = includeFilename.replace("\"", "")
                        if (includeFilename == filename) {
                            throw IOException("Do not include the calling file.")
                        }
                        sb.append(readShaderFileFromAssets(context, includeFilename))
                    } else {
                        sb.append(line).append("\n")
                    }
                }

                return sb.toString()
            }
        }
    }


}