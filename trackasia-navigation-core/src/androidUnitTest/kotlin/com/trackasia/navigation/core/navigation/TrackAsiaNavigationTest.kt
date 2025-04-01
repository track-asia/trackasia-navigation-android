package com.trackasia.navigation.core.navigation

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import com.trackasia.navigation.core.BaseTest
import com.trackasia.navigation.core.location.engine.LocationEngine
import com.trackasia.navigation.core.milestone.BannerInstructionMilestone
import com.trackasia.navigation.core.milestone.Milestone
import com.trackasia.navigation.core.milestone.StepMilestone
import com.trackasia.navigation.core.milestone.VoiceInstructionMilestone
import com.trackasia.navigation.core.offroute.OffRoute
import com.trackasia.navigation.core.snap.Snap
import com.trackasia.navigation.core.snap.SnapToRoute
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class TrackAsiaNavigationTest : BaseTest() {

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sanityTest() {
        val navigation = buildTrackAsiaNavigation()

        assertNotNull(navigation)
    }

    @Test
    fun sanityTestWithOptions() {
        val options = TrackAsiaNavigationOptions()
        val navigationWithOptions = buildTrackAsiaNavigationWithOptions(options)

        assertNotNull(navigationWithOptions)
    }

    @Test
    @Throws(Exception::class)
    fun voiceInstructionMilestone_onInitializationDoesGetAdded() {
        val navigation = buildTrackAsiaNavigation()

        val identifier = searchForVoiceInstructionMilestone(navigation)

        assertEquals(NavigationConstants.VOICE_INSTRUCTION_MILESTONE_ID, identifier)
    }

    @Test
    @Throws(Exception::class)
    fun bannerInstructionMilestone_onInitializationDoesGetAdded() {
        val navigation = buildTrackAsiaNavigation()

        val identifier = searchForBannerInstructionMilestone(navigation)

        assertEquals(NavigationConstants.BANNER_INSTRUCTION_MILESTONE_ID, identifier)
    }

    @Test
    @Throws(Exception::class)
    fun defaultMilestones_onInitializationDoNotGetAdded() {
        val options = TrackAsiaNavigationOptions(
            defaultMilestonesEnabled = false
        )
        val navigationWithOptions = buildTrackAsiaNavigationWithOptions(options)

        assertEquals(0, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun defaultEngines_offRouteEngineDidGetInitialized() {
        val navigation = buildTrackAsiaNavigation()

        assertNotNull(navigation.offRouteEngine)
    }

    @Test
    @Throws(Exception::class)
    fun defaultEngines_snapEngineDidGetInitialized() {
        val navigation = buildTrackAsiaNavigation()

        assertNotNull(navigation.snapEngine)
    }

    @Test
    @Throws(Exception::class)
    fun addMilestone_milestoneDidGetAdded() {
        val navigation = buildTrackAsiaNavigation()
        val milestone: Milestone = StepMilestone(identifier = 42)

        navigation.addMilestone(milestone)

        assertTrue(navigation.milestones.contains(milestone))
    }

    @Test
    @Throws(Exception::class)
    fun addMilestone_milestoneOnlyGetsAddedOnce() {
        val options = TrackAsiaNavigationOptions(
            defaultMilestonesEnabled = false
        )

        val navigationWithOptions = buildTrackAsiaNavigationWithOptions(options)
        val milestone: Milestone = StepMilestone(identifier = 42)
        navigationWithOptions.addMilestone(milestone)
        navigationWithOptions.addMilestone(milestone)

        assertEquals(1, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun removeMilestone_milestoneDidGetRemoved() {
        val options = TrackAsiaNavigationOptions(
            defaultMilestonesEnabled = false
        )
        val navigationWithOptions = buildTrackAsiaNavigationWithOptions(options)

        val milestone: Milestone = StepMilestone(identifier = 1)
        navigationWithOptions.addMilestone(milestone)
        navigationWithOptions.removeMilestone(milestone)

        assertEquals(0, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun removeMilestone_milestoneDoesNotExist() {
        val options = TrackAsiaNavigationOptions(
            defaultMilestonesEnabled = false
        )

        val navigationWithOptions = buildTrackAsiaNavigationWithOptions(options)
        val milestone: Milestone = StepMilestone(identifier = 1)
        navigationWithOptions.addMilestone(StepMilestone(identifier = 2))
        navigationWithOptions.removeMilestone(milestone)

        assertEquals(1, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun removeMilestone_nullRemovesAllMilestones() {
        val options = TrackAsiaNavigationOptions(
            defaultMilestonesEnabled = false
        )

        val navigationWithOptions = buildTrackAsiaNavigationWithOptions(options)
        navigationWithOptions.addMilestone(StepMilestone(identifier = 1))
        navigationWithOptions.addMilestone(StepMilestone(identifier = 2))
        navigationWithOptions.addMilestone(StepMilestone(identifier = 3))
        navigationWithOptions.addMilestone(StepMilestone(identifier = 4))

        navigationWithOptions.removeMilestone(null)

        assertEquals(0, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun removeMilestone_correctMilestoneWithIdentifierGetsRemoved() {
        val options = TrackAsiaNavigationOptions(
            defaultMilestonesEnabled = false
        )

        val navigationWithOptions = buildTrackAsiaNavigationWithOptions(options)
        val removedMilestoneIdentifier = 5678
        val milestone = StepMilestone(identifier = removedMilestoneIdentifier)
        navigationWithOptions.addMilestone(milestone)

        navigationWithOptions.removeMilestone(removedMilestoneIdentifier)

        assertEquals(0, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun removeMilestone_noMilestoneWithIdentifierFound() {
        val options = TrackAsiaNavigationOptions(
            defaultMilestonesEnabled = false
        )

        val navigationWithOptions = buildTrackAsiaNavigationWithOptions(options)
        navigationWithOptions.addMilestone(StepMilestone(identifier = 1234))
        val removedMilestoneIdentifier = 5678

        navigationWithOptions.removeMilestone(removedMilestoneIdentifier)

        assertEquals(1, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun addMilestoneList_duplicateIdentifiersAreIgnored() {
        val options = TrackAsiaNavigationOptions(
            defaultMilestonesEnabled = false
        )

        val navigationWithOptions = buildTrackAsiaNavigationWithOptions(options)
        val milestoneIdentifier = 5678
        val milestone = StepMilestone(identifier = milestoneIdentifier)
        navigationWithOptions.addMilestone(milestone)
        val milestones: MutableList<Milestone> = java.util.ArrayList()
        milestones.add(milestone)
        milestones.add(milestone)
        milestones.add(milestone)

        navigationWithOptions.addMilestones(milestones)

        assertEquals(1, navigationWithOptions.milestones.size)
    }

    @Test
    @Throws(Exception::class)
    fun addMilestoneList_allMilestonesAreAdded() {
        val options = TrackAsiaNavigationOptions(
            defaultMilestonesEnabled = false
        )

        val navigationWithOptions = buildTrackAsiaNavigationWithOptions(options)
        val firstMilestoneId = 5678
        val secondMilestoneId = 5679
        val firstMilestone = StepMilestone(identifier = firstMilestoneId)
        val secondMilestone = StepMilestone(identifier = secondMilestoneId)
        val milestones: MutableList<Milestone> = ArrayList()
        milestones.add(firstMilestone)
        milestones.add(secondMilestone)

        navigationWithOptions.addMilestones(milestones)

        assertEquals(2, navigationWithOptions.milestones.size)
    }

    @Test
    fun locationEngine_returnsCorrectLocationEngine() {
        val navigation = buildTrackAsiaNavigation()
        val locationEngine = mockk<LocationEngine>()
        val locationEngineInstanceNotUsed = mockk<LocationEngine>()

        navigation.locationEngine = locationEngine

        assertNotSame(locationEngineInstanceNotUsed, navigation.locationEngine)
        assertEquals(locationEngine, navigation.locationEngine)
    }

    @Test
    @Throws(Exception::class)
    fun startNavigation_doesSendTrueToNavigationEvent() {
        val navigation = buildTrackAsiaNavigation()
        val navigationEventListener = mockk<NavigationEventListener>(relaxed = true)

        navigation.addNavigationEventListener(navigationEventListener)
        navigation.startNavigation(buildTestDirectionsRoute())

        verify {
            navigationEventListener.onRunning(true)
        }
    }

    @Test
    @Throws(Exception::class)
    fun setSnapEngine_doesReplaceDefaultEngine() {
        val navigation = buildTrackAsiaNavigation()

        val snap = mockk<Snap>()
        navigation.snapEngine = snap

        assertTrue(navigation.snapEngine !is SnapToRoute)
    }

    @Test
    @Throws(Exception::class)
    fun setOffRouteEngine_doesReplaceDefaultEngine() {
        val navigation = buildTrackAsiaNavigation()

        val offRoute = mockk<OffRoute>()
        navigation.offRouteEngine = offRoute

        assertEquals(offRoute, navigation.offRouteEngine)
    }

    private fun buildTrackAsiaNavigation(): TrackAsiaNavigation {
        return TrackAsiaNavigation(locationEngine = mockk(relaxed = true))
    }

    private fun buildTrackAsiaNavigationWithOptions(options: TrackAsiaNavigationOptions): TrackAsiaNavigation {
        return TrackAsiaNavigation(options = options, locationEngine = mockk())
    }

    private fun searchForVoiceInstructionMilestone(navigation: TrackAsiaNavigation): Int {
        var identifier = -1
        for (milestone in navigation.milestones) {
            if (milestone is VoiceInstructionMilestone) {
                identifier = milestone.identifier
            }
        }
        return identifier
    }

    private fun searchForBannerInstructionMilestone(navigation: TrackAsiaNavigation): Int {
        var identifier = -1
        for (milestone in navigation.milestones) {
            if (milestone is BannerInstructionMilestone) {
                identifier = milestone.identifier
            }
        }
        return identifier
    }
}