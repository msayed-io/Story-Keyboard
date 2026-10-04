package com.example.data

import android.media.audiofx.Visualizer
import com.example.ui.components.OrbAudioState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

/**
 * Audio Reactivity Engine.
 * Extracts frequency bands (Bass, Mid, Treble), runs exponential attack/release envelope smoothing,
 * and tracks transient speech onsets for the ThinkingOrb.
 */
class AudioReactivityEngine(private val scope: CoroutineScope) {

    private val _audioState = MutableStateFlow(OrbAudioState())
    val audioState: StateFlow<OrbAudioState> = _audioState.asStateFlow()

    private var visualizer: Visualizer? = null
    private var envelope = 0f
    private var prevEnvelope = 0f
    private var simulationJob: Job? = null

    // Attack / Release smoothing factors
    private val attack = 0.18f
    private val release = 0.055f

    fun attachAudioSession(audioSessionId: Int) {
        release()
        if (audioSessionId != 0) {
            try {
                visualizer = Visualizer(audioSessionId).apply {
                    captureSize = Visualizer.getCaptureSizeRange()[1].coerceAtMost(1024)
                    setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, samplingRate: Int) {}

                        override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                            if (fft == null || fft.isEmpty()) return
                            processFftData(fft)
                        }
                    }, Visualizer.getMaxCaptureRate() / 2, false, true)
                    enabled = true
                }
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Fallback to organic speech envelope synthesis if Visualizer isn't permitted
        startSyntheticSpeechModulation()
    }

    private fun processFftData(fft: ByteArray) {
        val n = fft.size / 2
        val magnitudes = FloatArray(n)
        for (i in 0 until n) {
            val r = fft[2 * i].toFloat()
            val im = fft[2 * i + 1].toFloat()
            magnitudes[i] = (kotlin.math.hypot(r, im) / 128f).coerceIn(0f, 1f)
        }

        val rawBass = averageRange(magnitudes, 0, 8.coerceAtMost(n))
        val rawMid = averageRange(magnitudes, 8.coerceAtMost(n), 42.coerceAtMost(n))
        val rawTreble = averageRange(magnitudes, 42.coerceAtMost(n), 110.coerceAtMost(n))

        val currentRaw = (rawMid * 0.55f + rawBass * 0.35f + rawTreble * 0.10f).coerceIn(0f, 1f)
        updateEnvelopeAndState(currentRaw, rawBass, rawMid, rawTreble)
    }

    private fun averageRange(data: FloatArray, start: Int, end: Int): Float {
        if (start >= end || start >= data.size) return 0f
        val clampedEnd = end.coerceAtMost(data.size)
        var total = 0f
        for (i in start until clampedEnd) {
            total += data[i]
        }
        return total / (clampedEnd - start).coerceAtLeast(1)
    }

    private fun updateEnvelopeAndState(rawVoice: Float, bass: Float, mid: Float, treble: Float) {
        val factor = if (rawVoice > envelope) attack else release
        envelope += (rawVoice - envelope) * factor

        val rise = envelope - prevEnvelope
        prevEnvelope = envelope
        val transient = (rise * 5f).coerceIn(0f, 1f)

        _audioState.value = OrbAudioState(
            envelope = envelope.coerceIn(0f, 1f),
            bass = bass.coerceIn(0f, 1f),
            mid = mid.coerceIn(0f, 1f),
            treble = treble.coerceIn(0f, 1f),
            transient = transient
        )
    }

    fun startSyntheticSpeechModulation() {
        simulationJob?.cancel()
        simulationJob = scope.launch(Dispatchers.Default) {
            var step = 0f
            while (isActive) {
                step += 0.05f
                // Organic speech syllables cadence simulation
                val syllabic = (0.55f * sin(step * 2.8f) + 0.45f * sin(step * 5.1f)).coerceIn(-1f, 1f)
                val rawVoice = if (syllabic > 0.1f) abs(syllabic) * 0.75f else 0.05f
                val rawBass = (rawVoice * 0.8f).coerceIn(0f, 1f)
                val rawMid = (rawVoice * 0.95f).coerceIn(0f, 1f)
                val rawTreble = (rawVoice * 0.6f).coerceIn(0f, 1f)

                updateEnvelopeAndState(rawVoice, rawBass, rawMid, rawTreble)
                delay(20) // ~50fps calculation
            }
        }
    }

    fun stop() {
        simulationJob?.cancel()
        simulationJob = null
        try {
            visualizer?.enabled = false
            visualizer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        visualizer = null
        envelope = 0f
        prevEnvelope = 0f
        _audioState.value = OrbAudioState()
    }

    fun release() {
        stop()
    }
}
