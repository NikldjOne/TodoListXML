package com.example.todolistxml.data.ui.features.tasks.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistxml.data.ui.features.tasks.model.TaskViewModel
import com.example.todolistxml.databinding.ItemTodoBinding


class TaskAdapter(
    private val dataset: MutableList<TaskViewModel>,
    private val listener: TaskActionListener
) :
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {
        val foregroundCard = binding.foregroundCard
        val backgroundButtons = binding.backgroundButtons

        fun closeItemView() {
            if (itemView.scrollX != 0) {
                ObjectAnimator.ofInt(itemView, "scrollX", itemView.scrollX, 0)
                    .setDuration(300)
                    .start()
            }
        }

        init {
            binding.backgroundButtons.bringToFront()

            itemView.setOnClickListener {
                if (itemView.scrollX != 0) {
                    closeItemView()
                }
            }
        }

        fun bind(task: TaskViewModel, onDeleteClick: () -> Unit, onEditClick: () -> Unit) {
            binding.textView.text = task.text
            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.isChecked = task.isChecked
            binding.textViewDate.text = task.date
            binding.btnEdit.setOnClickListener {
                closeItemView()
            }
            binding.btnDelete.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    closeItemView()
                    onDeleteClick()
                }
            }
            binding.btnEdit.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    closeItemView()
                    onEditClick()
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.scrollTo(0, 0)
        holder.bind(
            dataset[position], onDeleteClick = {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onDelete(pos)
                }
            },
            onEditClick = {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onEdit(pos)
                }
            })
    }


    override fun getItemCount(): Int = dataset.size
}

