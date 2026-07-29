package com.okbatech.smartevents.feature.events.presentation.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.okbatech.smartevents.core.designsystem.components.CategoryChip
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.core.designsystem.components.EvenroButtonStyle
import com.okbatech.smartevents.feature.events.presentation.search.SearchCategories
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

@Composable
fun FilterRoute(
    initialCriteria: FilterCriteria,
    onBack: () -> Unit,
    onApply: (FilterCriteria) -> Unit,
) {
    FilterScreen(initialCriteria = initialCriteria, onBack = onBack, onApply = onApply)
}

@Composable
private fun FilterScreen(
    initialCriteria: FilterCriteria,
    onBack: () -> Unit,
    onApply: (FilterCriteria) -> Unit,
) {
    val extended = EvenroTheme.extendedColors
    var category by remember { mutableStateOf(initialCriteria.category) }
    var priceRange by remember { mutableStateOf(initialCriteria.minPrice..initialCriteria.maxPrice) }

    Scaffold(
        bottomBar = {
            Column(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                EvenroButton(
                    text = "RESET",
                    style = EvenroButtonStyle.Outline,
                    onClick = { onApply(FilterCriteria()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
                )
                EvenroButton(
                    text = "APPLY FILTER",
                    onClick = {
                        onApply(
                            FilterCriteria(
                                category = category,
                                minPrice = priceRange.start,
                                maxPrice = priceRange.endInclusive,
                            ),
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                )
            }
        },
    ) { padding ->
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
                    text = "Filter",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                (listOf(AllCategory) + SearchCategories).chunked(2).forEach { row ->
                    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { rowCategory ->
                            CategoryChip(
                                label = rowCategory,
                                selected = rowCategory == category,
                                onClick = { category = rowCategory },
                            )
                        }
                    }
                }
            }

            Text(
                text = "Price range",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 32.dp, bottom = 4.dp),
            )
            Text(
                text = "$${priceRange.start.toInt()} - $${priceRange.endInclusive.toInt()} USD",
                style = MaterialTheme.typography.bodyMedium,
                color = extended.textSecondary,
            )
            RangeSlider(
                value = priceRange,
                onValueChange = { priceRange = it },
                valueRange = 0f..500f,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterScreenPreview() {
    SmartEventsTheme {
        FilterScreen(initialCriteria = FilterCriteria(), onBack = {}, onApply = {})
    }
}
