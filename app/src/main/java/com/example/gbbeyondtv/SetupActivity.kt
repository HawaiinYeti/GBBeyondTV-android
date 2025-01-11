package com.example.gbbeyondtv

// SetupActivity.kt
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class SetupActivity : GBBActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        setContentView(R.layout.activity_setup)

        val hostEditText: EditText = findViewById(R.id.ip_edit_text)
        val portEditText: EditText = findViewById(R.id.port_edit_text)
        val submitButton: Button = findViewById(R.id.submit_button)

        submitButton.setOnClickListener {
            val host = hostEditText.text.toString().trim()
            val port = portEditText.text.toString().trim()

            if (host.isNotEmpty() && port.isNotEmpty()) {
                saveHostAndPort(host, port)
                navigateToActivity(ChannelListActivity::class.java, true)
            } else {
                Toast.makeText(this, "Please enter valid IP and Port", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveHostAndPort(host: String, port: String) {
        with(sharedPreferences.edit()) {
            putString("HOST", host)
            putString("PORT", port)
            apply()
        }
    }

//    private fun navigateToChannelList() {
//        val intent = Intent(this, ChannelListActivity::class.java)
//        startActivity(intent)
//    }
}
