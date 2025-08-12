package com.example.todolistxml.data.remote.api

import com.example.todolistxml.data.remote.model.TaskDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskApi {
    @GET("tasks")
    suspend fun getAll(): List<TaskDto>

    @POST("tasks")
    suspend fun create(@Body task: TaskDto): TaskDto

    @PUT("tasks/{id}")
    suspend fun update(@Path("id") id: String, @Body task: TaskDto): TaskDto

    @DELETE("tasks/{id}")
    suspend fun delete(@Path("id") id: String): Unit
}