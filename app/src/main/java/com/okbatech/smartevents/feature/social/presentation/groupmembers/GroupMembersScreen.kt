package com.okbatech.smartevents.feature.social.presentation.groupmembers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

@Composable
fun GroupMembersRoute(
    onBack: () -> Unit,
    viewModel: GroupMembersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    GroupMembersScreen(uiState = uiState, onBack = onBack)
}

@Composable
private fun GroupMembersScreen(uiState: GroupMembersUiState, onBack: () -> Unit) {
    val extended = EvenroTheme.extendedColors

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    "Group Members (${uiState.members.size})",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            if (uiState.members.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No members yet", style = MaterialTheme.typography.bodyMedium, color = extended.textSecondary)
                }
            } else {
                LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)) {
                    items(uiState.members, key = { it.id }) { member ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AsyncImage(
                                model = member.avatarUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(extended.surfaceMuted),
                            )
                            Text(member.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupMembersScreenPreview() {
    SmartEventsTheme { GroupMembersScreen(uiState = GroupMembersUiState(), onBack = {}) }
}
