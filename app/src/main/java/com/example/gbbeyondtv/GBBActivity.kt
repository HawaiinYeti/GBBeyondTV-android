package com.example.gbbeyondtv

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

open class GBBActivity : AppCompatActivity() {
    fun navigateToActivity(activity: Class<*>, clearStack: Boolean = false) {
        val intent = Intent(this, activity)
        if (clearStack) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

    }
}