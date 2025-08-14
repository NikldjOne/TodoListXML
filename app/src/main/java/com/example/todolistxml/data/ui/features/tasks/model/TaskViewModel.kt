package com.example.todolistxml.data.ui.features.tasks.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolistxml.data.remote.model.TaskDto
import com.example.todolistxml.data.repository.TaskRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskViewModel(private val repo: TaskRepository) : ViewModel() {
    private val _tasks = MutableLiveData<List<TaskDto>>(emptyList())
    private val _isLoading = MutableLiveData<Boolean>(false)
    val tasks: LiveData<List<TaskDto>> = _tasks
    val isLoading: LiveData<Boolean> = _isLoading
    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _isLoading.value = true
        _tasks.value = repo.loadAll()
        _isLoading.value = false
    }

    fun add(text: String) = viewModelScope.launch {
        val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        val newDto = TaskDto("", text, false, date)
        val created = repo.add(newDto)
        _tasks.value = _tasks.value.orEmpty() + created
        refresh()
    }

    fun edit(id: String, newText: String, checkBox: Boolean? = null) = viewModelScope.launch {

        if (id.isBlank()) return@launch
        val old = _tasks.value?.firstOrNull { it.id == id } ?: return@launch
        val update = repo.edit(old.copy(text = newText, isChecked = checkBox ?: old.isChecked))
        _tasks.value = _tasks.value!!.map { if (it.id == id) update else it }
        refresh()
    }

    fun delete(id: String) = viewModelScope.launch {
        repo.delete(id)
        _tasks.value = _tasks.value!!.filterNot { it.id == id }
        refresh()
    }

    fun getTask(id: String): TaskDto? {
        return _tasks.value?.firstOrNull { it.id == id }
    }
}