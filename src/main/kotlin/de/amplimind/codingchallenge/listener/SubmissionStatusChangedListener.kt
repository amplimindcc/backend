package de.amplimind.codingchallenge.listener

import de.amplimind.codingchallenge.events.SubmissionStatusChangedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Customer listener to listen for [SubmissionStatusChangedEvent] events.
 */
@Component
class SubmissionStatusChangedListener {
    private val emitters = CopyOnWriteArraySet<SseEmitter>()

    init {
        initHeartbeat()
    }

    @EventListener
    fun onSubmissionStatusChangedEvent(event: SubmissionStatusChangedEvent) {
        val deadEmitters = mutableSetOf<SseEmitter>()
        for (emitter in emitters) {
            try {
                emitter.send(SseEmitter.event().name("submission-status-changed").data(event.changedSubmission))
            } catch (e: IOException) {
                deadEmitters.add(emitter)
            }
        }
        // Remove dead emitter from the active pool
        emitters.removeAll(deadEmitters)
    }

    /**
     * Run periodic heartbeat to keep the connection alive.
     */
    private fun initHeartbeat() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            val deadEmitters = mutableSetOf<SseEmitter>()
            emitters.forEach {
                try {
                    it.send(SseEmitter.event().name("heartbeat").data("keep-alive"))
                } catch (e: IOException) {
                    deadEmitters.add(it)
                }
            }
            // Remove dead emitters from the active pool
            emitters.removeAll(deadEmitters)
        }, 0, 15, TimeUnit.SECONDS)
    }

    /**
     * Add a new emitter to the list of emitters.
     * @param emitter the emitter to add
     * @return the added emitter
     */
    fun addEmitter(emitter: SseEmitter): SseEmitter {
        emitters.add(emitter)
        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        return emitter
    }
}
