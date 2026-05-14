package com.example.saniya

import com.example.saniya.data.BusStatus
import com.example.saniya.data.Route
import com.example.saniya.data.RouteProgressSnapshot
import com.example.saniya.data.Stop
import com.example.saniya.data.buildQuickPingActions
import com.example.saniya.data.etaToDestination
import com.example.saniya.data.progressFraction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteAnalyticsTest {
    private val route = Route(
        id = "route_1",
        institutionId = "campus_1",
        code = "4A",
        name = "River Road Connector",
        summary = "GDC Rural Campus via River Road",
        scheduleLabel = "Departs 7:15 AM",
        durationMinutes = 38,
        seatsAvailable = 12,
        vehicleLabel = "Bus 12",
        stops = listOf(
            Stop("start", "Village Square", 0, 0),
            Stop("bridge", "Old Bridge", 1, 10),
            Stop("market", "Market Cross", 2, 18),
            Stop("gate", "North Gate", 3, 26),
            Stop("campus", "GDC Campus", 4, 38)
        )
    )

    @Test
    fun etaToDestination_includes_delay_minutes() {
        val snapshot = RouteProgressSnapshot(
            currentStopId = "market",
            status = BusStatus.DELAYED,
            delayMinutes = 8
        )

        assertEquals(28, route.etaToDestination(snapshot))
    }

    @Test
    fun progressFraction_tracks_stop_position() {
        val snapshot = RouteProgressSnapshot(currentStopId = "market")

        assertEquals(0.5f, route.progressFraction(snapshot), 0.001f)
    }

    @Test
    fun quickPingActions_use_route_landmarks() {
        val actions = buildQuickPingActions(route)

        assertEquals(4, actions.size)
        assertTrue(actions.first().title.contains("Old Bridge"))
        assertTrue(actions.last().title.contains("Market Cross"))
    }
}
