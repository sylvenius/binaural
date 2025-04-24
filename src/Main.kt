import typeotech.binaural.gui.Gui
import java.awt.EventQueue

private fun createAndShowGUI() {
    val frame = Gui()
    frame.isVisible = true
}

fun main() {
    EventQueue.invokeLater(::createAndShowGUI)
}