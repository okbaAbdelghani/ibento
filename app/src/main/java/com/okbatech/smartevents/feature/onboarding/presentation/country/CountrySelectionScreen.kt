package com.okbatech.smartevents.feature.onboarding.presentation.country

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.core.designsystem.components.EvenroSearchBar
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

private data class Country(val name: String, val flagEmoji: String)

private val Countries = listOf(
    Country("Bangladesh", "🇧🇩"),
    Country("Australia", "🇦🇺"),
    Country("Pakistan", "🇵🇰"),
    Country("England", "🇬🇧"),
    Country("United Arab Emirates", "🇦🇪"),
    Country("Germany", "🇩🇪"),
    Country("United States America", "🇺🇸"),
    Country("Netherland", "🇳🇱"),
)

@Composable
fun CountrySelectionRoute(
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    CountrySelectionScreen(onBack = onBack, onSave = onSave)
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CountrySelectionScreen(onBack: () -> Unit, onSave: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(Countries.first().name) }
    val filtered = remember(query) {
        if (query.isBlank()) Countries else Countries.filter { it.name.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Country Selection") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            EvenroButton(
                text = "SAVE",
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            EvenroSearchBar(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Find Conversation",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(filtered, key = { it.name }) { country ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(country.flagEmoji, style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = country.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                        RadioButton(
                            selected = selected == country.name,
                            onClick = { selected = country.name },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CountrySelectionScreenPreview() {
    SmartEventsTheme { CountrySelectionScreen(onBack = {}, onSave = {}) }
}
