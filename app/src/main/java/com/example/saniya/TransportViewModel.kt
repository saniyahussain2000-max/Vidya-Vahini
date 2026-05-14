package com.example.saniya

import androidx.lifecycle.ViewModel
import com.example.saniya.data.ActivityItem
import com.example.saniya.data.ActivityType
import com.example.saniya.data.Announcement
import com.example.saniya.data.AnnouncementSeverity
import com.example.saniya.data.BusStatus
import com.example.saniya.data.EmergencyContact
import com.example.saniya.data.Institution
import com.example.saniya.data.PingSource
import com.example.saniya.data.Route
import com.example.saniya.data.RoutePing
import com.example.saniya.data.RouteProgressSnapshot
import com.example.saniya.data.Stop
import com.example.saniya.data.buildQuickPingActions
import com.example.saniya.data.stopIndex
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class TransportUiState(
    val institutions: List<Institution>,
    val routes: List<Route>,
    val selectedInstitutionId: String? = null,
    val routeQuery: String = "",
    val activeRouteId: String,
    val plannerSelectionId: String,
    val routeSnapshots: Map<String, RouteProgressSnapshot>,
    val routePingHistory: Map<String, List<RoutePing>>,
    val announcements: List<Announcement>,
    val contacts: List<EmergencyContact>,
    val activity: List<ActivityItem>,
    val lastSafeReachTimestamp: Long? = null
) {
    val activeRoute: Route
        get() = routes.first { it.id == activeRouteId }

    val plannerSelection: Route
        get() = routes.first { it.id == plannerSelectionId }

    val filteredRoutes: List<Route>
        get() {
            val query = routeQuery.trim()
            return routes.filter { route ->
                val institution = institutions.firstOrNull { it.id == route.institutionId }
                val matchesInstitution = selectedInstitutionId == null || route.institutionId == selectedInstitutionId
                val matchesQuery = query.isBlank() || listOf(
                    route.code,
                    route.name,
                    route.summary,
                    institution?.name.orEmpty()
                ).any { it.contains(query, ignoreCase = true) }
                matchesInstitution && matchesQuery
            }
        }

    val enabledContactsCount: Int
        get() = contacts.count { it.isEnabled }

    fun institutionNameFor(route: Route): String {
        return institutions.firstOrNull { it.id == route.institutionId }?.name.orEmpty()
    }

    fun snapshotFor(routeId: String): RouteProgressSnapshot {
        return routeSnapshots.getValue(routeId)
    }

    fun pingHistoryFor(routeId: String): List<RoutePing> {
        return routePingHistory[routeId].orEmpty()
    }
}

class TransportViewModel : ViewModel() {
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages = _messages.asSharedFlow()

    private val _uiState = MutableStateFlow(seedState())
    val uiState: StateFlow<TransportUiState> = _uiState

    fun selectInstitution(institutionId: String?) {
        _uiState.update { state ->
            state.copy(
                selectedInstitutionId = if (state.selectedInstitutionId == institutionId) null else institutionId
            )
        }
    }

    fun updateRouteQuery(query: String) {
        _uiState.update { it.copy(routeQuery = query) }
    }

    fun choosePlannerRoute(routeId: String) {
        _uiState.update { state ->
            state.copy(
                plannerSelectionId = routeId,
                selectedInstitutionId = state.routes.firstOrNull { it.id == routeId }?.institutionId
                    ?: state.selectedInstitutionId
            )
        }
    }

    fun clearRouteFilters() {
        _uiState.update { it.copy(selectedInstitutionId = null, routeQuery = "") }
    }

    fun confirmPlannerSelection() {
        val state = _uiState.value
        val selectedRoute = state.plannerSelection
        val changedRoute = selectedRoute.id != state.activeRouteId

        _uiState.update {
            it.copy(activeRouteId = selectedRoute.id)
        }

        if (changedRoute) {
            addActivity(
                title = "Switched to Route ${selectedRoute.code}",
                detail = "${selectedRoute.name} is now the live commute.",
                type = ActivityType.ROUTE
            )
            _messages.tryEmit("Now tracking Route ${selectedRoute.code}.")
        } else {
            _messages.tryEmit("Route ${selectedRoute.code} is ready to track.")
        }
    }

