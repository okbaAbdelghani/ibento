package com.okbatech.smartevents.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.okbatech.smartevents.ui.theme.EvenroTheme

enum class EvenroNavDestination(val icon: ImageVector, val label: String) {
    Home(Icons.Filled.Home, "Home"),
    Calendar(Icons.Filled.CalendarMonth, "Calendar"),
    Map(Icons.Filled.Place, "Map"),
    Profile(Icons.Filled.Person, "Profile"),
}

/** The 4-tab bottom bar (Home / Calendar / Map / Profile) with a dot indicator on the active tab. */
@Composable
fun EvenroBottomNavBar(
    selected: EvenroNavDestination,
    onSelect: (EvenroNavDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            EvenroNavDestination.entries.forEach { destination ->
                NavItem(
                    destination = destination,
                    isSelected = destination == selected,
                    onClick = { onSelect(destination) },
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    destination: EvenroNavDestination,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val extended = EvenroTheme.extendedColors
    val tint = if (isSelected) MaterialTheme.colorScheme.primary else extended.textTertiary
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = destination.icon, contentDescription = destination.label, tint = tint)
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(4.dp)
                .background(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape,
                ),
        )
    }
}
