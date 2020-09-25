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
import kotlin.collections.ArrayList
import kotlin.concurrent.read
import kotlin.concurrent.thread
import kotlin.concurrent.write

object StrokeProvider{
    lateinit var arFragment: ARFragment
    val mStrokes: MutableList<Stroke> = ArrayList()
    val mOthersStrokes: MutableList<Stroke> = ArrayList()

    val textElements: MutableList<TextElement> = ArrayList()
    val OtherTextElements: MutableList<TextElement> = ArrayList()

    val imageElements: MutableList<ImageElement> = ArrayList()
    val OtherImageElements: MutableList<ImageElement> = ArrayList()

    val lock = ReentrantReadWriteLock()
    val othersLock = ReentrantReadWriteLock()
    val UPLOAD_INTERVAL = 10
    var newAddedPointNum = 0


    fun setup(arFragment: ARFragment){
        this.arFragment = arFragment
    }

    fun addNewEvent(motionEvent:MotionEvent, newPoint:Vector3f){
        when(motionEvent.action) {
            MotionEvent.ACTION_DOWN ->{
                when(arFragment.mode){
                    ARFragment.Mode.LINE-> {
                        if(mStrokes.size > 0){
                            thread {
                                updateDB(mStrokes[mStrokes.size - 1])
                            }
                        }
                        val stroke = Stroke(UUID.randomUUID().toString())
                        // 新規Stroke処理
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

                    ARFragment.Mode.TEXT->{
                        val element = TextElement(UUID.randomUUID().toString(), arFragment.textElementText, newPoint)
                        textElements.add(element)
                        thread {
                            updateDB(textElements[textElements.size - 1])
                        }

                    }

                    ARFragment.Mode.IMAGE-> {
                        val element = ImageElement(UUID.randomUUID().toString(), arFragment.imageElementBitmapId, arFragment.imageElementBitmap!!, newPoint)
                        imageElements.add(element)
                        thread {
                            updateDB(imageElements[imageElements.size - 1])
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE ->{
                if(arFragment.mode != ARFragment.Mode.LINE) {
                    return
                }
                // Down検出漏れ対策
                if(mStrokes.size == 0){
                    val stroke = Stroke(UUID.randomUUID().toString())
                    stroke.localLine = true
                    stroke.setLineWidth(0.006f)
                    stroke.add(newPoint)
                    lock.write{
                        mStrokes.add(stroke)
                    }
                }else{
                    lock.write {
                        mStrokes[mStrokes.size - 1].add(newPoint)
                    }
                }

                newAddedPointNum += 1
                if(newAddedPointNum % UPLOAD_INTERVAL == 0){
                    thread {
                        updateDB(mStrokes[mStrokes.size - 1])
                    }
                }
            }
            MotionEvent.ACTION_UP ->{
                if(arFragment.mode != ARFragment.Mode.LINE) {
                    return
                }

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
        val json:String
        lock.read {
            dbobj= DBObject(stroke.uuid, DBObject.TYPE.LINE, DBObject.TEXTURE_TYPE.COLOR, Color.RED, "", 0, stroke.getPoints())
            json = Gson().toJson(dbobj)
        }
        arFragment.arOperationListener!!.updateDB(stroke.uuid, json, add, {}, {})
    }

    private fun updateDB(textElement:TextElement){
        val dbobj:DBObject
        val json:String
        lock.read {
            dbobj= DBObject(textElement.uuid, DBObject.TYPE.RECT, DBObject.TEXTURE_TYPE.STRING, Color.RED, textElement.text, 0, listOf(textElement.position))
            json = Gson().toJson(dbobj)
        }
        arFragment.arOperationListener!!.updateDB(textElement.uuid, json, true, {}, {})
    }

    private fun updateDB(imageElement:ImageElement){
        val dbobj:DBObject
        val json:String
        lock.read {
            dbobj= DBObject(imageElement.uuid, DBObject.TYPE.RECT, DBObject.TEXTURE_TYPE.STRING, Color.RED, "", imageElement.resId, listOf(imageElement.position))
            json = Gson().toJson(dbobj)
        }
        arFragment.arOperationListener!!.updateDB(imageElement.uuid, json, true, {}, {})
    }



    fun addElementFromDB(json:String){
        val dbobj = Gson().fromJson(json, DBObject::class.java)
        othersLock.write {
            var stroke = mOthersStrokes.find{stroke ->  stroke.uuid == dbobj.uuid}
            if(stroke == null){
                stroke = Stroke(dbobj.uuid)
            }
            stroke.clearPoints()
            for(f3 in dbobj.locations){
                stroke.add(f3)
            }
            mOthersStrokes.add(stroke)
        }
    }

    fun initialize(){
        lock.write {
            mStrokes.removeAll { true }
            textElements.removeAll { true }
            imageElements.removeAll{true}
        }
    }
}

