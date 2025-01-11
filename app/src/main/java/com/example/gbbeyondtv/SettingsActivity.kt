package com.example.gbbeyondtv

import android.os.Bundle
import android.widget.Button
import android.content.Context
import android.content.SharedPreferences

class SettingsActivity : GBBActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        val clearButton: Button = findViewById(R.id.clear_button)
        val backButton: Button = findViewById(R.id.back_button)

        clearButton.setOnClickListener {
            clearHostAndPort()
            navigateToActivity(SetupActivity::class.java)
        }

        backButton.setOnClickListener {
            navigateToActivity(ChannelListActivity::class.java)
        }
    }

    private fun clearHostAndPort() {
        sharedPreferences.edit().remove("HOST").remove("PORT").apply()
    }
}