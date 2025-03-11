package com.trackasia.navigation.android.navigation.v5.milestone

import com.trackasia.navigation.android.navigation.v5.routeprogress.RouteProgress

fun interface MilestoneEventListener {
    fun onMilestoneEvent(routeProgress: RouteProgress, instruction: String?, milestone: Milestone)
}
