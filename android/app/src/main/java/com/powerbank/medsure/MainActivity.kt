package com.powerbank.medsure

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.powerbank.medsure.ui.navigation.MedSureNavHost
import com.powerbank.medsure.ui.theme.MedSureTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MedSureTheme {
                MedSureNavHost()
            }
        }
    }
}
