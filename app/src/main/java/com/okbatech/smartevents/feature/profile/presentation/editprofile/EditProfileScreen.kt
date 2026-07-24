package com.okbatech.smartevents.feature.profile.presentation.editprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.core.designsystem.components.EvenroTextField
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.SmartEventsTheme

@Composable
fun EditProfileRoute(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    EditProfileScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
    )
}

@Composable
private fun EditProfileScreen(
    uiState: EditProfileUiState,
    onEvent: (EditProfileEvent) -> Unit,
    onBack: () -> Unit,
) {
    val extended = EvenroTheme.extendedColors

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Edit Profile",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = uiState.avatarUrl.ifBlank { null },
                    contentDescription = null,
                    modifier = Modifier.size(96.dp).clip(CircleShape).background(extended.surfaceMuted),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                EvenroTextField(
                    value = uiState.name,
                    onValueChange = { onEvent(EditProfileEvent.NameChanged(it)) },
                    placeholder = "Full name",
                    leadingIcon = Icons.Filled.Person,
                )
                EvenroTextField(
                    value = uiState.phone,
                    onValueChange = { onEvent(EditProfileEvent.PhoneChanged(it)) },
                    placeholder = "Phone number",
                    leadingIcon = Icons.Filled.Phone,
                    keyboardType = KeyboardType.Phone,
                )
                EvenroTextField(
                    value = uiState.avatarUrl,
                    onValueChange = { onEvent(EditProfileEvent.AvatarUrlChanged(it)) },
                    placeholder = "Avatar image URL",
                    leadingIcon = Icons.Filled.Link,
                )
                EvenroTextField(
                    value = uiState.bio,
                    onValueChange = { onEvent(EditProfileEvent.BioChanged(it)) },
                    placeholder = "Short bio",
                    leadingIcon = Icons.Filled.Info,
                )
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            EvenroButton(
                text = if (uiState.isLoading) "SAVING..." else "SAVE CHANGES",
                onClick = { onEvent(EditProfileEvent.Save) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditProfileScreenPreview() {
    SmartEventsTheme {
        EditProfileScreen(
            uiState = EditProfileUiState(name = "MD Rafi Islam", phone = "+880 1234 567890", bio = "Design meetup organizer."),
            onEvent = {},
            onBack = {},
        )
    }
}
