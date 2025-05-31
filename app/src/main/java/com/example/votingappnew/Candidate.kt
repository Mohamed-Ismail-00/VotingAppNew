package com.example.votingappnew.models

data class Candidate(
    val id: String,
    val name: String,
    val age: Long,
    val totalVotes: Long,
    val imageUrl: String?
)