    fun sendQuickPing(actionId: String) {
        val state = _uiState.value
        val route = state.activeRoute
        val action = buildQuickPingActions(route).firstOrNull { it.id == actionId } ?: return
        val now = System.currentTimeMillis()

        val updatedSnapshot = RouteProgressSnapshot(
            currentStopId = action.stopId,
            status = action.status,
            delayMinutes = action.delayMinutes,
            updatedAt = now
        )

        val ping = RoutePing(
            id = UUID.randomUUID().toString(),
            routeId = route.id,
            stopId = action.stopId,
            timestamp = now,
            status = action.status,
            author = "You",
            source = PingSource.COMMUNITY,
            message = action.message
        )

        _uiState.update { current ->
            current.copy(
                routeSnapshots = current.routeSnapshots + (route.id to updatedSnapshot),
                routePingHistory = current.routePingHistory + (
                    route.id to listOf(ping).plus(current.pingHistoryFor(route.id)).take(6)
                )
            )
        }

        addActivity(
            title = "Shared a route ping",
            detail = action.title,
            type = ActivityType.PING,
            timestamp = now
        )
        _messages.tryEmit(action.title)
    }

    fun reportBreakdown() {
        val state = _uiState.value
        val route = state.activeRoute
        val currentSnapshot = state.snapshotFor(route.id)
        val currentStop = route.stops.firstOrNull { it.id == currentSnapshot.currentStopId } ?: route.stops.first()
        val now = System.currentTimeMillis()

        val breakdownSnapshot = currentSnapshot.copy(
            status = BusStatus.BREAKDOWN,
            delayMinutes = 25,
            updatedAt = now
        )

        val ping = RoutePing(
            id = UUID.randomUUID().toString(),
            routeId = route.id,
            stopId = currentStop.id,
            timestamp = now,
            status = BusStatus.BREAKDOWN,
            author = "You",
            source = PingSource.COMMUNITY,
            message = "Breakdown reported near ${currentStop.name}."
        )

        val announcement = Announcement(
            id = UUID.randomUUID().toString(),
            title = "Route ${route.code} support dispatched",
            message = "The transport desk has been alerted near ${currentStop.name}.",
            timestamp = now,
            severity = AnnouncementSeverity.WARNING
        )

        _uiState.update { current ->
            current.copy(
                routeSnapshots = current.routeSnapshots + (route.id to breakdownSnapshot),
                routePingHistory = current.routePingHistory + (
                    route.id to listOf(ping).plus(current.pingHistoryFor(route.id)).take(6)
                ),
                announcements = listOf(announcement).plus(current.announcements).take(6)
            )
        }

        addActivity(
            title = "Breakdown reported",
            detail = "Support has been alerted for Route ${route.code}.",
            type = ActivityType.ALERT,
            timestamp = now
        )
        _messages.tryEmit("Breakdown alert sent to the route team.")
    }

    fun notifySafeArrival() {
        if (_uiState.value.enabledContactsCount == 0) {
            _messages.tryEmit("Enable at least one trusted contact first.")
            return
        }

        val now = System.currentTimeMillis()
        val route = _uiState.value.activeRoute
        val announcement = Announcement(
            id = UUID.randomUUID().toString(),
            title = "Safe arrival shared",
            message = "Trusted contacts were notified that Route ${route.code} reached safely.",
            timestamp = now,
            severity = AnnouncementSeverity.SUCCESS
        )

        _uiState.update { current ->
            current.copy(
                lastSafeReachTimestamp = now,
                announcements = listOf(announcement).plus(current.announcements).take(6)
            )
        }

        addActivity(
            title = "Safe arrival sent",
            detail = "${_uiState.value.enabledContactsCount} trusted contacts were notified.",
            type = ActivityType.SAFETY,
            timestamp = now
        )
        _messages.tryEmit("Safe arrival sent to trusted contacts.")
    }

