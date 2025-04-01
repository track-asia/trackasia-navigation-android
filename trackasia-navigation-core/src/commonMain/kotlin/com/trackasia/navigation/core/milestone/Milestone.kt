package com.trackasia.navigation.core.milestone

import com.trackasia.navigation.core.instruction.Instruction
import com.trackasia.navigation.core.routeprogress.RouteProgress
import com.trackasia.navigation.core.navigation.TrackAsiaNavigation

/**
 * Base Milestone statement. Subclassed to provide concrete statements.
 *
 * @since 0.4.0
 */
abstract class Milestone(
    val identifier: Int,
    instruction: Instruction? = null,
    val trigger: Trigger.Statement? = null
) {

    private val internalInstruction = instruction

    /**
     * A milestone can either be passed in to the
     * [TrackAsiaNavigation] object
     * (recommended) or validated directly inside your activity.
     *
     * @param previousRouteProgress last locations generated [RouteProgress] object used to
     *  determine certain [TriggerProperty]s
     * @param routeProgress         used to determine certain [TriggerProperty]s
     * @return true if the milestone trigger's valid, else false
     * @since 0.4.0
     */
    abstract fun isOccurring(
        previousRouteProgress: RouteProgress?,
        routeProgress: RouteProgress
    ): Boolean

    open fun getInstruction(): Instruction? {
        return internalInstruction
    }
}
