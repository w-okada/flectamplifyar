package com.example.flectamplifyar.model

import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import com.amplifyframework.api.graphql.model.ModelMutation
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Element
import com.example.flectamplifyar.dbmodel.DBObject
import com.example.flectamplifyar.ui.ARFragment
import com.example.flectamplifyar.ui.ARFragmentSurfaceRenderer
import com.google.gson.Gson
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.vecmath.Vector3f
import kotlin.concurrent.read
import kotlin.concurrent.thread
import kotlin.concurrent.write

object StrokeProvider{
    lateinit var arFragment: ARFragment
    val mStrokes: MutableList<Stroke> = ArrayList()
    val lock = ReentrantReadWriteLock()
    val UPLOAD_INTERVAL = 10
    var newAddedPointNum = 0


    fun setup(arFragment: ARFragment){
        this.arFragment = arFragment

    }


    fun addNewEvent(motionEvent:MotionEvent, newPoint:Vector3f){
        when(motionEvent.action) {
            MotionEvent.ACTION_DOWN ->{

                // 前のUPが検出されない場合の救済
                if(mStrokes.size > 0){
                    thread {
                        updateDB(mStrokes[mStrokes.size - 1])
                    }
                }

                // 新規Stroke処理
                val stroke = Stroke(UUID.randomUUID().toString())
                stroke.localLine = true
                stroke.setLineWidth(0.006f)
                stroke.add(newPoint)
                lock.write{
                    mStrokes.add(stroke)
                }
                thread{
                    updateDB(stroke, true)
                }
                newAddedPointNum = 0

            }
            MotionEvent.ACTION_MOVE ->{
                lock.write {
                    mStrokes[mStrokes.size - 1].add(newPoint)
                }
                newAddedPointNum += 1
                if(newAddedPointNum % UPLOAD_INTERVAL == 0){
                    thread {
                        updateDB(mStrokes[mStrokes.size - 1])
                    }
                }
            }
            MotionEvent.ACTION_UP ->{
                lock.write {
                    mStrokes[mStrokes.size - 1].add(newPoint)
                }
                thread {
                    updateDB(mStrokes[mStrokes.size - 1])
                }
            }
            else ->{}
        }
    }

    private fun updateDB(stroke: Stroke, add:Boolean=false){
        val dbobj:DBObject
        lock.read {
            dbobj= DBObject(DBObject.TYPE.LINE, DBObject.TEXTURE_TYPE.COLOR, Color.RED, "", "", stroke.getPoints())
        }
        val json = Gson().toJson(dbobj)

        try {
            val element = Element.builder()
                .owner("owner")
                .content(json)
                .id(stroke.UUID)
                .canvas(arFragment.currentMarker!!.canvases[0])
                .build()
            if (add == true) {
                Amplify.API.mutate(
                    ModelMutation.create(element),
                    { response -> Log.i("MyAmplifyApp", "Create element with id: " + response) },
                    { error -> Log.e("MyAmplifyApp", "Create element failed", error) }
                )
            } else {
                Amplify.API.mutate(
                    ModelMutation.update(element),
                    { response -> Log.i("MyAmplifyApp", "Updated element with id: " + response) },
                    { error -> Log.e("MyAmplifyApp", "Update element failed", error) }
                )
            }
        }catch(e:Exception){
            Log.e("TTTTTTTTTTTTTTTTTTTTTt", "${e}")
            Log.e("TTTTTTTTTTTTTTTTTTTTTt", "${arFragment.currentMarker}")
            Log.e("TTTTTTTTTTTTTTTTTTTTTt", "${arFragment.currentMarker!!.canvases}")

        }

    }
}