    fun toggleContactEnabled(contactId: String) {
        var enabled = false
        var name = ""
        _uiState.update { current ->
            val updatedContacts = current.contacts.map { contact ->
                if (contact.id == contactId) {
                    enabled = !contact.isEnabled
                    name = contact.name
                    contact.copy(isEnabled = enabled)
                } else {
                    contact
                }
            }
            current.copy(contacts = updatedContacts)
        }
        if (name.isNotBlank()) {
            _messages.tryEmit(
                if (enabled) "$name will receive safety updates." else "$name has been muted for safety updates."
            )
        }
    }

    fun callContact(contactId: String) {
        val contact = _uiState.value.contacts.firstOrNull { it.id == contactId } ?: return
        addActivity(
            title = "Opened contact card",
            detail = "${contact.name} is ready to call.",
            type = ActivityType.SAFETY
        )
        _messages.tryEmit("${contact.name}: ${contact.phone}")
    }

    fun refreshDashboard() {
        val state = _uiState.value
        val route = state.activeRoute
        val snapshot = state.snapshotFor(route.id)
        val now = System.currentTimeMillis()
        val currentIndex = route.stopIndex(snapshot.currentStopId)
        val nextIndex = if (snapshot.status == BusStatus.BREAKDOWN) {
            currentIndex
        } else {
            (currentIndex + 1).coerceAtMost(route.stops.lastIndex)
        }
        val nextStop = route.stops[nextIndex]
        val resolvedStatus = when {
            snapshot.status == BusStatus.BREAKDOWN -> BusStatus.BREAKDOWN
            snapshot.delayMinutes > 2 -> BusStatus.DELAYED
            else -> BusStatus.NORMAL
        }
        val updatedSnapshot = snapshot.copy(
            currentStopId = nextStop.id,
            status = resolvedStatus,
            delayMinutes = (snapshot.delayMinutes - 2).coerceAtLeast(0),
            updatedAt = now
        )
        val refreshMessage = when {
            snapshot.status == BusStatus.BREAKDOWN ->
                "Support update: bus still paused near ${nextStop.name}."
            nextIndex > currentIndex ->
                "System update: bus is now near ${nextStop.name}."
            else ->
                "Tracker refreshed at ${nextStop.name}."
        }
        val systemPing = RoutePing(
            id = UUID.randomUUID().toString(),
            routeId = route.id,
            stopId = nextStop.id,
            timestamp = now,
            status = updatedSnapshot.status,
            author = "System",
            source = PingSource.SYSTEM,
            message = refreshMessage.removePrefix("System update: ")
                .removePrefix("Support update: ")
        )

        _uiState.update { current ->
            current.copy(
                routeSnapshots = current.routeSnapshots + (route.id to updatedSnapshot),
                routePingHistory = current.routePingHistory + (
                    route.id to listOf(systemPing).plus(current.pingHistoryFor(route.id)).take(6)
                )
            )
        }

        addActivity(
            title = "Tracker refreshed",
            detail = refreshMessage,
            type = ActivityType.ROUTE,
            timestamp = now
        )
        _messages.tryEmit(refreshMessage)
    }

    private fun addActivity(
        title: String,
        detail: String,
        type: ActivityType,
        timestamp: Long = System.currentTimeMillis()
    ) {
        val item = ActivityItem(
            id = UUID.randomUUID().toString(),
            title = title,
            detail = detail,
            timestamp = timestamp,
            type = type
        )

        _uiState.update { current ->
            current.copy(activity = listOf(item).plus(current.activity).take(8))
        }
    }

