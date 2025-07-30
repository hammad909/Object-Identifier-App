package com.example.objectdistance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.objectdistance.mynavigation.MyAppNavigation
import com.example.objectdistance.ui.theme.ObjectDistanceTheme
import kotlin.getValue
import com.example.objectdistance.viewmodel.CameraViewModel

class MainActivity : ComponentActivity() {

    val cameraViewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ObjectDistanceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    MyAppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        cameraViewModel
                    )

                }
            }
        }
    }
}
