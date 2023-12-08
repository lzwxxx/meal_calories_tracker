package com.cs461.g6.mealportiontracker.foodimageprocessing

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.cs461.g6.mealportiontracker.theme.mealColors
import com.cs461.g6.mealportiontracker.theme.MealTheme
import com.google.accompanist.permissions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executor
import kotlin.coroutines.*

class CameraXPreviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MealTheme {
                App()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun App(modifier: Modifier = Modifier) {
    var imageUri by remember { mutableStateOf(EMPTY_IMAGE_URI) }
    val context = LocalContext.current
    if (imageUri != EMPTY_IMAGE_URI) {
        Box(modifier = modifier) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Captured image"
            )
            Column(
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                Button(
                    onClick = {
                        val intent =
                            Intent(Intent(context, FoodImageProcessingActivity::class.java))
                        intent.putExtra("imageUri", imageUri.toString())
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = mealColors.primary // Set the background color to the primary color
                    )


                ) {
                    Text("Process Image", modifier.padding(), color = mealColors.onPrimary)
                }

                Button(
                    onClick = {
                        imageUri = EMPTY_IMAGE_URI
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = mealColors.primary // Set the background color to the primary color
                    )
                ) {
                    Text("Remove image", modifier.padding(), color = mealColors.onPrimary)
                }
            }

        }
    } else {
        var showGallerySelect by remember { mutableStateOf(false) }
        if (showGallerySelect) {
            GallerySelect(
                modifier = modifier,
                onImageUri = { uri ->
                    showGallerySelect = false
                    imageUri = uri
                }
            )
        } else {
            Box(modifier = modifier) {
                CameraCapture(
                    modifier = modifier,
                    onImageFile = { file ->
                        imageUri = file.toUri()
                    }
                )
                Button(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(4.dp),
                    onClick = {
                        showGallerySelect = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = mealColors.primary // Set the background color to the primary color
                    )
                ) {
                    Text("Select from Gallery", modifier = Modifier.padding(), color = mealColors.onPrimary)
                }
            }
        }

    }
}


@ExperimentalPermissionsApi
@Composable
fun GallerySelect(
    modifier: Modifier = Modifier,
    onImageUri: (Uri) -> Unit = { }
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                Log.d("SelectedImageUri", uri.toString())
                onImageUri(uri)

                // After selecting an image, trigger image processing immediately
                val intent = Intent(context, FoodImageProcessingActivity::class.java)
                intent.putExtra("imageUri", uri.toString())
                context.startActivity(intent)
            }
        }
    )

    @Composable
    fun LaunchGallery() {
        Log.d("Gallery Select", "Gallery Selected")
        SideEffect {
            launcher.launch("image/*")
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Permission(
            permission = Manifest.permission.ACCESS_MEDIA_LOCATION,
            rationale = "You want to read from photo gallery, so I'm going to have to ask for permission.",
            permissionNotAvailableContent = {
                Column(modifier) {
                    Text("O noes! No Photo Gallery!")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(
                            modifier = Modifier.padding(4.dp),
                            onClick = {
                                context.startActivity(
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = mealColors.primary // Set the background color to the primary color
                            )
                        ) {
                            Text("Open Settings", color = mealColors.onPrimary)
                        }
                        // If they don't want to grant permissions, this button will result in going back
                        Button(
                            modifier = Modifier.padding(4.dp),
                            onClick = {
                                onImageUri(EMPTY_IMAGE_URI)
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = mealColors.primary // Set the background color to the primary color
                            )
                        ) {
                            Text("Use Camera", color = mealColors.onPrimary)
                        }
                    }
                }
            },
        ) {
            LaunchGallery()
        }
    } else {
        LaunchGallery()
    }
}


