package com.example.objectdistance.mynavigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.objectdistance.screens.CameraXPreview
import com.example.objectdistance.screens.MainScreen
import com.example.objectdistance.viewmodel.CameraViewModel

@Composable
fun MyAppNavigation( modifier : Modifier,cameraViewModel: CameraViewModel) {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "mainScreen") {

        composable("mainScreen") {

            MainScreen(
                navController = navController,
                cameraViewModel = cameraViewModel
            )
        }




        composable("cameraScreen") {

            CameraXPreview(navController)
        }


    }

}

