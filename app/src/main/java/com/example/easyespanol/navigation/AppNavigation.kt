package com.example.easyespanol.navigation

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
    NavHost(navController = navController, startDestination = Routes.HOME) {
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