    private fun seedState(): TransportUiState {
        val now = System.currentTimeMillis()
        val minute = 60_000L
        val institutions = listOf(
            Institution("gdc", "GDC Rural Campus", "River Road"),
            Institution("vidya", "Vidya Junior College", "Market Square"),
            Institution("sunrise", "Sunrise Public School", "East Valley")
        )
        val routes = listOf(
            Route(
                id = "r1",
                institutionId = "gdc",
                code = "4A",
                name = "River Road Connector",
                summary = "GDC Rural Campus via River Road",
                scheduleLabel = "Departs 7:15 AM",
                durationMinutes = 38,
                seatsAvailable = 12,
                vehicleLabel = "Bus 12",
                stops = listOf(
                    Stop("r1_start", "Village Square", 0, 0),
                    Stop("r1_bridge", "Old Bridge", 1, 10),
                    Stop("r1_market", "Market Cross", 2, 18),
                    Stop("r1_gate", "North Gate", 3, 26),
                    Stop("r1_campus", "GDC Campus", 4, 38)
                )
            ),
            Route(
                id = "r2",
                institutionId = "gdc",
                code = "2B",
                name = "Lake View Express",
                summary = "GDC Rural Campus via Lake Road",
                scheduleLabel = "Departs 7:30 AM",
                durationMinutes = 42,
                seatsAvailable = 7,
                vehicleLabel = "Mini Bus 3",
                stops = listOf(
                    Stop("r2_start", "Bus Stand", 0, 0),
                    Stop("r2_park", "City Park", 1, 11),
                    Stop("r2_lake", "Lake View", 2, 21),
                    Stop("r2_hostel", "Girls Hostel", 3, 31),
                    Stop("r2_campus", "GDC Campus", 4, 42)
                )
            ),
            Route(
                id = "r3",
                institutionId = "vidya",
                code = "B1",
                name = "Town Loop Scholar Line",
                summary = "Vidya Junior College via Town Loop",
                scheduleLabel = "Departs 7:05 AM",
                durationMinutes = 35,
                seatsAvailable = 18,
                vehicleLabel = "Bus 6",
                stops = listOf(
                    Stop("r3_start", "Clock Tower", 0, 0),
                    Stop("r3_clinic", "Civic Clinic", 1, 8),
                    Stop("r3_square", "Town Square", 2, 16),
                    Stop("r3_library", "Central Library", 3, 24),
                    Stop("r3_campus", "Vidya Campus", 4, 35)
                )
            ),
            Route(
                id = "r4",
                institutionId = "sunrise",
                code = "S7",
                name = "Sunrise Ridge Shuttle",
                summary = "Sunrise Public School via East Valley",
                scheduleLabel = "Departs 7:40 AM",
                durationMinutes = 33,
                seatsAvailable = 9,
                vehicleLabel = "Van 2",
                stops = listOf(
                    Stop("r4_start", "Temple Road", 0, 0),
                    Stop("r4_valley", "East Valley", 1, 9),
                    Stop("r4_garden", "Rose Garden", 2, 18),
                    Stop("r4_gate", "Sunrise Gate", 3, 26),
                    Stop("r4_campus", "School Block", 4, 33)
                )
            )
        )

        return TransportUiState(
            institutions = institutions,
            routes = routes,
            activeRouteId = "r1",
            plannerSelectionId = "r1",
            routeSnapshots = mapOf(
                "r1" to RouteProgressSnapshot("r1_market", BusStatus.NORMAL, 0, now - (2 * minute)),
                "r2" to RouteProgressSnapshot("r2_lake", BusStatus.DELAYED, 6, now - (5 * minute)),
                "r3" to RouteProgressSnapshot("r3_square", BusStatus.NORMAL, 0, now - (4 * minute)),
                "r4" to RouteProgressSnapshot("r4_valley", BusStatus.NORMAL, 0, now - (6 * minute))
            ),
            routePingHistory = mapOf(
                "r1" to listOf(
                    RoutePing(
                        id = "ping_1",
                        routeId = "r1",
                        stopId = "r1_market",
                        timestamp = now - (2 * minute),
                        status = BusStatus.NORMAL,
                        author = "Ananya",
                        source = PingSource.COMMUNITY,
                        message = "Bus crossed Market Cross."
                    ),
                    RoutePing(
                        id = "ping_2",
                        routeId = "r1",
                        stopId = "r1_bridge",
                        timestamp = now - (15 * minute),
                        status = BusStatus.NORMAL,
                        author = "System",
                        source = PingSource.SYSTEM,
                        message = "Bus departed Old Bridge."
                    ),
                    RoutePing(
                        id = "ping_3",
                        routeId = "r1",
                        stopId = "r1_market",
                        timestamp = now - (30 * minute),
                        status = BusStatus.DELAYED,
                        author = "Rahul",
                        source = PingSource.COMMUNITY,
                        message = "Traffic easing near the market."
                    )
                ),
                "r2" to listOf(
                    RoutePing(
                        id = "ping_4",
                        routeId = "r2",
                        stopId = "r2_lake",
                        timestamp = now - (5 * minute),
                        status = BusStatus.DELAYED,
                        author = "System",
                        source = PingSource.SYSTEM,
                        message = "Bus slowed near Lake View."
                    ),
                    RoutePing(
                        id = "ping_5",
                        routeId = "r2",
                        stopId = "r2_park",
                        timestamp = now - (16 * minute),
                        status = BusStatus.NORMAL,
                        author = "Meera",
                        source = PingSource.COMMUNITY,
                        message = "Plenty of seats from City Park."
                    )
                ),
                "r3" to listOf(
                    RoutePing(
                        id = "ping_6",
                        routeId = "r3",
                        stopId = "r3_square",
                        timestamp = now - (4 * minute),
                        status = BusStatus.NORMAL,
                        author = "System",
                        source = PingSource.SYSTEM,
                        message = "Bus crossed Town Square."
                    ),
                    RoutePing(
                        id = "ping_7",
                        routeId = "r3",
                        stopId = "r3_clinic",
                        timestamp = now - (12 * minute),
                        status = BusStatus.NORMAL,
                        author = "Riya",
                        source = PingSource.COMMUNITY,
                        message = "Reached Civic Clinic on time."
                    )
                ),
                "r4" to listOf(
                    RoutePing(
                        id = "ping_8",
                        routeId = "r4",
                        stopId = "r4_valley",
                        timestamp = now - (6 * minute),
                        status = BusStatus.NORMAL,
                        author = "System",
                        source = PingSource.SYSTEM,
                        message = "Van entered East Valley."
                    ),
                    RoutePing(
                        id = "ping_9",
                        routeId = "r4",
                        stopId = "r4_garden",
                        timestamp = now - (18 * minute),
                        status = BusStatus.NORMAL,
                        author = "Transport Lead",
                        source = PingSource.COMMUNITY,
                        message = "Morning pickup started smoothly."
                    )
                )
            ),
            announcements = listOf(
                Announcement(
                    id = "announcement_1",
                    title = "Route 4A Delay",
                    message = "Expected delay of 15 minutes due to traffic near the highway junction.",
                    timestamp = now - (10 * minute),
                    severity = AnnouncementSeverity.WARNING
                ),
                Announcement(
                    id = "announcement_2",
                    title = "Schedule Update",
                    message = "Morning pickup times will shift 5 minutes earlier starting next week.",
                    timestamp = now - (26 * 60 * minute),
                    severity = AnnouncementSeverity.INFO
                )
            ),
            contacts = listOf(
                EmergencyContact("contact_1", "RS", "Rahul Sharma", "Father", "+91 98765 43210", true),
                EmergencyContact("contact_2", "PS", "Priya Sharma", "Mother", "+91 98220 11773", true),
                EmergencyContact("contact_3", "AM", "Aditi Ma'am", "Transport Lead", "+91 98112 22884", false)
            ),
            activity = listOf(
                ActivityItem(
                    id = "activity_1",
                    title = "Started Route 4A",
                    detail = "Live commute tracking is active for River Road Connector.",
                    timestamp = now - (55 * minute),
                    type = ActivityType.ROUTE
                ),
                ActivityItem(
                    id = "activity_2",
                    title = "Safe arrival shared",
                    detail = "Trusted contacts confirmed the last ride reached campus.",
                    timestamp = now - (18 * minute),
                    type = ActivityType.SAFETY
                )
            ),
            lastSafeReachTimestamp = now - (18 * minute)
        )
    }
}
