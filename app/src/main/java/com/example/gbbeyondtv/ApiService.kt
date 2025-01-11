package com.example.gbbeyondtv

// ApiService.kt
import retrofit2.Call
import retrofit2.http.GET
import com.google.gson.JsonElement

interface ApiService {
    @GET("xmltv/epg") // Adjust the endpoint as needed
    fun getChannels(): Call<JsonElement>
}