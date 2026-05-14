package com.example.saniya.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.saniya.TransportUiState
import com.example.saniya.data.BusStatus
import com.example.saniya.data.PingSource
import com.example.saniya.data.Route
import com.example.saniya.data.RoutePing
import com.example.saniya.data.RouteProgressSnapshot
import com.example.saniya.data.currentStop
import com.example.saniya.data.etaToDestination
import com.example.saniya.data.hasReached
import com.example.saniya.data.nextStop
import com.example.saniya.data.progressFraction
import com.example.saniya.data.stopIndex
import com.example.saniya.ui.components.AppDestinationScaffold
import com.example.saniya.ui.navigation.Screen

@Composable
fun RouteDashboardScreen(
    navController: NavController,
    uiState: TransportUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit
) {
    val route = uiState.activeRoute
    val snapshot = uiState.snapshotFor(route.id)
    val currentStop = route.currentStop(snapshot)
    val nextStop = route.nextStop(snapshot)
    val eta = route.etaToDestination(snapshot)
    val recentPings = uiState.pingHistoryFor(route.id)

    AppDestinationScaffold(
        navController = navController,
        currentScreen = Screen.RouteDashboard,
        snackbarHostState = snackbarHostState
    ) { modifier ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = modifier.padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Route ${route.code} Dashboard",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = route.summary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ETA",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Text(
                                text = "${eta} mins",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Next stop",
                        value = nextStop.name,
                        accentColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Service",
                        value = statusLabel(snapshot),
                        accentColor = when (snapshot.status) {
                            BusStatus.NORMAL -> MaterialTheme.colorScheme.tertiaryContainer
                            BusStatus.DELAYED -> MaterialTheme.colorScheme.secondaryContainer
                            BusStatus.BREAKDOWN -> MaterialTheme.colorScheme.errorContainer
                        },
                        contentColor = when (snapshot.status) {
                            BusStatus.NORMAL -> MaterialTheme.colorScheme.onTertiaryContainer
                            BusStatus.DELAYED -> MaterialTheme.colorScheme.onSecondaryContainer
                            BusStatus.BREAKDOWN -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Live from",
                        value = currentStop.name,
                        accentColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                ElevatedCard(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Live Route Tracking",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = when (snapshot.status) {
                                    BusStatus.NORMAL -> MaterialTheme.colorScheme.tertiaryContainer
                                    BusStatus.DELAYED -> MaterialTheme.colorScheme.secondaryContainer
                                    BusStatus.BREAKDOWN -> MaterialTheme.colorScheme.errorContainer
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (snapshot.status) {
                                            BusStatus.NORMAL -> Icons.Default.MyLocation
                                            BusStatus.DELAYED -> Icons.Default.Traffic
                                            BusStatus.BREAKDOWN -> Icons.Default.Warning
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = when (snapshot.status) {
                                            BusStatus.NORMAL -> MaterialTheme.colorScheme.onTertiaryContainer
                                            BusStatus.DELAYED -> MaterialTheme.colorScheme.onSecondaryContainer
                                            BusStatus.BREAKDOWN -> MaterialTheme.colorScheme.onErrorContainer
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = statusLabel(snapshot),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = when (snapshot.status) {
                                            BusStatus.NORMAL -> MaterialTheme.colorScheme.onTertiaryContainer
                                            BusStatus.DELAYED -> MaterialTheme.colorScheme.onSecondaryContainer
                                            BusStatus.BREAKDOWN -> MaterialTheme.colorScheme.onErrorContainer
                                        }
                                    )
                                }
                            }
                        }

                        LiveRouteTimeline(route = route, snapshot = snapshot)
                    }
                }
            }

            item {
                ElevatedCard(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Pings",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = onRefresh) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh route",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (recentPings.isEmpty()) {
                            Text(
                                text = "No community updates yet for this route. Use Ping Hub to send the first live checkpoint.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            recentPings.forEach { ping ->
                                RecentPingItem(ping = ping)
                            }
                        }

                        Button(
                            onClick = {
                                navController.navigate(Screen.PingHub.route) {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open Ping Hub",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    accentColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = accentColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.86f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LiveRouteTimeline(route: Route, snapshot: RouteProgressSnapshot) {
    val progress by animateFloatAsState(
        targetValue = route.progressFraction(snapshot),
        label = "route_progress"
    )
    val currentIndex = route.stopIndex(snapshot.currentStopId)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
    ) {
        val markerOffset = ((maxWidth - 56.dp) * progress).coerceIn(0.dp, maxWidth - 56.dp)

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth(progress.coerceAtLeast(0.04f))
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            route.stops.forEachIndexed { index, stop ->
                Column(
                    modifier = Modifier.width(56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (index == route.stops.lastIndex) 30.dp else 24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    index < currentIndex -> MaterialTheme.colorScheme.primary
                                    index == route.stops.lastIndex -> MaterialTheme.colorScheme.surface
                                    index == currentIndex -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            index < currentIndex -> Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            index == route.stops.lastIndex -> Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = stop.name,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = if (route.hasReached(stop, snapshot)) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 4.dp)
                .offset(x = markerOffset, y = (-40).dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(top = 60.dp)
                .offset(x = markerOffset),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.inverseSurface
        ) {
            Text(
                text = "Updated ${relativeTime(snapshot.updatedAt)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun RecentPingItem(ping: RoutePing) {
    val accentColor = when {
        ping.status == BusStatus.BREAKDOWN -> MaterialTheme.colorScheme.error
        ping.status == BusStatus.DELAYED -> MaterialTheme.colorScheme.secondary
        ping.source == PingSource.SYSTEM -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(54.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(accentColor)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "${ping.author}: ${ping.message}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${relativeTime(ping.timestamp)} • ${ping.source.name.lowercase().replaceFirstChar(Char::titlecase)} ping",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun statusLabel(snapshot: RouteProgressSnapshot): String {
    return when (snapshot.status) {
        BusStatus.NORMAL -> "Live"
        BusStatus.DELAYED -> "Delayed +${snapshot.delayMinutes}m"
        BusStatus.BREAKDOWN -> "Support on route"
    }
}

internal fun relativeTime(timestamp: Long): String {
    val diffMinutes = ((System.currentTimeMillis() - timestamp) / 60_000L).coerceAtLeast(0L)
    return when {
        diffMinutes <= 1L -> "just now"
        diffMinutes < 60L -> "$diffMinutes mins ago"
        diffMinutes < 24L * 60L -> "${diffMinutes / 60L} hrs ago"
        else -> "${diffMinutes / (24L * 60L)} days ago"
    }
}
