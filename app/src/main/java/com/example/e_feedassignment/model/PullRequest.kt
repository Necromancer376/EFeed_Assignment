package com.example.e_feedassignment.model

data class PullRequest(
    val diff_url: String,
    val html_url: String,
    val merged_at: Any,
    val patch_url: String,
    val url: String
)