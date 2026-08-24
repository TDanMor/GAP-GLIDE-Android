package com.nbsas.gapglide

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

object GameAudioManager {
    private const val SAMPLE_RATE = 44100

    private var flapTrack: AudioTrack? = null
    private var scoreTrack: AudioTrack? = null
    private var crashTrack: AudioTrack? = null

    init {
        initSounds()
    }

    private fun initSounds() {
        flapTrack = createTrack(generateFlapBuffer())
        scoreTrack = createTrack(generateScoreBuffer())
        crashTrack = createTrack(generateCrashBuffer())
    }

    private fun createTrack(samples: ShortArray): AudioTrack {
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        
        track.write(samples, 0, samples.size)
        return track
    }

    private fun generateFlapBuffer(): ShortArray {
        val durationMs = 100
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val freq = 300.0 + (300.0 * t)
            val volume = 1.0 - t
            val angle = 2.0 * PI * freq * (i.toDouble() / SAMPLE_RATE)
            samples[i] = (sin(angle) * 32767 * volume).toInt().toShort()
        }
        return samples
    }

    private fun generateScoreBuffer(): ShortArray {
        val durationMs = 150
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val switchPoint = (SAMPLE_RATE * 50 / 1000)
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val freq = if (i < switchPoint) 800.0 else 1200.0
            val volume = 0.5 * (1.0 - t)
            val period = SAMPLE_RATE / freq
            val value = if ((i % period) < (period / 2)) 1.0 else -1.0
            samples[i] = (value * 32767 * volume).toInt().toShort()
        }
        return samples
    }

    private fun generateCrashBuffer(): ShortArray {
        val durationMs = 300
        val numSamples = (SAMPLE_RATE * durationMs / 1000)
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / numSamples
            val freq = 150.0 - (110.0 * t)
            val volume = 0.7 * (1.0 - t)
            val period = SAMPLE_RATE / freq
            val value = 2.0 * ((i % period) / period) - 1.0
            samples[i] = (value * 32767 * volume).toInt().toShort()
        }
        return samples
    }

    fun playFlapSound() {
        playSound(flapTrack)
    }

    fun playScoreSound() {
        playSound(scoreTrack)
    }

    fun playCrashSound() {
        playSound(crashTrack)
    }

    private fun playSound(track: AudioTrack?) {
        track?.let {
            it.stop()
            it.reloadStaticData()
            it.play()
        }
    }
    
    fun release() {
        flapTrack?.release()
        scoreTrack?.release()
        crashTrack?.release()
    }
}
