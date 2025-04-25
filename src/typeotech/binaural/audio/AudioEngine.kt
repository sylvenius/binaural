package typeotech.binaural.audio

import java.util.*
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine
import kotlin.math.PI
import kotlin.math.sin

/****
Square Wave:
Implemented in squareWave(phase): Returns 1.0 if sin(phase) >= 0, else -1.0.

This creates a basic square wave by thresholding the sine wave’s phase.

Note: Pure square waves have strong harmonics, which can sound harsher than sine waves. If this is too sharp, we can smooth it later (e.g., using a Fourier series approximation).

Triangle Wave:
Implemented in triangleWave(phase): Uses linear interpolation to create a triangle wave.

Divides the phase cycle ([0, 2π)) into segments: rising from 0 to 1, falling from 1 to -1, and rising from -1 to 0.

Triangle waves are softer than square waves, with fewer harmonics.

Mixing:
The generateSample function blends square and triangle waves using mixRatio (0.0 = square, 1.0 = triangle).

Formula: square * (1.0 - mixRatio) + triangle * mixRatio.

If useSine is true, it returns sin(phase) instead.

Phase Continuity:
Retained the phase-continuous approach (phaseLeft, phaseRight) to ensure smooth frequency changes.

Wave type or mix changes don’t affect phase, so there’s no crackling.


 */
class AudioEngine {
    private var audioThread: Thread? = null
    private var isRunning = false
    private var leftFrequency = 100.0 // Default left channel frequency
    private var rightFrequency = 106.0 // Default right channel frequency (100 + 12/2)
    private var volume = 0.78 // Default volume (0.0 to 1.0)
    private var mixRatio = 0.0 // 0.0 = square, 1.0 = triangle
    private var useSine = true // True = sine, False = mixed square/triangle
    private val sampleRate = 44100f // 44.1 kHz
    private val bufferSize = 1024 // Buffer size for audio playback
    private val format = AudioFormat(sampleRate, 16, 2, true, false) // Stereo, 16-bit, signed PCM
    private var line: SourceDataLine? = null
    private var phaseLeft = 0.0 // Phase for left channel
    private var phaseRight = 0.0 // Phase for right channel
    private val random = Random()

    @Volatile
    private var parameters = AudioParameters(leftFrequency, rightFrequency, volume, mixRatio, useSine)

    fun start() {
        if (isRunning) return
        isRunning = true
        audioThread = Thread { runAudioLoop() }
        audioThread?.start()
    }

    fun stop() {
        isRunning = false
        audioThread?.join()
        line?.drain()
        line?.close()
        line = null
    }

    fun updateParameters(leftHz: Double, rightHz: Double, volume: Int, mixRatio: Int, useSine: Boolean) {
        synchronized(this) {
            parameters = AudioParameters(leftHz, rightHz, volume / 100.0, mixRatio / 100.0, useSine)
        }
    }

    private fun squareWave(phase: Double): Double {
        // Square wave: 1 if sin(phase) > 0, else -1
        return if (sin(phase) >= 0) 1.0 else -1.0
    }

    private fun triangleWave(phase: Double): Double {
        // Triangle wave: Linear interpolation based on phase
        val p = (phase % (2 * PI)) / (2 * PI) // Normalize to [0, 1)
        return when {
            p < 0.25 -> 4.0 * p // Rising from 0 to 1
            p < 0.75 -> 2.0 - 4.0 * p // Falling from 1 to -1
            else -> -4.0 + 4.0 * p // Rising from -1 to 0
        }
    }

    private fun generateSample(phase: Double, mixRatio: Double, useSine: Boolean): Double {
        return if (useSine) {
            sin(phase)
        } else {
            val square = squareWave(phase)
            val triangle = triangleWave(phase)
            square * (1.0 - mixRatio) + triangle * mixRatio
        }
    }

    private fun runAudioLoop() {
        try {
            line = AudioSystem.getSourceDataLine(format)
            line?.open(format, bufferSize * 2)
            line?.start()

            val buffer = ByteArray(bufferSize * 4) // 4 bytes per stereo sample (2 channels, 2 bytes each)

            while (isRunning) {
                val (leftHz, rightHz, vol, mix, sine) = synchronized(this) { parameters }

                for (i in buffer.indices step 4) {
                    // Generate samples
                    val leftSample = generateSample(phaseLeft, mix, sine) * vol
                    val rightSample = generateSample(phaseRight, mix, sine) * vol

                    // Update phases for the next sample
                    phaseLeft += 2.0 * PI * leftHz / sampleRate
                    phaseRight += 2.0 * PI * rightHz / sampleRate

                    // Wrap phases to prevent numerical overflow
                    if (phaseLeft >= 2.0 * PI) phaseLeft -= 2.0 * PI
                    if (phaseRight >= 2.0 * PI) phaseRight -= 2.0 * PI

                    // Convert to 16-bit PCM
                    val leftValue = (leftSample * Short.MAX_VALUE).toInt()
                    val rightValue = (rightSample * Short.MAX_VALUE).toInt()

                    // Write stereo samples to buffer (little-endian)
                    buffer[i] = (leftValue and 0xFF).toByte()
                    buffer[i + 1] = ((leftValue shr 8) and 0xFF).toByte()
                    buffer[i + 2] = (rightValue and 0xFF).toByte()
                    buffer[i + 3] = ((rightValue shr 8) and 0xFF).toByte()
                }

                line?.write(buffer, 0, buffer.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private data class AudioParameters(
        val leftFrequency: Double,
        val rightFrequency: Double,
        val volume: Double,
        val mixRatio: Double,
        val useSine: Boolean
    )
}