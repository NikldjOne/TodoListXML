package com.example.todolistxml.data.ui.features.tasks.adapter

interface TaskActionListener{
    fun onDelete(id: String)
    fun onEdit(id: String)
    fun onCheckBox(id: String)
}