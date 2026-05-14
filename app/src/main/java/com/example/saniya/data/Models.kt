package com.example.saniya.data

data class Institution(
    val id: String,
    val name: String,
    val areaLabel: String
)

data class Route(
    val id: String,
    val institutionId: String,
    val code: String,
    val name: String,
    val summary: String,
    val scheduleLabel: String,
    val durationMinutes: Int,
    val seatsAvailable: Int,
    val vehicleLabel: String,
    val stops: List<Stop>
)

data class Stop(
    val id: String,
    val name: String,
    val order: Int,
    val estimatedMinutesFromStart: Int
)

data class RouteProgressSnapshot(
    val currentStopId: String,
    val status: BusStatus = BusStatus.NORMAL,
    val delayMinutes: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

data class RoutePing(
    val id: String,
    val routeId: String,
    val stopId: String,
    val timestamp: Long,
    val status: BusStatus,
    val author: String,
    val source: PingSource,
    val message: String
)

data class QuickPingAction(
    val id: String,
    val kind: QuickPingKind,
    val title: String,
    val detail: String,
    val stopId: String,
    val status: BusStatus,
    val delayMinutes: Int = 0,
    val message: String
)

data class Announcement(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val severity: AnnouncementSeverity
)

data class EmergencyContact(
    val id: String,
    val initials: String,
    val name: String,
    val relation: String,
    val phone: String,
    val isEnabled: Boolean = true
)

data class ActivityItem(
    val id: String,
    val title: String,
    val detail: String,
    val timestamp: Long,
    val type: ActivityType
)

enum class BusStatus {
    NORMAL,
    DELAYED,
    BREAKDOWN
}

enum class PingSource {
    COMMUNITY,
    SYSTEM,
    SAFETY
}

enum class QuickPingKind {
    CHECKPOINT,
    MARKET,
    GATE,
    DELAY
}

enum class AnnouncementSeverity {
    INFO,
    WARNING,
    SUCCESS
}

enum class ActivityType {
    ROUTE,
    PING,
    SAFETY,
    ALERT
}
