package com.example.e_feedassignment.api

import com.example.e_feedassignment.model.APIResponse
import com.example.e_feedassignment.model.Issue
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface APIService {
    @GET("search/issues")
    suspend fun getRepoIssues(
        @Header("Authorization") authToken: String,
        @Query("q") q: String
    ): Response<APIResponse>
}

//sample query
//issues?q=something%20repo:torvalds/linux%20state:closed