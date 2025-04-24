package typeotech.binaural.audio

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine
import kotlin.math.PI
import kotlin.math.sin

class AudioEngine {
    private var audioThread: Thread? = null
    private var isRunning = false
    private var leftFrequency = 400.0 // Default left channel frequency
    private var rightFrequency = 400.0 // Default right channel frequency
    private var volume = 0.78 // Default volume (0.0 to 1.0)
    private val sampleRate = 44100f // 44.1 kHz
    private val bufferSize = 1024 // Buffer size for audio playback
    private val format = AudioFormat(sampleRate, 16, 2, true, false) // Stereo, 16-bit, signed PCM
    private var line: SourceDataLine? = null
    private var phaseLeft = 0.0 // Phase for left channel
    private var phaseRight = 0.0 // Phase for right channel

    @Volatile
    private var parameters = AudioParameters(leftFrequency, rightFrequency, volume)

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

    fun updateParameters(leftHz: Double, rightHz: Double, volume: Int) {
        synchronized(this) {
            parameters = AudioParameters(leftHz, rightHz, volume / 100.0)
        }
    }

    private fun runAudioLoop() {
        try {
            line = AudioSystem.getSourceDataLine(format)
            line?.open(format, bufferSize * 2)
            line?.start()

            val buffer = ByteArray(bufferSize * 4) // 4 bytes per stereo sample (2 channels, 2 bytes each)

            while (isRunning) {
                val (leftHz, rightHz, vol) = synchronized(this) { parameters }

                for (i in buffer.indices step 4) {
                    // Calculate samples using phase
                    val leftSample = sin(phaseLeft) * vol
                    val rightSample = sin(phaseRight) * vol

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
        val volume: Double
    )
}