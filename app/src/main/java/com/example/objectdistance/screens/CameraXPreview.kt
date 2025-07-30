package com.example.objectdistance.screens


import android.graphics.*
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.objectdistance.imageutils.imageToBitmap
import com.example.objectdistance.imageutils.smoothRect
import com.example.objectdistance.ml.SsdMobilenetV11Metadata1
import com.example.objectdistance.models.DetectionResult
import com.example.objectdistance.models.SmoothedDetection
import com.example.objectdistance.userInterface.DrawDetections
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraXPreview(navController: NavController) {
    val context = LocalContext.current
    //responsible for camera lifecycle like when to off and when to load etc
    val lifecycleOwner = LocalLifecycleOwner.current
    //for preview of CameraX camera preview UI component from the Android View system.
    val previewView = remember { PreviewView(context) }
    //model instance to instantiate model
    val model = remember { SsdMobilenetV11Metadata1.newInstance(context) }
    //Loads the list of object names (labels) that correspond to the class indices predicted by your ML model.
    val labels = remember { FileUtil.loadLabels(context, "labels.txt") }
    //The camera gives larger or differently shaped images, so you must resize them before feeding them into the model.
    val imageProcessor = remember {
        ImageProcessor.Builder()
            .add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR))
            .build()
    }

//create and manage a stateful list of SmoothedDetection objects, which is remembered across recompositions.
    var smoothedDetections by remember { mutableStateOf<List<SmoothedDetection>>(emptyList()) }

    Box(Modifier.fillMaxSize()) {
        //This embeds a traditional Android View (like PreviewView from CameraX) inside Jetpack Compose.
        //previewView shows the live camera feed.
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        DrawDetections(smoothedDetections.map { DetectionResult(it.label, it.score, it.rect) } as List<DetectionResult>)
    }

    // Throttle updates to every 200ms
    val lastUpdateTime = remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        //Camera Initialization
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.get()

        // Preview Setup
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        //Image Analysis Setup
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build()

        // Analyzer Logic
        analysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
            try {
                //Frame received from camera
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    //Convert to Bitmap with correct rotation
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    val bitmap = imageToBitmap(mediaImage, rotationDegrees)

                    //Convert Bitmap to TensorImage for ML model
                    val tensorImage = TensorImage.fromBitmap(bitmap)
                    val processedImage = imageProcessor.process(tensorImage)

                    //Run the TensorFlow Lite model
                    val output = model.process(processedImage)
                    //Extract detection info from model output
                    val locations = output.locationsAsTensorBuffer.floatArray
                    val classes = output.classesAsTensorBuffer.floatArray
                    val scores = output.scoresAsTensorBuffer.floatArray

                    //Initializes an empty list to hold the final filtered and labeled detection results.
                    val newDetections = mutableListOf<DetectionResult>()
                    //The model may detect multiple objects per frame.
                    for (i in scores.indices) {
                        //Only keep predictions with high enough confidence (> 60%).
                        if (scores[i] > 0.6f) {
                            //The locations array holds bounding box coordinates.
                            //Each detection has 4 values: top, left, bottom, right → grouped using index math.
                            //These values are turned into a RectF, which is a drawable rectangle.
                            val top = locations[i * 4]
                            val left = locations[i * 4 + 1]
                            val bottom = locations[i * 4 + 2]
                            val right = locations[i * 4 + 3]
                            val rect = RectF(left, top, right, bottom)
                            //Gets the human-readable label from the labels list using the class index.
                            //If the class index is invalid, fallback label is "Unknown".
                            val label = labels.getOrElse(classes[i].toInt()) { "Unknown" }
                            //adding result to our object
                            newDetections.add(DetectionResult(label, scores[i], rect))
                        }
                    }

                    // Get current time
                    val currentTime = System.currentTimeMillis()

                    // Only update the UI state every 200ms to slow redraw rate
                    if (currentTime - lastUpdateTime.longValue > 200) {
                        lastUpdateTime.longValue = currentTime

                        // Smooth between old and new detections
                        val updatedSmoothed = mutableListOf<SmoothedDetection>()

                        newDetections.forEach { newDet ->
                            val oldDet = smoothedDetections.find { it.label == newDet.label }
                            if (oldDet != null) {
                                val smoothedRect = smoothRect(oldDet.rect, newDet.rect)
                                updatedSmoothed.add(SmoothedDetection(newDet.label, newDet.score, smoothedRect))
                            } else {
                                updatedSmoothed.add(SmoothedDetection(newDet.label, newDet.score, newDet.rect))
                            }
                        }

                        smoothedDetections = updatedSmoothed
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                imageProxy.close()
            }
        }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analysis
        )
    }
}




