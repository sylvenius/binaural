package typeotech.binaural.gui

import typeotech.binaural.audio.AudioEngine
import typeotech.binaural.info.WaveTypes
import java.awt.BorderLayout
import java.awt.EventQueue
import java.awt.GridLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import kotlin.math.abs
import kotlin.math.round


class Gui : JFrame() {
    private var hzMul = 10.0
    private val diffMul = 10.0
    private val hzMin = 20 * hzMul
    private val hzMax = 300 * hzMul
    private val hzInit = 100 * hzMul
    private val volume = JSlider(JSlider.HORIZONTAL, 0, 100, 78)
    private val baseHz = JSlider(JSlider.VERTICAL, hzMin.toInt(), hzMax.toInt(), hzInit.toInt())

    private val leftLabel = JLabel("Left HZ")
    private val rightLabel = JLabel("Right HZ")
    private val diffLabel = JLabel("Binaural HZ")
    private val infoLabel = JLabel("Info")
    private val infoScrollLabel = JLabel("More Info")
    private var infoScroll = JScrollPane(infoScrollLabel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
    private val diffSwap = JToggleButton("<-Swap->")
    private val diffMin = -50 * diffMul
    private val diffMax = 50 * diffMul
    private val diffInit = 12 * diffMul
    private val diffHz = JSlider(JSlider.HORIZONTAL, diffMin.toInt(), diffMax.toInt(), diffInit.toInt())

    private val startStop = JToggleButton("Start")
    private val outerGrid = JPanel(GridLayout(7, 1))
    private val innerGrid = JPanel(GridLayout(1, 3))

    private val audioEngine = AudioEngine()
    private val waveInfo= WaveTypes()

    init {
        createUI()
        setLabels()
    }

    private fun createUI() {
        title = "Binaural"
        innerGrid.add(leftLabel)
        innerGrid.add(diffSwap)
        innerGrid.add(rightLabel)
        outerGrid.add(innerGrid)
        outerGrid.add(infoLabel)
        outerGrid.add(infoScroll)
        outerGrid.add(diffLabel)
        outerGrid.add(diffHz)
        outerGrid.add(startStop)
        leftLabel.horizontalAlignment = JLabel.CENTER
        rightLabel.horizontalAlignment = JLabel.CENTER
        diffLabel.horizontalAlignment = JLabel.CENTER
        infoLabel.horizontalAlignment = JLabel.CENTER
        add(baseHz, BorderLayout.EAST)
        add(outerGrid, BorderLayout.CENTER)
        add(volume, BorderLayout.SOUTH)
        volume.addChangeListener(SliderChange())
        baseHz.addChangeListener(SliderChange())
        diffHz.addChangeListener(SliderChange())
        diffSwap.addActionListener(ButtonListener())
        startStop.addActionListener(ButtonListener())
        addWindowListener(ExitListener())
        setSize(400, 300)
        setLocationRelativeTo(null)
    }

    class Vals(val volume: Int, val leftHz: Double, val rightHz: Double, val diffHz: Double)

    fun getVals(): Vals {
        val swap = if (diffSwap.isSelected) -1 else 1
        val vol = volume.value
        val diff = diffHz.value.toDouble() / diffMul
        val right = (baseHz.value.toDouble() / hzMul) + (swap * diff / 2)
        val left = (baseHz.value.toDouble() / hzMul) - (swap * diff / 2)
        return Vals(vol, left, right, diff)
    }

    fun setLabels() {
        val vals = getVals()
        leftLabel.text = "Left  HZ: ${round(vals.leftHz * 100) / 100}"
        rightLabel.text = "Right HZ: ${round(vals.rightHz * 100) / 100}"
        diffLabel.text = "Binaural HZ: ${vals.diffHz}"
        infoLabel.text=waveInfo.getName(abs(vals.diffHz))
        infoScrollLabel.text=waveInfo.getInfo(abs(vals.diffHz))
    }

    inner class SliderChange : ChangeListener {
        override fun stateChanged(e: ChangeEvent?) {
            setLabels()
            val vals = getVals()
            audioEngine.updateParameters(vals.leftHz, vals.rightHz, vals.volume)
        }
    }

    inner class ButtonListener : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            when (e?.actionCommand) {
                "Start" -> {
                    (e.source as JToggleButton).text = "Stop"
                    val vals = getVals()
                    audioEngine.updateParameters(vals.leftHz, vals.rightHz, vals.volume)
                    audioEngine.start()
                }
                "Stop" -> {
                    (e.source as JToggleButton).text = "Start"
                    audioEngine.stop()
                }
                "<-Swap->" -> {
                    setLabels()
                    val vals = getVals()
                    audioEngine.updateParameters(vals.leftHz, vals.rightHz, vals.volume)
                }
            }
        }
    }

    inner class ExitListener: WindowAdapter() {
        override fun windowClosing(e: WindowEvent) {
            audioEngine.stop()
            e.window.dispose()
        }
    }
}

private fun createAndShowGUI() {
    val frame = Gui()
    frame.isVisible = true
}

fun main() {
    EventQueue.invokeLater(::createAndShowGUI)
}