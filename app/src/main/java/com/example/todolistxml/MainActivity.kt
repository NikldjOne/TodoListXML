package com.example.todolistxml

import android.graphics.Canvas
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.todolistxml.databinding.ActivityMainBinding
import androidx.core.view.isGone
import androidx.core.view.updatePadding
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todolistxml.data.ui.features.tasks.adapter.TaskAdapter
import com.example.todolistxml.data.ui.features.tasks.model.TaskViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val tasks = mutableListOf<TaskViewModel>()
    private val adapter by lazy { TaskAdapter(tasks) }
    private var isKeyboardVisibleNow: Boolean = false
    private val imm by lazy { getSystemService(InputMethodManager::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsetsListener()
        setupRecycler()
        attachSwipeHandler()
        setupKeyboardObserver()
        setupBackCallback()
        setupTextWatcher()
        setupFab()
        setupIconBtn()
        setupContainerOpacity()
    }

    private fun setupInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            view.updatePadding(bottom = navBarHeight)
            insets
        }
    }

    private fun setupRecycler() {
        binding.recyclerTask.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupKeyboardObserver() {
        val observer = KeyboardVisibilityObserver(binding.root)
        lifecycle.addObserver(observer)
        observer.isKeyboardVisible.observe(this) { isVisible ->
            isKeyboardVisibleNow = isVisible
            if (isVisible && binding.floatingEditContainer.isVisible) {
                binding.floatingEditContainer.translationY = 0f
            } else {
                centerFloatingContainer()
            }
        }
    }

    private fun centerFloatingContainer() {
        val rootHeight = binding.rootContainer.height
        val viewHeight = binding.floatingEditContainer.height
        binding.floatingEditContainer.translationY = ((rootHeight / 2) - viewHeight / 2).toFloat()
    }

    private fun setupBackCallback() {
        onBackPressedDispatcher.addCallback(this) {
            when {
                isKeyboardVisibleNow -> {
                    imm.hideSoftInputFromWindow(binding.editText.windowToken, 0)
                }

                binding.floatingEditContainer.isVisible -> {
                    hideEditor()
                }

                else -> finish()
            }
        }
    }

    private fun hideEditor() {
        binding.floatingEditContainer.visibility = View.GONE
        binding.containerOpacity.visibility = View.GONE
        binding.floatingActionButton.setImageResource(R.drawable.ic_add)
    }

    private fun setupTextWatcher() {
        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(c: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun onTextChanged(c: CharSequence?, start: Int, before: Int, count: Int) {
                binding.iconBtn.isEnabled = count > 0
            }
        })
    }

    private fun setupFab() {
        binding.floatingActionButton.setOnClickListener {
            if (binding.floatingEditContainer.isGone) {
                showEditor()
            } else {
                addTask()
            }
        }
    }

    private fun showEditor() {
        binding.floatingEditContainer.visibility = View.VISIBLE
        binding.containerOpacity.visibility = View.VISIBLE
        binding.editText.requestFocus()
        imm.showSoftInput(binding.editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setupIconBtn() {
        binding.iconBtn.setOnClickListener {
            if (binding.editText.text.isNotEmpty()) {
                addTask()
            }
        }
    }

    private fun addTask() {
        tasks.add(TaskViewModel(binding.editText.text.toString(), false))
        adapter.notifyItemInserted(tasks.size - 1)
        imm.hideSoftInputFromWindow(binding.editText.windowToken, 0)
        binding.floatingEditContainer.visibility = View.GONE
        binding.containerOpacity.visibility = View.GONE
        binding.editText.setText("")
    }

    private fun setupContainerOpacity() {
        binding.containerOpacity.setOnClickListener {
            if (binding.containerOpacity.isVisible) {
                binding.containerOpacity.visibility = View.GONE
                binding.floatingEditContainer.visibility = View.GONE
                imm.hideSoftInputFromWindow(binding.editText.windowToken, 0)
                binding.editText.setText("")
            }
        }
    }

    private fun attachSwipeHandler() {
        val callback = object : ItemTouchHelper.Callback() {
            // флаг: начался ли swipe
            private var isSwiping = false

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                // свайп только влево и вправо
                val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                return makeMovementFlags(0, swipeFlags)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = true

            // Мы не удаляем элемент из списка по завершении свайпа:
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            // здесь узнаём, что жест именно свайп, а не клик
            override fun onSelectedChanged(vh: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(vh, actionState)
                isSwiping = actionState == ItemTouchHelper.ACTION_STATE_SWIPE
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,    // текущее смещение по X
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val holder = viewHolder as TaskAdapter.ViewHolder
                val buttonsWidth = holder.backgroundButtons.width.toFloat()

                // Зажимаем смещение между –buttonsWidth и +buttonsWidth—но если вам
                // нужен только влево, то используйте coerceIn(-buttonsWidth, 0f)
                val clampedDx = dX.coerceIn(-buttonsWidth, 0f)

                // Применяем translationX к карточке и к кнопкам
                holder.foregroundCard.translationX = clampedDx
                holder.backgroundButtons.translationX = clampedDx
            }

            // После того, как палец отпущен и жест завершён, clearView вызывается
            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                if (!isSwiping) return       // если не было swipe → ничего не делаем

                val holder = viewHolder as TaskAdapter.ViewHolder
                val buttonsWidth = holder.backgroundButtons.width.toFloat()
                val current = holder.foregroundCard.translationX

                // фиксируем только после свайпа
                val target = if (current <= -buttonsWidth/2) -buttonsWidth else 0f
                holder.foregroundCard
                    .animate().translationX(target).setDuration(150).start()
                holder.backgroundButtons
                    .animate().translationX(target).setDuration(150).start()

                isSwiping = false
            }

            // Важный момент: по умолчанию для того, чтобы фиксация «onSwiped»
            // не удаляла элемент, достаточно вернуть 1.0:
            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 1.0f
        }

        ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerTask)
    }
}