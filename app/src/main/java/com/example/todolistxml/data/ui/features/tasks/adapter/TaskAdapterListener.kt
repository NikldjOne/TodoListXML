package com.example.todolistxml.data.ui.features.tasks.adapter

interface TaskActionListener{
    fun onDelete(position: Int)
    fun onEdit(position: Int)
}