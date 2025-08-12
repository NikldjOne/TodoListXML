package com.example.todolistxml.data.repository

import com.example.todolistxml.data.remote.api.TaskApi
import com.example.todolistxml.data.remote.model.TaskDto

class TaskRepository(private val api: TaskApi) {

    suspend fun loadAll(): List<TaskDto> = api.getAll()

    suspend fun add(dto: TaskDto): TaskDto = api.create(dto)

    suspend fun edit(dto: TaskDto): TaskDto = api.update(dto.id,dto)

    suspend fun delete(id: String) = api.delete(id)
}