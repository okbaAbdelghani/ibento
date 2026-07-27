package com.okbatech.smartevents.feature.booking.presentation.coviddeclaration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

private val Declarations = listOf(
    "I am not experiencing any COVID-19 symptoms.",
    "I have not been in close contact with a confirmed case in the last 14 days.",
    "I agree to follow the venue's health and safety guidelines.",
)

@Composable
fun CovidDeclarationRoute(
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    CovidDeclarationScreen(onBack = onBack, onContinue = onContinue)
}

@Composable
private fun CovidDeclarationScreen(onBack: () -> Unit, onContinue: () -> Unit) {
    val extended = EvenroTheme.extendedColors
    var checked by remember { mutableStateOf(List(Declarations.size) { false }) }
    val allChecked = checked.all { it }

    Scaffold(
        bottomBar = {
            EvenroButton(
                text = "CONTINUE",
                onClick = onContinue,
                enabled = allChecked,
                modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(16.dp).height(52.dp),
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Covid Declaration",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Text(
                text = "Please confirm the following before completing your booking",
                style = MaterialTheme.typography.bodyMedium,
                color = extended.textSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )

            Declarations.forEachIndexed { index, declaration ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = checked[index],
                        onCheckedChange = { value -> checked = checked.toMutableList().also { it[index] = value } },
                    )
                    Text(declaration, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CovidDeclarationScreenPreview() {
    SmartEventsTheme { CovidDeclarationScreen(onBack = {}, onContinue = {}) }
}
