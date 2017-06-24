package com.tomodian

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnErrorListener
import android.net.Uri
import android.util.Log
import com.facebook.react.bridge.*
import java.io.File
import java.io.IOException
import java.util.*

class RNGaplessSoundModule(internal var context: ReactApplicationContext) : ReactContextBaseJavaModule(context) {

    internal var mainPool: MutableMap<Int, MediaPlayer> = HashMap()
    internal var shadowPool: MutableMap<Int, MediaPlayer> = HashMap()
    internal var currentPool: String = ""

    internal val mainPoolName: String = "main"
    internal val shadowPoolName: String = "shadow"

    override fun getName(): String {
        return "RNGaplessSound"
    }

    @ReactMethod
    fun prepare(fileName: String, key: Int, options: ReadableMap, callback: Callback) {
        currentPool = mainPoolName
        setupPlayer(mainPoolName, fileName, key, options, callback)
        setupPlayer(shadowPoolName, fileName, key, options, callback)
    }

    protected fun setupPlayer(pool: String, fileName: String, key: Int, options: ReadableMap, callback: Callback): MediaPlayer? {
        val player = createMediaPlayer(fileName)

        if (player == null) {
            val e = Arguments.createMap()
            e.putInt("code", -1)

            val msg: String = "resource not found"
            e.putString("message", msg)

            throw RuntimeException(msg)
        }

        val module = this

        player.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
            internal var callbackWasCalled = false

            @Synchronized override fun onPrepared(mp: MediaPlayer) {
                if (callbackWasCalled) {
                    return
                }
                callbackWasCalled = true

                // Append loaded sound resource to the according pool.
                if(pool == mainPoolName) {
                    module.mainPool.put(key, mp)
                } else {
                    module.shadowPool.put(key, mp)
                }

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

        return player
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
    fun play(key: Int, callback: Callback) {

        val player = this.mainPool[key]

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
        val player = this.mainPool[key]
        if (player != null && player.isPlaying) {
            player.pause()
        }
        callback.invoke()
    }

    @ReactMethod
    fun stop(key: Int?, callback: Callback) {
        val player = this.mainPool[key]
        if (player != null && player.isPlaying) {
            player.pause()
            player.seekTo(0)
        }
        callback.invoke()
    }

    @ReactMethod
    fun release(key: Int) {

        val main = this.mainPool[key]
        val shadow = this.shadowPool[key]

        if (main != null) {
            main.release()
            this.mainPool.remove(key)
        }

        if (shadow != null) {
            shadow.release()
            this.shadowPool.remove(key)
        }
    }

    @ReactMethod
    fun setVolume(key: Int, left: Float?, right: Float?) {

        val main = this.mainPool[key]
        main?.setVolume(left!!, right!!)

        val shadow = this.shadowPool[key]
        shadow?.setVolume(left!!, right!!)
    }

    @ReactMethod
    fun setLooping(key: Int?, looping: Boolean?) {

        val main = this.mainPool[key]
        if (main != null) {
            main.isLooping = looping!!
        }

        val shadow = this.shadowPool[key]
        if (shadow != null) {
            shadow.isLooping = looping!!
        }
    }

    @ReactMethod
    fun setSpeed(key: Int?, speed: Float?) {

        val main = this.mainPool[key]
        if (main != null) {
            main.playbackParams = main.playbackParams.setSpeed(speed!!)
        }

        val shadow = this.shadowPool[key]
        if (shadow != null) {
            shadow.playbackParams = shadow.playbackParams.setSpeed(speed!!)
        }
    }

    @ReactMethod
    fun setCurrentTime(key: Int?, sec: Float?) {

        val main = this.mainPool[key]
        main?.seekTo(Math.round(sec!! * 1000).toInt())

        val shadow = this.shadowPool[key]
        shadow?.seekTo(Math.round(sec!! * 1000).toInt())
    }

    @ReactMethod
    fun getCurrentTime(key: Int?, callback: Callback) {

        val main = this.mainPool[key]
        if (main == null) {
            callback.invoke(-1, false)
            return
        }
        callback.invoke(main.currentPosition * .001, main.isPlaying)

        val shadow = this.shadowPool[key]
        if (shadow == null) {
            callback.invoke(-1, false)
            return
        }
        callback.invoke(shadow.currentPosition * .001, shadow.isPlaying)
    }

    //turn speaker on
    @ReactMethod
    fun setSpeakerphoneOn(key: Int?, speaker: Boolean?) {

        val main = this.mainPool[key]
        if (main != null) {
            main.setAudioStreamType(AudioManager.STREAM_MUSIC)
            val audioManager = this.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = speaker!!
        }

        val shadow = this.shadowPool[key]
        if (shadow != null) {
            shadow.setAudioStreamType(AudioManager.STREAM_MUSIC)
            val audioManager = this.context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
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