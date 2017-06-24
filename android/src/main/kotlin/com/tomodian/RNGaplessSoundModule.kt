package com.tomodian

import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnErrorListener
import android.net.Uri

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap

import java.io.File
import java.io.IOException
import java.util.HashMap
import android.util.Log

class RNGaplessSoundModule(internal var context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {

    internal var playerPool: MutableMap<Int, MediaPlayer> = HashMap()

    override fun getName(): String {
        return "RNGaplessSound"
    }

    @ReactMethod
    fun prepare(fileName: String, key: Int?, options: ReadableMap, callback: Callback) {
        val player = createMediaPlayer(fileName)
        if (player == null) {
            val e = Arguments.createMap()
            e.putInt("code", -1)
            e.putString("message", "resource not found")
            return
        }

        val module = this

        player.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
            internal var callbackWasCalled = false

            @Synchronized override fun onPrepared(mp: MediaPlayer) {
                if (callbackWasCalled) return
                callbackWasCalled = true

                module.playerPool.put(key, mp)
                val props = Arguments.createMap()
                props.putDouble("duration", mp.duration * .001)
                try {
                    callback.invoke(NULL, props)
                } catch (runtimeException: RuntimeException) {
                    // The callback was already invoked
                    Log.e("RNGaplessSoundModule", "Exception", runtimeException)
                }

            }

        })

        player.setOnErrorListener(object : OnErrorListener {
            internal var callbackWasCalled = false

            @Synchronized override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
                if (callbackWasCalled) return true
                callbackWasCalled = true
                try {
                    val props = Arguments.createMap()
                    props.putInt("what", what)
                    props.putInt("extra", extra)
                    callback.invoke(props, NULL)
                } catch (runtimeException: RuntimeException) {
                    // The callback was already invoked
                    Log.e("RNGaplessSoundModule", "Exception", runtimeException)
                }

                return true
            }
        })

        try {
            player.prepareAsync()
        } catch (ignored: IllegalStateException) {
            // When loading files from a file, we useMediaPlayer.create, which actually
            // prepares the audio for us already. So we catch and ignore this error
        }

    }

    protected fun createMediaPlayer(fileName: String): MediaPlayer? {
        val res = this.context.resources.getIdentifier(fileName, "raw", this.context.packageName)
        if (res != 0) {
            return MediaPlayer.create(this.context, res)
        }
        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            Log.i("RNGaplessSoundModule", fileName)
            try {
                mediaPlayer.setDataSource(fileName)
            } catch (e: IOException) {
                Log.e("RNGaplessSoundModule", "Exception", e)
                return null
            }

            return mediaPlayer
        }

        val file = File(fileName)
        if (file.exists()) {
            val uri = Uri.fromFile(file)
            // Mediaplayer is already prepared here.
            return MediaPlayer.create(this.context, uri)
        }
        return null
    }

    @ReactMethod
    fun play(key: Int?, callback: Callback) {
        val player = this.playerPool[key]
        if (player == null) {
            callback.invoke(false)
            return
        }
        if (player.isPlaying) {
            return
        }
        player.setOnCompletionListener(object : OnCompletionListener {
            internal var callbackWasCalled = false

            @Synchronized override fun onCompletion(mp: MediaPlayer) {
                if (!mp.isLooping) {
                    if (callbackWasCalled) return
                    callbackWasCalled = true
                    try {
                        callback.invoke(true)
                    } catch (e: Exception) {
                        //Catches the exception: java.lang.RuntimeException·Illegal callback invocation from native module
                    }

                }
            }
        })
        player.setOnErrorListener(object : OnErrorListener {
            internal var callbackWasCalled = false

            @Synchronized override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
                if (callbackWasCalled) return true
                callbackWasCalled = true
                callback.invoke(false)
                return true
            }
        })
        player.start()
    }

    @ReactMethod
    fun pause(key: Int?, callback: Callback) {
        val player = this.playerPool[key]
        if (player != null && player.isPlaying) {
            player.pause()
        }
        callback.invoke()
    }

    @ReactMethod
    fun stop(key: Int?, callback: Callback) {
        val player = this.playerPool[key]
        if (player != null && player.isPlaying) {
            player.pause()
            player.seekTo(0)
        }
        callback.invoke()
    }

    @ReactMethod
    fun release(key: Int?) {
        val player = this.playerPool[key]
        if (player != null) {
            player.release()
            this.playerPool.remove(key)
        }
    }

    @ReactMethod
    fun setVolume(key: Int?, left: Float?, right: Float?) {
        val player = this.playerPool[key]
        player?.setVolume(left!!, right!!)
    }

    @ReactMethod
    fun setLooping(key: Int?, looping: Boolean?) {
        val player = this.playerPool[key]
        if (player != null) {
            player.isLooping = looping!!
        }
    }

    @ReactMethod
    fun setSpeed(key: Int?, speed: Float?) {
        val player = this.playerPool[key]
        if (player != null) {
            player.playbackParams = player.playbackParams.setSpeed(speed!!)
        }
    }

    @ReactMethod
    fun setCurrentTime(key: Int?, sec: Float?) {
        val player = this.playerPool[key]
        player?.seekTo(Math.round(sec!! * 1000).toInt())
    }

    @ReactMethod
    fun getCurrentTime(key: Int?, callback: Callback) {
        val player = this.playerPool[key]
        if (player == null) {
            callback.invoke(-1, false)
            return
        }
        callback.invoke(player.currentPosition * .001, player.isPlaying)
    }

    //turn speaker on
    @ReactMethod
    fun setSpeakerphoneOn(key: Int?, speaker: Boolean?) {
        val player = this.playerPool[key]
        if (player != null) {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC)
            val audioManager = this.context.getSystemService(this.context.AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = speaker!!
        }
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