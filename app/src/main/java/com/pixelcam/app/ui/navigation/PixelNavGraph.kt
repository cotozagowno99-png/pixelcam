package com.pixelcam.app.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pixelcam.app.ui.screens.camera.CameraScreen
import com.pixelcam.app.ui.screens.gallery.GalleryScreen
import com.pixelcam.app.ui.screens.settings.SettingsScreen
import com.pixelcam.app.ui.screens.splash.SplashScreen
import com.pixelcam.app.ui.screens.viewer.PhotoViewerScreen

object Routes {
    const val SPLASH = "splash"
    const val CAMERA = "camera"
    const val GALLERY = "gallery"
    const val SETTINGS = "settings"
    const val VIEWER = "viewer/{photoId}"
    fun viewer(photoId: Long) = "viewer/$photoId"
}

@Composable
fun PixelNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { slideInHorizontally(tween(180)) { it / 3 } + fadeIn(tween(180)) },
        exitTransition = { fadeOut(tween(120)) },
        popEnterTransition = { fadeIn(tween(150)) },
        popExitTransition = { slideOutHorizontally(tween(150)) { it / 3 } + fadeOut(tween(150)) }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                navController.navigate(Routes.CAMERA) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }
        composable(Routes.CAMERA) {
            CameraScreen(
                onOpenGallery = { navController.navigate(Routes.GALLERY) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.GALLERY) {
            GalleryScreen(
                onBack = { navController.popBackStack() },
                onOpenPhoto = { id -> navController.navigate(Routes.viewer(id)) }
            )
        }
        composable(
            route = Routes.VIEWER,
            arguments = listOf(navArgument("photoId") { type = NavType.LongType })
        ) { entry ->
            val photoId = entry.arguments?.getLong("photoId") ?: return@composable
            PhotoViewerScreen(
                photoId = photoId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
