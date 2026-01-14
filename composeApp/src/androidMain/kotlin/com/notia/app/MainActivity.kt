package com.notia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.notia.app.navigation.NotiaNavGraph
import com.notia.app.ui.theme.NotiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContextProvider.context = applicationContext
        
        // Force High Refresh Rate (120Hz+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val display = this.display
            display?.let {
                val supportedModes = it.supportedModes
                val maxRefreshRateMode = supportedModes.maxByOrNull { mode -> mode.refreshRate }
                maxRefreshRateMode?.let { maxMode ->
                    val layoutParams = window.attributes
                    layoutParams.preferredDisplayModeId = maxMode.modeId
                    window.attributes = layoutParams
                }
            }
        }
        
        enableEdgeToEdge()
        setContent {
            App(context = applicationContext)
        }
    }
}
