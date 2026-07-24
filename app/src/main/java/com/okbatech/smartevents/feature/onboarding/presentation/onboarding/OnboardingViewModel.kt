package com.okbatech.smartevents.feature.onboarding.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okbatech.smartevents.feature.auth.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingPage(
    val title: String,
    val description: String,
)

val OnboardingPages = listOf(
    OnboardingPage(
        title = "Explore Upcoming and Nearby Events",
        description = "In publishing and graphic design, Lorem is a placeholder text commonly used.",
    ),
    OnboardingPage(
        title = "Create and Find Events Easily in One Place",
        description = "In this app you can create any kind of events and you can join all events.",
    ),
    OnboardingPage(
        title = "Watching Free Concerts with Friends",
        description = "Find and booking concert tickets near you. Invite your friends to watch together.",
    ),
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ViewModel() {

    fun finishOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            completeOnboarding()
            onFinished()
        }
    }
}
