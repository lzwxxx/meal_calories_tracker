package com.cs461.g6.mealportiontracker.core

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cs461.g6.mealportiontracker.R
import com.cs461.g6.mealportiontracker.home.HomeNavigationActivity
import com.cs461.g6.mealportiontracker.samples.animation.*
import com.cs461.g6.mealportiontracker.samples.customview.*
import com.cs461.g6.mealportiontracker.samples.layout.*
import com.cs461.g6.mealportiontracker.samples.material.*
import com.cs461.g6.mealportiontracker.samples.stack.*
import com.cs461.g6.mealportiontracker.samples.text.*
import com.cs461.g6.mealportiontracker.samples.image.ImageActivity
import com.cs461.g6.mealportiontracker.samples.interop.ComposeInClassicAndroidActivity
import com.cs461.g6.mealportiontracker.samples.scrollers.HorizontalScrollableActivity
import com.cs461.g6.mealportiontracker.samples.scrollers.VerticalScrollableActivity
import com.cs461.g6.mealportiontracker.samples.state.ProcessDeathActivity
import com.cs461.g6.mealportiontracker.samples.state.StateActivity
import com.cs461.g6.mealportiontracker.samples.state.backpress.BackPressActivity
import com.cs461.g6.mealportiontracker.samples.state.coroutine.CoroutineFlowActivity
import com.cs461.g6.mealportiontracker.samples.state.livedata.LiveDataActivity
import com.cs461.g6.mealportiontracker.accounts.AccountNavigationActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)  // Set the theme back to AppTheme
        // startActivity(Intent(this, HomeNavigationActivity::class.java))
        startActivity(Intent(this, AccountNavigationActivity::class.java))
//        setContentView(R.layout.activity_main)
    }


    fun startSimpleTextExample() {
        startActivity(Intent(this, SimpleTextActivity::class.java))
    }

    fun startCustomTextExample() {
        startActivity(Intent(this, CustomTextActivity::class.java))
    }

    fun startVerticalScrollableExample() {
        startActivity(Intent(this, VerticalScrollableActivity::class.java))
    }

    fun startHorizontalScrollableExample() {
        startActivity(Intent(this, HorizontalScrollableActivity::class.java))
    }

    fun starLoadImageExample() {
        startActivity(Intent(this, ImageActivity::class.java))
    }

    fun startAlertDialogExample() {
        startActivity(Intent(this, AlertDialogActivity::class.java))
    }

    fun startDrawerExample() {
        startActivity(Intent(this, DrawerAppActivity::class.java))
    }

    fun startButtonsExample() {
        startActivity(Intent(this, ButtonActivity::class.java))
    }

    fun startStateExample() {
        startActivity(Intent(this, StateActivity::class.java))
    }

    fun startCustomViewExample() {
        startActivity(Intent(this, CustomViewActivity::class.java))
    }

    fun startCustomViewPaintExample() {
        startActivity(Intent(this, CustomViewPaintActivity::class.java))
    }

    fun startAutofillTextExample() {
        startActivity(Intent(this, TextFieldActivity::class.java))
    }

    fun startStackExample() {
        startActivity(Intent(this, StackActivity::class.java))
    }

    fun startViewAlignExample() {
        startActivity(Intent(this, ViewLayoutConfigurationsActivity::class.java))
    }

    fun startMaterialDesignExample() {
        startActivity(Intent(this, MaterialActivity::class.java))
    }

    fun startFixedActionButtonExample() {
        startActivity(Intent(this, FixedActionButtonActivity::class.java))
    }

    fun startConstraintLayoutExample() {
        startActivity(Intent(this, ConstraintLayoutActivity::class.java))
    }

    fun startBottomNavigationExample() {
        startActivity(Intent(this, BottomNavigationActivity::class.java))
    }

    fun startAnimation1Example() {
        startActivity(Intent(this, Animation1Activity::class.java))
    }

    fun startAnimation2Example() {
        startActivity(Intent(this, Animation2Activity::class.java))
    }

    fun startTextInlineContentExample() {
        startActivity(Intent(this, TextAnimationActivity::class.java))
    }

    fun startListAnimation() {
        startActivity(Intent(this, ListAnimationActivity::class.java))
    }

    fun startLayoutModifierExample() {
        startActivity(Intent(this, LayoutModifierActivity::class.java))
    }

    fun startProcessDeathExample() {
        startActivity(Intent(this, ProcessDeathActivity::class.java))
    }

    fun startLiveDataExample() {
        startActivity(Intent(this, LiveDataActivity::class.java))
    }

    fun startFlowRowExample() {
        startActivity(Intent(this, FlowRowActivity::class.java))
    }

    fun startShadowExample() {
        startActivity(Intent(this, ShadowActivity::class.java))
    }

    fun startCoroutineFlowExample() {
        startActivity(Intent(this, CoroutineFlowActivity::class.java))
    }

    fun startComposeWithClassicAndroidExample() {
        startActivity(Intent(this, ComposeInClassicAndroidActivity::class.java))
    }

    fun startMeasuringScaleExample() {
        startActivity(Intent(this, MeasuringScaleActivity::class.java))
    }

    fun startBackPressExample() {
        startActivity(Intent(this, BackPressActivity::class.java))
    }

    fun startZoomableExample() {
        startActivity(Intent(this, ZoomableActivity::class.java))
    }

    fun startComposeNavigationExample() {
        startActivity(Intent(this, HomeNavigationActivity::class.java))
    }
}
