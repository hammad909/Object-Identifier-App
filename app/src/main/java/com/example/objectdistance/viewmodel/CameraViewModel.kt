package com.example.objectdistance.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraViewModel : ViewModel() {

    private val _cameraPermissionGranted = MutableStateFlow(false)
    val cameraPermissionGranted : StateFlow<Boolean> = _cameraPermissionGranted.asStateFlow()


    fun onPermissionGranted() {
        _cameraPermissionGranted.value = true
    }

    fun onPermissionDenied(){
        _cameraPermissionGranted.value = false
    }


}