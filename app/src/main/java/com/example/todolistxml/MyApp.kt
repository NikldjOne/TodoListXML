package com.example.todolistxml

import android.app.Application
import com.example.todolistxml.data.remote.api.TaskApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApp: Application() {
    companion object{
        lateinit var taskApi: TaskApi
    }

    override fun onCreate() {
        super.onCreate()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://6894cf81be3700414e149839.mockapi.io/todoList/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        taskApi = retrofit.create(TaskApi::class.java)
    }
}