package com.example.gbbeyondtv

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Process

open class GBBActivity : AppCompatActivity() {
    fun navigateToActivity(activity: Class<*>, clearStack: Boolean = false) {
        val intent = Intent(this, activity)
        if (clearStack) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

    }

    fun restartApp(context: Context) {
        val intent = Intent(context, SetupActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        Process.killProcess(Process.myPid()) // Kill the current process
    }
}