package com.cs461.g6.mealportiontracker.foodimageprocessing

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.cs461.g6.mealportiontracker.theme.mealColors
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.FirebaseApp
import android.net.Uri
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import com.bumptech.glide.request.transition.Transition
import com.cs461.g6.mealportiontracker.R
import com.cs461.g6.mealportiontracker.accounts.AccountNavigationActivity
import com.cs461.g6.mealportiontracker.core.FirebaseAuthUtil
import com.cs461.g6.mealportiontracker.home.HomeNavigationActivity
import com.cs461.g6.mealportiontracker.theme.MealTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.RGBLuminanceSource

data class FoodInfo(
    val name: String,
    val calories: Int,
    val proteins: Int,
    val carbo: Int,
    val fats: Int
)

data class FoodInfoWithDate(
    val name: String,
    val calories: Int,
    val proteins: Int,
    val carbo: Int,
    val fats: Int,
    val date: String,
    val imageUrl: String,
    val userId: String
)

class FoodImageProcessingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        val imageUri = intent.getStringExtra("imageUri")
        if (imageUri != null) {
            setContent {
                MealTheme {
                    App(imageUri)
                }
            }
        }
    }
}

@Composable
fun App(imageUri: String) {
    val context = LocalContext.current
    var foodInfo by remember { mutableStateOf<FoodInfo?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (foodInfo != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUri),
                    contentDescription = null, // Set a meaningful content description
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                FoodInfoRow("Food Name", foodInfo!!.name)
                FoodInfoRow("Calories", foodInfo!!.calories.toString())
                FoodInfoRow("Proteins", foodInfo!!.proteins.toString())
                FoodInfoRow("Carbo", foodInfo!!.carbo.toString())
                FoodInfoRow("Fats", foodInfo!!.fats.toString())
            }

            // Add
            Button(shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    backgroundColor = mealColors.secondary,
                ),
                onClick = {
                    if (foodInfo != null) {
                        addFoodInfoToFirebase(context, foodInfo!!, imageUri)
                        // Go to HomeNavigation here
                        // Create an Intent to start HomeNavigationActivity
                        val intent =
                            Intent(Intent(context, HomeNavigationActivity::class.java))
//                        intent.putExtra("isFromFoodProcessed", true)
                        context.startActivity(intent)
                    } else {
                        mToast(context, "No Food information")
                    }
                }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Food",
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    "Add Food",
                    fontSize = 14.sp
                )
            }


            // Go back to HomeNav, don't save food
            Button(shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.White,
                    backgroundColor = mealColors.primaryVariant,
                ),
                onClick = {

                    val intent =
                        Intent(Intent(context, HomeNavigationActivity::class.java))
//                        intent.putExtra("isFromFoodProcessed", true)
                    context.startActivity(intent)
                }) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Cancel",
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    " Cancel",
                    fontSize = 14.sp
                )
            }

        } else {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }

    LaunchedEffect(key1 = Unit) {
        processFoodImage(context, imageUri) { resultFoodInfo ->
            foodInfo = resultFoodInfo
        }
    }
}

@Composable
fun FoodInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
//        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label: ",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = value,
            color = Color.Black,
            fontSize = 18.sp
        )
    }
}


private fun processFoodImage(
    context: Context,
    imageUri: String,
    resultFoodInfo: (FoodInfo) -> Unit
) {
    Glide.with(context)
        .asBitmap()
        .load(imageUri)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady( resource: Bitmap, transition: Transition<in Bitmap>?) {

                //if QR Code is detected, decode QR
                val foodInfo = QRCodeDecoder.process(resource)
                if (foodInfo != null) {
                    resultFoodInfo(foodInfo)
                }

                //if no QR Code detected, run through food model
                else
                {
                    thread {
                        try {
                            val resizedBitmap = Bitmap.createScaledBitmap(resource, 224, 224, false)
                            val moduleFileAbsoluteFilePath = assetFilePath(context, "model.pt")?.let {
                                File(it).absolutePath
                            }
                            val module = Module.load(moduleFileAbsoluteFilePath)
                            val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                                resizedBitmap,
                                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                                TensorImageUtils.TORCHVISION_NORM_STD_RGB,
                            )
                            val outputTensor =
                                module.forward(IValue.from(inputTensor)).toTuple()[0].toTensor()
                            val scores = outputTensor.dataAsFloatArray

                            var maxScore = -Float.MAX_VALUE
                            var maxScoreIdx = -1
                            for (i in scores.indices) {
                                if (scores[i] > maxScore) {
                                    maxScore = scores[i]
                                    maxScoreIdx = i
                                }
                            }

                            Log.v("ai result", maxScoreIdx.toString())
                            val foodInfo = getFoodNameAndCaloriesFromModelResult(context, maxScoreIdx)
                            resultFoodInfo(foodInfo)
                        } catch (error: Exception) {
                            error.localizedMessage?.let { Log.e("AI error", it) }
                        }
                    }
                }


            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
}


