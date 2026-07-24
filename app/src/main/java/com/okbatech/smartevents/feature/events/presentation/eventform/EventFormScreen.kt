package com.okbatech.smartevents.feature.events.presentation.eventform

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.okbatech.smartevents.core.designsystem.components.CategoryChip
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.core.designsystem.components.EvenroTextField
import com.okbatech.smartevents.feature.events.presentation.search.SearchCategories
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.Shapes
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatEventDate

@Composable
fun EventFormRoute(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: EventFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedEventId) {
        uiState.savedEventId?.let(onSaved)
    }

    EventFormScreen(uiState = uiState, onEvent = viewModel::onEvent, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventFormScreen(
    uiState: EventFormUiState,
    onEvent: (EventFormEvent) -> Unit,
    onBack: () -> Unit,
) {
    val extended = EvenroTheme.extendedColors
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            EvenroButton(
                text = when {
                    uiState.isSaving -> "SAVING..."
                    uiState.isEditMode -> "SAVE CHANGES"
                    else -> "CREATE EVENT"
                },
                onClick = { onEvent(EventFormEvent.Submit) },
                enabled = uiState.canSubmit && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = if (uiState.isEditMode) "Edit Event" else "Add Event",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Text("Cover image", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EventFormSampleImageUrls.forEach { url ->
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(Shapes.medium)
                            .clickable { onEvent(EventFormEvent.ImageSelected(url)) },
                    ) {
                        AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        if (url == uiState.imageUrl) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = "Selected", tint = androidx.compose.ui.graphics.Color.White)
                            }
                        }
                    }
                }
            }

            EvenroTextField(
                value = uiState.title,
                onValueChange = { onEvent(EventFormEvent.TitleChanged(it)) },
                placeholder = "Event title",
                modifier = Modifier.padding(top = 16.dp),
            )
            EvenroTextField(
                value = uiState.description,
                onValueChange = { onEvent(EventFormEvent.DescriptionChanged(it)) },
                placeholder = "Description",
                modifier = Modifier.padding(top = 16.dp),
            )

            Text("Category", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SearchCategories.forEach { category ->
                    CategoryChip(
                        label = category,
                        selected = category == uiState.category,
                        onClick = { onEvent(EventFormEvent.CategoryChanged(category)) },
                    )
                }
            }

            EvenroTextField(
                value = uiState.venueName,
                onValueChange = { onEvent(EventFormEvent.VenueChanged(it)) },
                placeholder = "Venue name",
                modifier = Modifier.padding(top = 16.dp),
            )
            EvenroTextField(
                value = uiState.city,
                onValueChange = { onEvent(EventFormEvent.CityChanged(it)) },
                placeholder = "City",
                modifier = Modifier.padding(top = 16.dp),
            )

            Row(modifier = Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EvenroTextField(
                    value = uiState.priceAmount,
                    onValueChange = { onEvent(EventFormEvent.PriceChanged(it)) },
                    placeholder = "Price (USD)",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
                EvenroTextField(
                    value = uiState.capacity,
                    onValueChange = { onEvent(EventFormEvent.CapacityChanged(it)) },
                    placeholder = "Capacity",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp)
                    .clickable { showDatePicker = true }
                    .background(extended.surfaceMuted, Shapes.medium)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(
                    text = formatEventDate(uiState.startDateTime),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }

            if (uiState.errorMessage != null) {
                Text(uiState.errorMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.startDateTime)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onEvent(EventFormEvent.DateChanged(it)) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EventFormScreenPreview() {
    SmartEventsTheme {
        EventFormScreen(uiState = EventFormUiState(), onEvent = {}, onBack = {})
    }
}
