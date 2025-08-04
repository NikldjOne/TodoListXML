package com.example.todolistxml.data.ui.features.tasks.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistxml.data.ui.features.tasks.model.TaskViewModel
import com.example.todolistxml.databinding.ItemTodoBinding

class TaskAdapter(private val dataset: List<TaskViewModel>) :
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {
        val foregroundCard = binding.foregroundCard
        val backgroundButtons = binding.backgroundButtons
        init {
            binding.btnEdit.setOnClickListener {

            }
            binding.btnDelete.setOnClickListener {

            }
        }

        fun bind(task: TaskViewModel) {
            binding.textView.text = task.text
            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.isChecked = task.isChecked
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(dataset[position])
    }

    override fun getItemCount(): Int = dataset.size
}

