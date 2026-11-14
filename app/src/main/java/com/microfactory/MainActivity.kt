package com.microfactory

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.microfactory.ui.FactoryGameScreen
import com.microfactory.ui.MicroFactoryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.parseColor("#12161F")),
            navigationBarStyle = SystemBarStyle.dark(Color.parseColor("#12161F"))
        )
        setContent {
            MicroFactoryTheme {
                FactoryGameScreen()
            }
        }
    }
}
