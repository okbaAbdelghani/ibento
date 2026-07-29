package com.okbatech.smartevents.feature.events.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.okbatech.smartevents.core.designsystem.components.CategoryChip
import com.okbatech.smartevents.core.designsystem.components.EventListRow
import com.okbatech.smartevents.core.designsystem.components.EvenroSearchBar
import com.okbatech.smartevents.feature.events.presentation.filter.AllCategory
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import com.okbatech.smartevents.util.formatEventDate

@Composable
fun SearchRoute(
    onBack: () -> Unit,
    onOpenEventDetails: (String) -> Unit,
    onOpenFilter: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SearchScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onOpenEventDetails = onOpenEventDetails,
        onOpenFilter = onOpenFilter,
    )
}

@Composable
private fun SearchScreen(
    uiState: SearchUiState,
    onEvent: (SearchEvent) -> Unit,
    onBack: () -> Unit,
    onOpenEventDetails: (String) -> Unit,
    onOpenFilter: () -> Unit,
) {
    val extended = EvenroTheme.extendedColors

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            EvenroSearchBar(
                query = uiState.query,
                onQueryChange = { onEvent(SearchEvent.QueryChanged(it)) },
                onFilterClick = onOpenFilter,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 12.dp),
            ) {
                (listOf(AllCategory) + SearchCategories).forEach { category ->
                    CategoryChip(
                        label = category,
                        selected = category == uiState.filter.category,
                        onClick = { onEvent(SearchEvent.CategoryToggled(category)) },
                    )
                }
            }

            if (uiState.results.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No events match your search",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extended.textSecondary,
                    )
                }
            } else {
                LazyColumn {
                    items(uiState.results, key = { it.id }) { event ->
                        EventListRow(
                            imageUrl = event.imageUrl,
                            title = event.title,
                            dateLabel = formatEventDate(event.startDateTime),
                            locationLabel = "${event.venueName}, ${event.city}",
                            priceLabel = "$${event.priceAmount.toInt()} USD",
                            onClick = { onOpenEventDetails(event.id) },
                            onActionClick = { onEvent(SearchEvent.Join(event.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    SmartEventsTheme {
        SearchScreen(uiState = SearchUiState(), onEvent = {}, onBack = {}, onOpenEventDetails = {}, onOpenFilter = {})
    }
}
