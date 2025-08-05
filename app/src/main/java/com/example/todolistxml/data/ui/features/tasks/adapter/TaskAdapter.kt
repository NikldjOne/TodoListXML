package com.example.todolistxml.data.ui.features.tasks.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistxml.data.ui.features.tasks.model.TaskViewModel
import com.example.todolistxml.databinding.ItemTodoBinding

class TaskAdapter(private val dataset: List<TaskViewModel>) :
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    private val openedPositions = mutableSetOf<Int>()

    fun isOpened(position: Int): Boolean = openedPositions.contains(position)

    fun setOpened(position: Int, opened: Boolean) {
        if (opened) openedPositions.add(position)
        else openedPositions.remove(position)
    }

    class ViewHolder(private val binding: ItemTodoBinding) : RecyclerView.ViewHolder(binding.root) {
        val foregroundCard = binding.foregroundCard
        val backgroundButtons = binding.backgroundButtons

        init {
            binding.backgroundButtons.bringToFront()
            binding.btnEdit.setOnClickListener {
                AlertDialog.Builder(binding.root.context)                // this — ваш Context (Activity)
                    .setTitle("Заголовок").setMessage("Сообщение в диалоге")
                    .setPositiveButton("ОК") { dialog, _ ->
                        // обработка нажатия на кнопку ОК
                        dialog.dismiss()
                    }.setNegativeButton("Отмена") { dialog, _ ->
                        // обработка нажатия на кнопку Отмена
                        dialog.dismiss()
                    }
                    .setCancelable(true)                  // можно закрыть по тапу вне диалога или кнопкой «Назад»
                    .show()
            }
            binding.btnDelete.setOnClickListener {

            }
            foregroundCard.setOnClickListener {
                if (foregroundCard.scrollX != 0) {
                    foregroundCard.scrollTo(0, 0)
                }
            }
        }

        fun bind(task: TaskViewModel) {
            binding.textView.text = task.text
            binding.checkBox.setOnCheckedChangeListener(null)
            binding.checkBox.isChecked = task.isChecked

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.foregroundCard.scrollTo(0, 0)
        holder.bind(dataset[position])
    }


    override fun getItemCount(): Int = dataset.size
}

