package com.example.e_feedassignment.model

data class APIResponse(
    val incomplete_results: Boolean,
    val items: MutableList<Issue>,
    val total_count: Int
)