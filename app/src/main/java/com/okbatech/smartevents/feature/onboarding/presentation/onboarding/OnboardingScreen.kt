package com.okbatech.smartevents.feature.onboarding.presentation.onboarding

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.okbatech.smartevents.core.designsystem.components.EvenroButton
import com.okbatech.smartevents.ui.theme.SmartEventsTheme
import kotlinx.coroutines.launch

@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    OnboardingScreen(onFinished = { viewModel.finishOnboarding(onFinished) })
}

@Composable
private fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { OnboardingPages.size })
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        Column(modifier = Modifier.padding(padding)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                OnboardingPageContent(
                    page = OnboardingPages[page],
                    isLastPage = page == OnboardingPages.lastIndex,
                    pageIndex = page,
                    pageCount = OnboardingPages.size,
                    onSkip = onFinished,
                    onNext = {
                        if (page == OnboardingPages.lastIndex) {
                            onFinished()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(page + 1) }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    isLastPage: Boolean,
    pageIndex: Int,
    pageCount: Int,
    onSkip: () -> Unit,
    onNext: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "EVENT",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp),
            )
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = onboardingEmoji(pageIndex), fontSize = 64.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .padding(24.dp),
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 8.dp, bottom = 28.dp),
            )

            if (isLastPage) {
                EvenroButton(
                    text = "GET STARTED",
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.clickable(onClick = onSkip),
                    )
                    PageDots(pageCount = pageCount, currentPage = pageIndex)
                    Text(
                        text = "Next",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.clickable(onClick = onNext),
                    )
                }
            }
        }
    }
}

@Composable
private fun PageDots(pageCount: Int, currentPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentPage) 8.dp else 6.dp)
                    .background(
                        color = Color.White.copy(alpha = if (index == currentPage) 1f else 0.5f),
                        shape = CircleShape,
                    ),
            )
        }
    }
}

private fun onboardingEmoji(pageIndex: Int): String = when (pageIndex) {
    0 -> "🎉"
    1 -> "🎟️"
    else -> "🎶"
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    SmartEventsTheme { OnboardingScreen(onFinished = {}) }
}
