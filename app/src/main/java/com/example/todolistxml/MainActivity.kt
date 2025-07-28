package com.example.todolistxml

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.todolistxml.databinding.ActivityMainBinding
import androidx.core.view.isGone
import androidx.core.view.updatePadding
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todolistxml.data.ui.features.tasks.adapter.TaskAdapter
import com.example.todolistxml.data.ui.features.tasks.model.TaskViewModel

class MainActivity : AppCompatActivity() {
   private lateinit var binding: ActivityMainBinding
   private val mutableList: MutableList<TaskViewModel> = mutableListOf()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imm = getSystemService(InputMethodManager::class.java)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            view.updatePadding(bottom = navBarHeight)
            insets
        }

        val fab = binding.floatingActionButton
        val editText = binding.editText
        val rootView = binding.rootContainer
        val floatingEditContainer = binding.floatingEditContainer
        val observer = KeyboardVisibilityObserver(binding.root)
        lifecycle.addObserver(observer)
        binding.recyclerTask.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(
            mutableList,
        )
        binding.recyclerTask.adapter = adapter

        observer.isKeyboardVisible.observe(this) { isVisible ->
            if (isVisible && floatingEditContainer.isVisible) {
                floatingEditContainer.translationY = 0f
            } else {
                val rootHeight = rootView.height
                val viewHeight = floatingEditContainer.height
                Log.d("Keyboard", "Клавиатура закрыта")
                floatingEditContainer.translationY = ((rootHeight/2)-viewHeight/2).toFloat()

            }
        }

        fab.setOnClickListener {
            if(floatingEditContainer.isGone){
                floatingEditContainer.visibility = View.VISIBLE
                editText.requestFocus()
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                fab.setImageResource(R.drawable.ic_check)
                rootView.setBackgroundColor(getResources().getColor(R.color.backgroundDark, null))

            }else{
                mutableList.add(TaskViewModel(editText.text.toString(), false))
                adapter.notifyItemInserted(mutableList.size - 1)
                imm.hideSoftInputFromWindow(editText.windowToken, 0)
                floatingEditContainer.visibility = View.GONE
                fab.setImageResource(R.drawable.ic_add)
                rootView.setBackgroundColor(getResources().getColor(R.color.background, null))
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(rootView) {_, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val containerHeight = floatingEditContainer.height

            if(imeVisible){
                floatingEditContainer.visibility = View.VISIBLE
            }else{
                floatingEditContainer.visibility = View.GONE
            }
            insets
        }
    }
}