val Context.executor: Executor
    get() = ContextCompat.getMainExecutor(this)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraCapture(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    onImageFile: (File) -> Unit = { }
) {
    val context = LocalContext.current
    Permission(
        permission = Manifest.permission.CAMERA,
        permissionNotAvailableContent = {
            Column(modifier) {
                Text("O noes! No Camera!")
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = mealColors.primary // Set the background color to the primary color
                    )
                ) {
                    Text("Open Settings", color = mealColors.onPrimary)
                }
            }
        }
    ) {
        Box(modifier = modifier) {
            val lifecycleOwner = LocalLifecycleOwner.current
            val coroutineScope = rememberCoroutineScope()
            var previewUseCase by remember { mutableStateOf<UseCase>(Preview.Builder().build()) }
            val imageCaptureUseCase by remember {
                mutableStateOf(
                    ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .build()
                )
            }
            Box {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onUseCase = {
                        previewUseCase = it
                    }
                )
                CapturePictureButton(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    onClick = {
                        coroutineScope.launch {
                            onImageFile(imageCaptureUseCase.takePicture(context.executor))
                        }
                    }
                )
            }
            LaunchedEffect(previewUseCase) {
                val cameraProvider = context.getCameraProvider()
                try {
                    // Must unbind the use-cases before rebinding them.
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, previewUseCase, imageCaptureUseCase
                    )
                } catch (ex: Exception) {
                    Log.e("CameraCapture", "Failed to bind camera use cases", ex)
                }

            }
        }
    }
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener(
            {
                val cameraProvider = future.get()
                Log.d("CameraCapture", "Camera provider is ready: $cameraProvider")
                continuation.resume(future.get())
            },
            executor
        )
    }
}

suspend fun ImageCapture.takePicture(executor: Executor): File {
    val photoFile = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            File.createTempFile("image", "jpg")
        }.getOrElse { ex ->
            Log.e("TakePicture", "Failed to create temporary file", ex)
            File("/dev/null")
        }
    }

    return suspendCoroutine { continuation ->
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        takePicture(
            outputOptions, executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    continuation.resume(photoFile)
                }

                override fun onError(ex: ImageCaptureException) {
                    Log.e("TakePicture", "Image capture failed", ex)
                    continuation.resumeWithException(ex)
                }
            }
        )
    }
}

@ExperimentalPermissionsApi
@Composable
fun Permission(
    permission: String = Manifest.permission.CAMERA,
    rationale: String = "This permission is important for this app. Please grant the permission.",
    permissionNotAvailableContent: @Composable () -> Unit = { },
    content: @Composable () -> Unit = { }
) {
    val permissionState = rememberPermissionState(permission)
    PermissionRequired(
        permissionState = permissionState,
        permissionNotGrantedContent = {
            Rationale(
                text = rationale,
                onRequestPermission = { permissionState.launchPermissionRequest() }
            )
        },
        permissionNotAvailableContent = permissionNotAvailableContent,
        content = content
    )
}

@Composable
private fun Rationale(
    text: String,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Don't */ },
        title = {
            Text(text = "Permission request")
        },
        text = {
            Text(text)
        },
        confirmButton = {
            Button(onClick = onRequestPermission) {
                Text("Ok")
            }
        }
    )
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    onUseCase: (UseCase) -> Unit = { }
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val previewView = PreviewView(context).apply {
                this.scaleType = scaleType
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            onUseCase(
                Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
            )
            previewView
        }
    )
}

@Composable
fun CapturePictureButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val color = if (isPressed) Color.Blue else Color.Black
    val contentPadding = PaddingValues(if (isPressed) 8.dp else 12.dp)
    OutlinedButton(
        modifier = modifier,
        shape = CircleShape,
        border = BorderStroke(2.dp, Color.Black),
        contentPadding = contentPadding,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
        onClick = { },
        enabled = false
    ) {
        Button(
            modifier = Modifier
                .fillMaxSize(),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = color
            ),
            interactionSource = interactionSource,
            onClick = onClick
        ) {
            // No content
        }
    }
}



val EMPTY_IMAGE_URI: Uri = Uri.parse("file://dev/null")