package com.example.flectamplifyar.ui

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.NavHostFragment
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.api.rest.RestOptions
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Marker
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import com.example.flectamplifyar.App
import com.example.flectamplifyar.R
import kotlinx.android.synthetic.main.arfragment.view.*
import kotlinx.android.synthetic.main.image_capture_view.view.*
import java.io.File
import java.util.*

class ImageCaptureView: ConstraintLayout {
    companion object{
        val TAG = ImageCaptureView::class.java.simpleName
    }
    constructor(context: Context) : this(context, null){}
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.image_capture_view, this, true)
    }

    var bm:Bitmap? = null
    fun getBitmap():Bitmap{
        return bm!!
    }
    var uploadSucceeded = false

    override fun onViewAdded(view: View?) {

        captureView.addCallback(
            object : OverlayView.DrawCallback {
                override fun drawCallback(canvas: Canvas) {

                    val bmp = Bitmap.createBitmap(captureView.width, captureView.height, Bitmap.Config.ARGB_8888);
                    val c   = Canvas(bmp)
                    val p = Paint().apply {
                        color = Color.argb(200,100,100,100)
                    }

                    canvas.drawRect(0f, 0f, captureView.width.toFloat(), captureView.height.toFloat(), p)
                    p.xfermode = PorterDuffXfermode( PorterDuff.Mode.CLEAR )
                    canvas.drawRect(captureView.width.toFloat()/2 - ARFragment.MARKER_WIDTH /2, captureView.height.toFloat()/2 - ARFragment.MARKER_HEIGHT /2,
                        captureView.width.toFloat()/2 + ARFragment.MARKER_WIDTH /2, captureView.height.toFloat()/2 + ARFragment.MARKER_HEIGHT /2,
                        p)

                    p.apply {
//                        xfermode = PorterDuffXfermode( PorterDuff.Mode.DST_OVER)
                        xfermode = PorterDuffXfermode( PorterDuff.Mode.SRC_OVER)
                        style = Paint.Style.STROKE
                        strokeWidth = 25f
                        color = Color.argb(100,250,0,0)
                    }

                    canvas.drawRect(captureView.width.toFloat()/2 - ARFragment.MARKER_WIDTH /2, captureView.height.toFloat()/2 - ARFragment.MARKER_HEIGHT /2,
                        captureView.width.toFloat()/2 + ARFragment.MARKER_WIDTH /2, captureView.height.toFloat()/2 + ARFragment.MARKER_HEIGHT /2,
                        p)
                }
            }
        )

        val mHandler = Handler(Looper.getMainLooper());
        captureView.setOnClickListener {
            val bitmap = Bitmap.createBitmap(rootView.rSurfaceView.width, rootView.rSurfaceView.height, Bitmap.Config.ARGB_8888)
            PixelCopy.request(rootView.rSurfaceView, bitmap,
                { result ->
                    if (result == PixelCopy.SUCCESS) {
                        bm = Bitmap.createBitmap(bitmap, bitmap.width/2 - ARFragment.MARKER_WIDTH /2, bitmap.height/2 - ARFragment.MARKER_HEIGHT /2, ARFragment.MARKER_WIDTH, ARFragment.MARKER_HEIGHT);
                        markerCaptureConfirmLayout.visibility = View.VISIBLE
                        confirmingMarkerImage.setImageBitmap(bm)
                    } else {
                        Toast.makeText(context, "失敗しました", Toast.LENGTH_SHORT).show()
                    }
                },
                mHandler
            )

            editTextMarkerName.visibility = View.VISIBLE
            uploadMarkerButton.visibility = View.VISIBLE
            cancelMarkerButton.visibility = View.VISIBLE

        }
        markerCaptureConfirmLayout.setOnClickListener{}
        cancelMarkerButton.setOnClickListener{
            markerCaptureConfirmLayout.visibility = View.INVISIBLE
        }
        exitCaptureMarkerButton.setOnClickListener{
            markerCaptureConfirmLayout.visibility = View.INVISIBLE
            imageCaptureView.visibility = View.INVISIBLE
        }

        uploadMarkerButton.setOnClickListener {
            val uuidString = UUID.randomUUID().toString()
            uploadMarker(bm!!, "${uuidString}.jpg", editTextMarkerName.text.toString())
            waitUploadingStatusText.text = "uploading...."

            editTextMarkerName.visibility = View.INVISIBLE
            uploadMarkerButton.visibility = View.INVISIBLE
            cancelMarkerButton.visibility = View.INVISIBLE

            waitUploadingStatusText.visibility = View.VISIBLE
            waitUploadingProgressBar.visibility = View.VISIBLE

            uploadSucceeded = false
        }

        waitUploadingExitButton.setOnClickListener{
            markerCaptureConfirmLayout.visibility = View.INVISIBLE
            imageCaptureView.visibility = View.INVISIBLE
            waitUploadingExitButton.visibility = View.INVISIBLE
            waitUploadingStatusText.visibility = View.INVISIBLE
            if(uploadSucceeded){
                val nav = (context as AppCompatActivity).supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
                // https://issuetracker.google.com/issues/119800853
                // https://stackoverflow.com/questions/58703451/fragmentcontainerview-as-navhostfragment
                val arFragment = nav.childFragmentManager.primaryNavigationFragment
                (arFragment as ARFragment).setMarker(bm!!)
            }
        }
    }



    private fun uploadMarker(bm: Bitmap, filename: String, title:String){
        val exampleFile = File(App.getApp().applicationContext.filesDir, "${filename}")
        bm.compress(Bitmap.CompressFormat.JPEG, 100, exampleFile.outputStream())
        val mHandler = Handler(Looper.getMainLooper());
        val key = "pict/${filename}"
        Log.e("-----", "START UPLOAD")
        Amplify.Storage.uploadFile(
            "${key}",
            exampleFile,
            { result ->
                Log.i("MyAmplifyApp", "Successfully uploaded: " + result.getKey())
                try {
                    val plugin = Amplify.Storage.getPlugin("awsS3StoragePlugin") as AWSS3StoragePlugin
                    val body = "{" +
                            "\"bucket\":\"${plugin.bucketName}\", " +
                            "\"region\":\"${plugin.regionStr}\", " +
                            "\"key\":\"${key}\", " +
                            "\"name\":\"${title}\" " +
                            "}"
                    Log.e(TAG, "BODY !! ${body}")
                    Log.e(TAG, "BODY !! ${title}")
                    val options: RestOptions = RestOptions.builder()
                        .addPath("/markers")
                        .addBody(body.toByteArray())
                        .build()

                    Amplify.API.post(options,
                        { response ->
                            Log.i("MyAmplifyApp", "POST " + response.data.asString())
                            val score = response.data.asJSONObject()["score"]
                            if(score.equals("")){
                                mHandler.post{
                                    waitUploadingStatusText.text = "can not get enough feature from image"
                                    waitUploadingExitButton.visibility = View.VISIBLE
                                    waitUploadingProgressBar.visibility = View.INVISIBLE
                                }
                            }else {
                                mHandler.post {
                                    waitUploadingStatusText.text = "upload image succeeded. Score: ${score}"
                                    waitUploadingExitButton.visibility = View.VISIBLE
                                    waitUploadingProgressBar.visibility = View.INVISIBLE
                                }
                                uploadSucceeded = true
                            }
                        },
                        { error ->
                            Log.e("MyAmplifyApp", "POST failed", error)
                            mHandler.post{
                                waitUploadingStatusText.text = "analyzing image failed"
                                waitUploadingExitButton.visibility = View.VISIBLE
                                waitUploadingProgressBar.visibility = View.INVISIBLE
                            }
                        }
                    )
                } catch (e: Exception) {
                    mHandler.post{
                        waitUploadingStatusText.text = "upload failed"
                        waitUploadingExitButton.visibility = View.VISIBLE
                        waitUploadingProgressBar.visibility = View.INVISIBLE
                    }
                    Log.e("---------------------------", " >  ${e}")
                }

            },
            { error -> Log.e("MyAmplifyApp", "Upload failed", error) }
        )
        Log.e("-----", "START UPLOAD 2")

//        Amplify.Storage.uploadFile(
//            "${key}_",
//            exampleFile,
//            { result ->
//                Log.i("MyAmplifyApp", "Successfully uploaded: " + result.getKey())
//                val plugin = Amplify.Storage.getPlugin("awsS3StoragePlugin") as AWSS3StoragePlugin
//                val body = "{" +
//                        "\"bucket\":\"${plugin.bucketName}\", " +
//                        "\"region\":\"${plugin.regionStr}\", " +
//                        "\"key\":\"${key}\" " +
//                        "}"
//                val options: RestOptions = RestOptions.builder()
//                    .addPath("/markers")
//                    .addBody(body.toByteArray())
//                    .build()
//
//                Amplify.API.post(options,
//                    { response -> Log.i("MyAmplifyApp", "POST " + response.data.asString()) },
//                    { error -> Log.e("MyAmplifyApp", "POST failed", error) }
//                )
//
//            },
//            { error -> Log.e("MyAmplifyApp", "Upload failed", error) }
//        )
//        Log.e("-----","START UPLOAD 2")


//        val marker = Marker.builder().score(11011110).name("abbbbbbb").id("a667841f-a0f2-4e98-bf46-66c68d06135ab").build()
//        Amplify.API.mutate(
//            ModelMutation.update(marker),
//            { response -> Log.i("MyAmplifyApp", "Added Marker with id: " + response) },
//            { error: ApiException? -> Log.e("MyAmplifyApp", "Create failed", error) }
//        )

//        fun getTodo(id: String) {
//            Amplify.API.query(
//                ModelQuery.get(Marker::class.java, id),
//                { response -> Log.i("MyAmplifyApp", response.data.name) },
//                { error -> Log.e("MyAmplifyApp", "Query failed", error) }
//            )
//        }

        Amplify.API.query(
            ModelQuery.list(Marker::class.java, Marker.SCORE.gt(0)),
            { response ->
                for (marker in response.data) {
                    Log.i("MyAmplifyApp", "marker ${marker}")
                }
            },
            { error -> Log.e("MyAmplifyApp", "Query failure", error) }
        )
    }



}