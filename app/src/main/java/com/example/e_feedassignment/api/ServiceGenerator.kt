package com.example.e_feedassignment.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ServiceGenerator {

    val URL = "https://api.github.com/"

    val api = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(APIService::class.java)
}