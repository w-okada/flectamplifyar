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
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.flectamplifyar.R
import kotlinx.android.synthetic.main.arfragment.*
import kotlinx.android.synthetic.main.arfragment.view.*
import kotlinx.android.synthetic.main.image_capture_view.view.*
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

    lateinit var arFragment:ARFragment


    var bitmap:Bitmap? = null

    var uploadSucceeded = false
    var uploadId = ""

    override fun onViewAdded(view: View?) {

        // オーバレイの描画処理を登録、
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

        // CaptureViewがクリックされたときの処理。
        val mHandler = Handler(Looper.getMainLooper());
        captureView.setOnClickListener {
            val bitmap = Bitmap.createBitmap(rootView.rSurfaceView.width, rootView.rSurfaceView.height, Bitmap.Config.ARGB_8888)
            PixelCopy.request(rootView.rSurfaceView, bitmap,
                { result ->
                    if (result == PixelCopy.SUCCESS) {
                        this.bitmap = Bitmap.createBitmap(bitmap, bitmap.width/2 - ARFragment.MARKER_WIDTH /2, bitmap.height/2 - ARFragment.MARKER_HEIGHT /2, ARFragment.MARKER_WIDTH, ARFragment.MARKER_HEIGHT);
                        markerCaptureConfirmLayout.visibility = View.VISIBLE
                        confirmingMarkerImage.setImageBitmap(this.bitmap)
                    } else {
                        Toast.makeText(context, "Failed to generate image bitmap.", Toast.LENGTH_SHORT).show()
                    }
                },
                mHandler
            )

            editTextMarkerName.visibility = View.VISIBLE
            uploadMarkerButton.visibility = View.VISIBLE
            cancelMarkerButton.visibility = View.VISIBLE

        }

        // Exitボタンがクリックされたときの処理(戻る)
        exitCaptureMarkerButton.setOnClickListener{
            markerCaptureConfirmLayout.visibility = View.INVISIBLE
            imageCaptureView.visibility = View.INVISIBLE
            waitUploadingExitButton.visibility = View.INVISIBLE
            waitUploadingStatusText.visibility = View.INVISIBLE

        }

        // Cancelボタンがクリックされたときの処理
        cancelMarkerButton.setOnClickListener{
            markerCaptureConfirmLayout.visibility = View.INVISIBLE
        }

        // Uploadボタンがクリックされたときの処理
        uploadMarkerButton.setOnClickListener {
            val uuidString = UUID.randomUUID().toString()
            arFragment.arOperationListener!!.uploadMarker(
                bitmap!!, "${uuidString}.jpg", editTextMarkerName.text.toString(),
                {uploadId, score ->
                    arFragment.requireActivity().runOnUiThread({
                        waitUploadingStatusText.text = "upload image succeeded. Score: ${score}"
                        waitUploadingExitButton.visibility = View.VISIBLE
                        waitUploadingProgressBar.visibility = View.INVISIBLE
                    })
                    this.uploadId   = uploadId
                    uploadSucceeded = true
                },
                {message ->
                    arFragment.requireActivity().runOnUiThread({
                        waitUploadingStatusText.text = message
                        waitUploadingExitButton.visibility = View.VISIBLE
                        waitUploadingProgressBar.visibility = View.INVISIBLE
                    })
                }
            )
            waitUploadingStatusText.text = "uploading...."

            editTextMarkerName.visibility = View.INVISIBLE
            uploadMarkerButton.visibility = View.INVISIBLE
            cancelMarkerButton.visibility = View.INVISIBLE

            waitUploadingStatusText.visibility = View.VISIBLE
            waitUploadingProgressBar.visibility = View.VISIBLE
        }



        // Exitボタンがクリックされたときの処理(マーカーを確定)
        waitUploadingExitButton.setOnClickListener{
            markerCaptureConfirmLayout.visibility = View.INVISIBLE
            imageCaptureView.visibility = View.INVISIBLE
            waitUploadingExitButton.visibility = View.INVISIBLE
            waitUploadingStatusText.visibility = View.INVISIBLE
            arFragment.main_marker_view.setImageBitmap(this.bitmap)
            if(uploadSucceeded){
                uploadSucceeded = false
                arFragment.arOperationListener!!.setCurrentMarker(uploadId,
                    {message ->
                        Log.e(TAG,"upload marker success. ${message}")
                        arFragment.refreshImageDatabase(bitmap!!)
                    },
                    {message ->
                        Log.e(TAG,"upload marker failed. ${message}")
                    }
                )
            }

        }
    }

}