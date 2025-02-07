package de.emaarco.common.test.zeebe

import io.camunda.zeebe.process.test.api.ZeebeTestEngine
import java.time.Duration

object TimerUtils {

    /**
     * Increase time in the engine. This will not take immediate effect, though.
     * There is a real-time delay of a couple of ms until the updated time is picked up by the scheduler
     * This code assumes that the increase of time will trigger timer events.
     * Therefore, we wait until the engine is busy.
     * This means that it started triggering events.
     **/
    fun increaseTime(engine: ZeebeTestEngine, duration: Duration) {
        try {
            engine.increaseTime(duration)
            engine.waitForBusyState(Duration.ofSeconds(5))
            engine.waitForIdleState(Duration.ofSeconds(5))
        } catch (e: Exception) {
            // Do nothing. We've waited up to 1 second for processing to start,
            // if it didn't start in this time the engine probably has not got anything left to process.
        }
    }

}