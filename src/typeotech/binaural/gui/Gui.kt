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
    private val mixSlider = JSlider(JSlider.HORIZONTAL, 0, 100, 0) // 0 = square, 100 = triangle
    private val waveToggle = JToggleButton("Sine Wave")

    private val leftLabel = JLabel("Left HZ")
    private val rightLabel = JLabel("Right HZ")
    private val diffLabel = JLabel("Binaural HZ")
    private val infoLabel = JLabel("Info")
    private val infoScrollLabel = JLabel("More Info")
    private val infoScroll = JScrollPane(infoScrollLabel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
    private val diffSwap = JToggleButton("<-Swap->")
    private val diffMin = -50 * diffMul
    private val diffMax = 50 * diffMul
    private val diffInit = 12 * diffMul
    private val diffHz = JSlider(JSlider.HORIZONTAL, diffMin.toInt(), diffMax.toInt(), diffInit.toInt())
    private val waveTypeCombo = JComboBox(arrayOf("Delta (2 Hz)", "Theta (6 Hz)", "Alpha (10 Hz)", "Beta (20 Hz)", "Gamma (40 Hz)"))

    private val startStop = JToggleButton("Start")
    private val outerGrid = JPanel(GridLayout(9, 1))
    private val innerGrid = JPanel(GridLayout(1, 3))

    private val audioEngine = AudioEngine()
    private val waveInfo = WaveTypes()

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
        outerGrid.add(waveTypeCombo)
        outerGrid.add(waveToggle)
        outerGrid.add(mixSlider)
        outerGrid.add(startStop)
        leftLabel.horizontalAlignment = JLabel.CENTER
        rightLabel.horizontalAlignment = JLabel.CENTER
        diffLabel.horizontalAlignment = JLabel.CENTER
        infoLabel.horizontalAlignment = JLabel.CENTER
        infoScroll.preferredSize = java.awt.Dimension(300, 50)
        mixSlider.toolTipText = "Mix: Square (0) to Triangle (100)"
        volume.toolTipText = "Volume (0-100%)"
        baseHz.toolTipText = "Base frequency (20-300 Hz)"
        diffHz.toolTipText = "Binaural difference (-50 to 50 Hz)"
        waveToggle.toolTipText = "Toggle between Sine and Square/Triangle waves"
        waveTypeCombo.toolTipText = "Select wave type (sets binaural difference)"
        add(baseHz, BorderLayout.EAST)
        add(outerGrid, BorderLayout.CENTER)
        add(volume, BorderLayout.SOUTH)
        volume.addChangeListener(SliderChange())
        baseHz.addChangeListener(SliderChange())
        diffHz.addChangeListener(SliderChange())
        mixSlider.addChangeListener(SliderChange())
        diffSwap.addActionListener(ButtonListener())
        startStop.addActionListener(ButtonListener())
        waveToggle.addActionListener(ButtonListener())
        waveTypeCombo.addActionListener(ButtonListener())
        addWindowListener(ExitListener())
        setSize(400, 350) // Slightly taller for new controls
        setLocationRelativeTo(null)
    }

    data class Vals(val volume: Int, val leftHz: Double, val rightHz: Double, val diffHz: Double, val mixRatio: Int, val useSine: Boolean)

    fun getVals(): Vals {
        val swap = if (diffSwap.isSelected) -1 else 1
        val vol = volume.value
        val diff = diffHz.value.toDouble() / diffMul
        val right = (baseHz.value.toDouble() / hzMul) + (swap * diff / 2)
        val left = (baseHz.value.toDouble() / hzMul) - (swap * diff / 2)
        val mix = mixSlider.value
        val useSine = !waveToggle.isSelected
        return Vals(vol, left, right, diff, mix, useSine)
    }

    fun setLabels() {
        val vals = getVals()
        leftLabel.text = "Left  HZ: ${round(vals.leftHz * 100) / 100}"
        rightLabel.text = "Right HZ: ${round(vals.rightHz * 100) / 100}"
        diffLabel.text = "Binaural HZ: ${vals.diffHz}"
        infoLabel.text = waveInfo.getName(abs(vals.diffHz))
        infoScrollLabel.text = waveInfo.getInfo(abs(vals.diffHz))
        waveToggle.text = if (vals.useSine) "Sine Wave" else "Square/Triangle Wave"
    }

    inner class SliderChange : ChangeListener {
        override fun stateChanged(e: ChangeEvent?) {
            setLabels()
            val vals = getVals()
            println(vals) // Prints Vals(volume=78, leftHz=100.0, ...)
            audioEngine.updateParameters(vals.leftHz, vals.rightHz, vals.volume, vals.mixRatio, vals.useSine)
        }
    }

    inner class ButtonListener : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            when (e?.actionCommand) {
                "Start" -> {
                    (e.source as JToggleButton).text = "Stop"
                    val vals = getVals()
                    audioEngine.updateParameters(vals.leftHz, vals.rightHz, vals.volume, vals.mixRatio, vals.useSine)
                    audioEngine.start()
                }
                "Stop" -> {
                    (e.source as JToggleButton).text = "Start"
                    audioEngine.stop()
                }
                "<-Swap->" -> {
                    setLabels()
                    val vals = getVals()
                    audioEngine.updateParameters(vals.leftHz, vals.rightHz, vals.volume, vals.mixRatio, vals.useSine)
                }
                "Sine Wave" -> {
                    (e.source as JToggleButton).text = "Square/Triangle Wave"
                    setLabels()
                    val vals = getVals()
                    audioEngine.updateParameters(vals.leftHz, vals.rightHz, vals.volume, vals.mixRatio, vals.useSine)
                }
                "Square/Triangle Wave" -> {
                    (e.source as JToggleButton).text = "Sine Wave"
                    setLabels()
                    val vals = getVals()
                    audioEngine.updateParameters(vals.leftHz, vals.rightHz, vals.volume, vals.mixRatio, vals.useSine)
                }
                else -> {
                    // Handle JComboBox wave type selection
                    val selected = waveTypeCombo.selectedItem?.toString() ?: return
                    val diffValue = when (selected) {
                        "Delta (2 Hz)" -> 2.0 * diffMul
                        "Theta (6 Hz)" -> 6.0 * diffMul
                        "Alpha (10 Hz)" -> 10.0 * diffMul
                        "Beta (20 Hz)" -> 20.0 * diffMul
                        "Gamma (40 Hz)" -> 40.0 * diffMul
                        else -> diffHz.value.toDouble()
                    }
                    diffHz.value = diffValue.toInt()
                    setLabels()
                    val vals = getVals()
                    audioEngine.updateParameters(vals.leftHz, vals.rightHz, vals.volume, vals.mixRatio, vals.useSine)
                }
            }
        }
    }

    inner class ExitListener : WindowAdapter() {
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