object QRCodeDecoder {
    fun process(bitmap: Bitmap): FoodInfo? {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val source: LuminanceSource = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader()

        return try {
            val result = reader.decode(binaryBitmap)
            val delimiter = ","
            val parts = result.text.split(delimiter)

            if(parts.size != 5) {
                null
            } else {
                val foodName = parts[0]
                val calories = parts[1].toInt()
                val proteins = parts[2].toInt()
                val carbo = parts[3].toInt()
                val fats = parts[4].toInt()

                FoodInfo(foodName, calories, proteins, carbo, fats)
            }
        } catch (e: Exception) {
            null //Not a QR code or QR code format is not valid
        }
    }
}






private fun getFoodNameAndCaloriesFromModelResult(context: Context, score: Int): FoodInfo {
    lateinit var jsonString: String
    try {
        jsonString = context.assets.open("food_and_calories.json")
            .bufferedReader()
            .use { it.readText() }
    } catch (ioException: IOException) {
        ioException.localizedMessage?.let { Log.e("error", it) }
    }

    val listType = object : TypeToken<List<String>>() {}.type
    val foodList: List<String> = Gson().fromJson(jsonString, listType)
    val foodData = foodList[score].split(": ")
    val foodName = foodData[0]
    val nutrientValues = foodData[1].split(", ").map { it.toInt() }

    if (nutrientValues.size == 4) {
        val calories = nutrientValues[0]
        val proteins = nutrientValues[1]
        val carbo = nutrientValues[2]
        val fats = nutrientValues[3]

        return FoodInfo(foodName, calories, proteins, carbo, fats)
    } else {
        // Handle invalid data in the JSON
        return FoodInfo("", 0, 0, 0, 0)
    }
}

private fun assetFilePath(context: Context, assetName: String): String? {
    val file = File(context.filesDir, assetName)
    if (file.exists() && file.length() > 0) {
        return file.absolutePath
    }
    try {
        context.assets.open(assetName).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            return file.absolutePath
        }
    } catch (e: IOException) {
        Log.e(
            "Error",
            "Error processing asset $assetName to a file path"
        )
    }
    return null
}

private fun addFoodInfoToFirebase(context: Context, foodInfo: FoodInfo, imageUri: String) {
    val storageReference = FirebaseStorage.getInstance().reference
    val databaseReference = FirebaseDatabase.getInstance().getReference("meal_histories")

    // Generate a new unique key for the data
    val foodInfoKey = databaseReference.push().key

    // Format the date in "dd/MM/yyyy" format
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = dateFormat.format(Date())

    val currentUser = FirebaseAuthUtil.getCurrentUser() // Get the currently signed-in user

    if (currentUser != null) {
        if (foodInfoKey != null) {
            // First, upload the image to Firebase Storage
            val imageRef = storageReference.child("images/$foodInfoKey.jpg")
            val uploadTask = imageRef.putFile(Uri.parse(imageUri))

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                // Continue with the task to get the download URL
                imageRef.downloadUrl
            }.addOnCompleteListener { downloadUrlTask ->
                if (downloadUrlTask.isSuccessful) {
                    val downloadUri = downloadUrlTask.result

                    if (downloadUri != null) {
                        // Now, create a FoodInfoWithDate object with the image URL
                        val foodInfoWithDate = FoodInfoWithDate(
                            name = foodInfo.name.toTitleCase(),
                            calories = foodInfo.calories,
                            proteins = foodInfo.proteins,
                            carbo = foodInfo.carbo,
                            fats = foodInfo.fats,
                            date = date,
                            imageUrl = downloadUri.toString(),
                            userId = currentUser.uid // Include the user's ID
                        )

                        // Add the FoodInfoWithDate object to the database under the generated key
                        databaseReference.child(foodInfoKey).setValue(foodInfoWithDate)
                            .addOnCompleteListener { saveTask ->
                                if (saveTask.isSuccessful) {
                                    // Data was successfully saved to the database
                                    // You can add any further logic here if needed
                                    mToast(context, "Food information added to the database!")
                                } else {
                                    // Handle database save failure
                                    val saveException = saveTask.exception
                                    mToast(context, "Failed to save food information: ${saveException?.message}")
                                }
                            }
                    } else {
                        mToast(context, "Download URL is null.")
                    }
                } else {
                    // Handle failure to get the image URL
                    mToast(context, "Failed to get image URL: ${downloadUrlTask.exception?.message}")
                }
            }
        } else {
            mToast(context, "Failed to generate a unique key for data.")
        }
    } else {
        mToast(context, "User is not authenticated.")
    }
}

fun String.toTitleCase(): String {
    return split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}


fun mToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
