package ca.qolt.ui.registration.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ca.qolt.R
import kotlinx.coroutines.launch

@Composable
fun Onboarding(modifier: Modifier = Modifier, viewModel: OnboardingViewModel) {
    BackHandler { viewModel.navigator.finish() }
    OnboardingPager(viewModel::onFinished)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingPager(
    onFinished: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val pages: List<@Composable () -> Unit> = listOf(
        {
            OnboardingScreen(
                imageRes = R.drawable.onboarding1,
                title = "Stay focused in one tap",
                subtitle = "Tap your phone to an NFC tag\nto block apps instantly.",
                buttonText = "Next",
                page = 0,
                onSkip = onFinished,
                onNext = {
                    scope.launch { pagerState.animateScrollToPage(1) }
                }
            )
        },

        {
            OnboardingScreen(
                imageRes = R.drawable.onboarding2,
                title = "Custom blocks for real life",
                subtitle = "Create different modes.\nChoose what gets blocked.",
                buttonText = "Next",
                page = 1,
                onSkip = onFinished,
                onNext = {
                    scope.launch { pagerState.animateScrollToPage(2) }
                }
            )
        },

        {
            OnboardingScreen(
                imageRes = R.drawable.onboarding3,
                title = "Place your tag far away",
                subtitle = "Make unlocking harder - \nand focus effortless.",
                buttonText = "Get Started",
                page = 2,
                onSkip = onFinished,
                onNext = {
                    onFinished()
                }
            )
        }
    )

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        pages[page]()
    }
}

