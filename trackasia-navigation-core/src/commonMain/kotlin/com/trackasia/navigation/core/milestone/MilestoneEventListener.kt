package com.trackasia.navigation.core.milestone

import com.trackasia.navigation.core.routeprogress.RouteProgress

fun interface MilestoneEventListener {
    fun onMilestoneEvent(routeProgress: RouteProgress, instruction: String?, milestone: Milestone)
}
