package com.example.todolistxml.data.remote.model

data class TaskDto(
    val id: String,
    var text: String,
    val isChecked: Boolean,
    val date: String
)