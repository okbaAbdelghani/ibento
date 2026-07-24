package com.okbatech.smartevents.feature.events.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarRoute(
    onBack: () -> Unit,
    onOpenDay: (Long) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CalendarScreen(uiState = uiState, onBack = onBack, onOpenDay = onOpenDay)
}

@Composable
private fun CalendarScreen(
    uiState: CalendarUiState,
    onBack: () -> Unit,
    onOpenDay: (Long) -> Unit,
) {
    val extended = EvenroTheme.extendedColors
    val month = uiState.month

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { label ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(label, style = MaterialTheme.typography.labelMedium, color = extended.textSecondary)
                    }
                }
            }

            val firstDayOffset = month.atDay(1).dayOfWeek.value % 7
            val totalDays = month.lengthOfMonth()
            val cells = (List(firstDayOffset) { null } + (1..totalDays).toList())
            cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { day ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (day != null) {
                                DayCell(
                                    day = day,
                                    hasEvents = day in uiState.eventDaysInMonth,
                                    onClick = { onOpenDay(month.atDay(day).toEpochDay()) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, hasEvents: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = day.toString(), style = MaterialTheme.typography.bodyMedium)
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(5.dp)
                .background(
                    if (hasEvents) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                    CircleShape,
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    SmartEventsTheme {
        CalendarScreen(
            uiState = CalendarUiState(month = YearMonth.now(), eventDaysInMonth = setOf(3, 12, 22)),
            onBack = {},
            onOpenDay = {},
        )
    }
}
