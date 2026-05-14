package com.example.saniya.data

fun Route.stopIndex(stopId: String): Int {
    return stops.indexOfFirst { it.id == stopId }.let { if (it >= 0) it else 0 }
}

fun Route.currentStop(snapshot: RouteProgressSnapshot): Stop {
    return stops.getOrElse(stopIndex(snapshot.currentStopId)) { stops.first() }
}

fun Route.nextStop(snapshot: RouteProgressSnapshot): Stop {
    return stops.getOrElse((stopIndex(snapshot.currentStopId) + 1).coerceAtMost(stops.lastIndex)) {
        stops.last()
    }
}

fun Route.progressFraction(snapshot: RouteProgressSnapshot): Float {
    if (stops.size <= 1) return 0f
    return stopIndex(snapshot.currentStopId).toFloat() / stops.lastIndex.toFloat()
}

fun Route.etaToDestination(snapshot: RouteProgressSnapshot): Int {
    val currentStopMinutes = currentStop(snapshot).estimatedMinutesFromStart
    val destinationMinutes = stops.lastOrNull()?.estimatedMinutesFromStart ?: currentStopMinutes
    return (destinationMinutes - currentStopMinutes + snapshot.delayMinutes).coerceAtLeast(0)
}

fun Route.hasReached(stop: Stop, snapshot: RouteProgressSnapshot): Boolean {
    return stopIndex(snapshot.currentStopId) >= stop.order
}

fun buildQuickPingActions(route: Route): List<QuickPingAction> {
    val checkpoint = route.stops.getOrElse(1) { route.stops.first() }
    val marketStop = route.stops.getOrElse(2) { route.stops.last() }
    val gateStop = route.stops.getOrElse((route.stops.lastIndex - 1).coerceAtLeast(0)) { route.stops.last() }

    return listOf(
        QuickPingAction(
            id = "checkpoint",
            kind = QuickPingKind.CHECKPOINT,
            title = "Crossed ${checkpoint.name}",
            detail = "Update the live tracker after a strong checkpoint.",
            stopId = checkpoint.id,
            status = BusStatus.NORMAL,
            message = "Bus crossed ${checkpoint.name}."
        ),
        QuickPingAction(
            id = "market",
            kind = QuickPingKind.MARKET,
            title = "At ${marketStop.name}",
            detail = "Share the current landmark with everyone on the route.",
            stopId = marketStop.id,
            status = BusStatus.NORMAL,
            message = "Bus is at ${marketStop.name}."
        ),
        QuickPingAction(
            id = "gate",
            kind = QuickPingKind.GATE,
            title = "Reached ${gateStop.name}",
            detail = "Let students know the bus is approaching campus.",
            stopId = gateStop.id,
            status = BusStatus.NORMAL,
            message = "Bus reached ${gateStop.name}."
        ),
        QuickPingAction(
            id = "delay",
            kind = QuickPingKind.DELAY,
            title = "Stuck near ${marketStop.name}",
            detail = "Flag a short delay so ETAs stay realistic.",
            stopId = marketStop.id,
            status = BusStatus.DELAYED,
            delayMinutes = 8,
            message = "Traffic building up near ${marketStop.name}."
        )
    )
}
