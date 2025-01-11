package com.example.gbbeyondtv

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

open class GBBActivity : AppCompatActivity() {
    fun navigateToActivity(activity: Class<*>) {
        val intent = Intent(this, activity)
        startActivity(intent)
    }
}