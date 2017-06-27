package com.tomodian.RNGaplessSound

import android.util.Log
import com.facebook.react.bridge.*
import java.util.*

class RNGaplessSoundModule(internal var context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {

    internal var resources: MutableMap<Int, Int> = HashMap()
    internal var pool: MutableMap<Int, LoopMediaPlayer> = HashMap()

    override fun getName(): String {
        return "RNGaplessSound"
    }

    @ReactMethod
    fun prepare(fileName: String, key: Int, options: ReadableMap, callback: Callback) {

        val resourceId = this.context.resources.getIdentifier(fileName, "raw", this.context.packageName)

        if(resourceId == 0) {
            val e = Arguments.createMap()
            e.putInt("code", -1)

            val msg: String = "resource ${fileName} not found"
            e.putString("message", msg)

            throw RuntimeException(msg)
        }

        resources[key] = resourceId

        val props = Arguments.createMap()
        props.putDouble("duration", 0.toDouble())

        callback.invoke(null, props)
    }

    @ReactMethod
    fun play(key: Int, callback: Callback) {
        Log.d("RNGaplessSound", "play called with key ${key}")

        pool[key] = LoopMediaPlayer.create(this.context, resources[key]!!)
    }

    @ReactMethod
    fun pause(key: Int?, callback: Callback) {
        pool[key]?.pause()
        callback.invoke()
    }

    @ReactMethod
    fun stop(key: Int?, callback: Callback) {
        pool[key]?.stop()
        callback.invoke()
    }

    @ReactMethod
    fun release(key: Int) {
        pool[key]?.release()
    }

    @ReactMethod
    fun setVolume(key: Int, left: Float?, right: Float?) {
        pool[key]?.setVolume(left!!, right!!)
    }

    @ReactMethod
    fun setLooping(key: Int?, looping: Boolean?) {
        // no op
    }

    @ReactMethod
    fun setSpeed(key: Int?, speed: Float?) {
        // no op
    }

    @ReactMethod
    fun setCurrentTime(key: Int?, sec: Float?) {
        // no op
    }

    @ReactMethod
    fun getCurrentTime(key: Int?, callback: Callback) {
        // no op
    }

    //turn speaker on
    @ReactMethod
    fun setSpeakerphoneOn(key: Int?, speaker: Boolean?) {
        // no op
    }

    @ReactMethod
    fun enable(enabled: Boolean?) {
        // no op
    }

    override fun getConstants(): Map<String, Any>? {
        val constants = HashMap<String, Any>()
        constants.put("IsAndroid", true)
        return constants
    }

    companion object {
        internal val NULL: Any? = null
    }
}
