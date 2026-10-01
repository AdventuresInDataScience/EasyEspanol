package com.example.easyespanol.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.easyespanol.data.AppSettings
import com.example.easyespanol.data.PhraseRepository
import com.example.easyespanol.ui.expressions.ExpressionsScreen
import com.example.easyespanol.ui.home.HomeScreen
import com.example.easyespanol.ui.settings.SettingsScreen
import com.example.easyespanol.ui.study.StudyScreen
import com.example.easyespanol.ui.topics.ScenesScreen
import com.example.easyespanol.ui.topics.TopicsScreen

object Routes {
    const val HOME = "home"
    const val TOPICS = "topics"
    const val SCENES = "scenes/{topicKey}"
    const val STUDY = "study/{sceneKey}"
    const val EXPRESSIONS = "expressions"
    const val SETTINGS = "settings"

    fun scenes(topicKey: String) = "scenes/$topicKey"
    fun study(sceneKey: String) = "study/$sceneKey"
}

@Composable
fun AppNavigation(navController: NavHostController, repo: PhraseRepository, settings: AppSettings) {
    // Screens slide in from the right as you go deeper, and back out to the right
    // as you return. The screen underneath drifts a little way for depth.
    val duration = 340
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(duration)) +
                fadeIn(tween(duration))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(duration)) { it / 4 } +
                fadeOut(tween(duration / 2))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(duration)) { it / 4 } +
                fadeIn(tween(duration))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(duration)) +
                fadeOut(tween(duration / 2))
        },
    ) {
        composable(Routes.HOME) {
            HomeScreen(navController, repo, settings)
        }
        composable(Routes.TOPICS) {
            TopicsScreen(navController, repo, settings)
        }
        composable(
            route = Routes.SCENES,
            arguments = listOf(navArgument("topicKey") { type = NavType.StringType }),
        ) { entry ->
            ScenesScreen(navController, repo, settings, entry.arguments?.getString("topicKey").orEmpty())
        }
        composable(
            route = Routes.STUDY,
            arguments = listOf(navArgument("sceneKey") { type = NavType.StringType }),
        ) { entry ->
            StudyScreen(navController, repo, settings, entry.arguments?.getString("sceneKey").orEmpty())
        }
        composable(Routes.EXPRESSIONS) {
            ExpressionsScreen(navController, repo, settings)
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(navController, repo, settings)
        }
    }
}
