package typeotech.binaural.info


class WaveTypes{
    /**
     * Delta Waves (0.5-4 Hz): Delta waves are associated with deep sleep and may help promote a state of relaxation and receptivity.
     *
     * Theta Waves (4-8 Hz): Theta waves are associated with deep relaxation, meditation, and enhanced creativity. They may help access the subconscious mind, as they are believed to promote a state of deep relaxation and openness to suggestion.
     *
     * Alpha Waves (8-13 Hz): Alpha waves are associated with a relaxed yet alert state of mind. They may help reduce stress and anxiety and promote a sense of well-being.
     *
     * Beta Waves (13-38 Hz): Beta waves are associated with wakefulness and alertness. They may be useful for improving focus and productivity and may also help to reduce symptoms of ADHD.
     *
     * Gamma Waves (38-100 Hz): Gamma waves are associated with high levels of focus, concentration, and cognitive processing. They may be useful for activities that require a high level of mental processing, such as problem-solving or creative work.
     */
    private val delta="Delta Waves (0.5-4 Hz)"
    private val theta="Theta Waves (4-8 Hz)"
    private val alpha="Alpha Waves (8-13 Hz)"
    private val beta="Beta Waves (13-38 Hz)"
    private val gamma="Gamma Waves (38-100 Hz)"

    private val infoDelta=" Delta waves are associated with deep sleep and may help promote a state of relaxation and receptivity."
    private val infoTheta=" Theta waves are associated with deep relaxation, meditation, and enhanced creativity. They may help access the subconscious mind, as they are believed to promote a state of deep relaxation and openness to suggestion."
    private val infoAlpha=" Alpha waves are associated with a relaxed yet alert state of mind. They may help reduce stress and anxiety and promote a sense of well-being."
    private val infoBeta=" Beta waves are associated with wakefulness and alertness. They may be useful for improving focus and productivity and may also help to reduce symptoms of ADHD."
    private val infoGamma=" Gamma waves are associated with high levels of focus, concentration, and cognitive processing. They may be useful for activities that require a high level of mental processing, such as problem-solving or creative work."

    fun getName(hz:Double): String{
        return when(hz){
            in 0.5 .. 4.0 -> delta
            in 4.0 .. 8.0 -> theta
            in 8.0 .. 13.0 -> alpha
            in 13.0 .. 38.0 -> beta
            in 38.0 .. 100.0 -> gamma
            else-> "?"
        }
    }

    fun getInfo(hz:Double):String{
        return when(hz){
            in 0.5 .. 4.0 -> infoDelta
            in 4.0 .. 8.0 -> infoTheta
            in 8.0 .. 13.0 -> infoAlpha
            in 13.0 .. 38.0 -> infoBeta
            in 38.0 .. 100.0 -> infoGamma
            else-> "?"
        }
    }
}