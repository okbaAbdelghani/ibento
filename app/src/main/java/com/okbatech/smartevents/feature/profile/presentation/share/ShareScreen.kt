package com.okbatech.smartevents.feature.profile.presentation.share

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.okbatech.smartevents.ui.theme.EvenroTheme
import com.okbatech.smartevents.ui.theme.Shapes
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import kotlinx.coroutines.launch

@Composable
fun ShareRoute(
    eventTitle: String,
    eventId: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val shareText = "Check out \"$eventTitle\" on Evenro! evenro://events/$eventId"

    ShareScreen(
        eventTitle = eventTitle,
        onDismiss = onDismiss,
        onCopyLink = {
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Event link", shareText)))
            }
            onDismiss()
        },
        onShareVia = {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            context.startActivity(Intent.createChooser(intent, "Share event"))
            onDismiss()
        },
    )
}

@Composable
private fun ShareScreen(
    eventTitle: String,
    onDismiss: () -> Unit,
    onCopyLink: () -> Unit,
    onShareVia: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .background(MaterialTheme.colorScheme.surface, Shapes.extraLarge)
                    .padding(vertical = 24.dp, horizontal = 20.dp),
            ) {
                Text(text = "Share \"$eventTitle\"", style = MaterialTheme.typography.titleMedium)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
                    ShareOption(icon = Icons.Filled.ContentCopy, label = "Copy Link", onClick = onCopyLink)
                    ShareOption(icon = Icons.AutoMirrored.Filled.Chat, label = "Message", onClick = onShareVia)
                    ShareOption(icon = Icons.Filled.Groups, label = "Group", onClick = onShareVia)
                    ShareOption(icon = Icons.Filled.Email, label = "Email", onClick = onShareVia)
                    ShareOption(icon = Icons.AutoMirrored.Filled.Send, label = "More", onClick = onShareVia)
                }
            }
        }
    }
}

@Composable
private fun RowScope.ShareOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    val extended = EvenroTheme.extendedColors
    Column(
        modifier = Modifier.weight(1f).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(48.dp).background(extended.surfaceMuted, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = extended.textSecondary,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ShareScreenPreview() {
    SmartEventsTheme {
        ShareScreen(eventTitle = "Shere Bangla Concert", onDismiss = {}, onCopyLink = {}, onShareVia = {})
    }
}
