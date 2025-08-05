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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.todolistxml.data.ui.features.tasks.adapter.TaskActionListener
import com.example.todolistxml.data.ui.features.tasks.adapter.TaskAdapter
import com.example.todolistxml.data.ui.features.tasks.model.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), TaskActionListener {
    private lateinit var binding: ActivityMainBinding
    private val tasks = mutableListOf<TaskViewModel>()
    private val adapter by lazy { TaskAdapter(tasks, this) }
    private var isKeyboardVisibleNow: Boolean = false
    private val imm by lazy { getSystemService(InputMethodManager::class.java) }
    private val swipeRefreshLayout: SwipeRefreshLayout by lazy {
        binding.swipeRefreshLayout
    }

    private var isEditMode = false
    private var positionEdit = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsetsListener()
        setupRecycler()
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
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
        }
        binding.recyclerTask.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
        val dividerItemDecoration = DividerItemDecoration(
            binding.recyclerTask.context,
            LinearLayoutManager.VERTICAL
        )
        binding.recyclerTask.addItemDecoration(dividerItemDecoration)

        setItemTouchHelper()
    }

    override fun onDelete(position: Int) {
        tasks.removeAt(position)
        adapter.notifyItemRemoved(position)
    }

    override fun onEdit(position: Int) {
        showEditor()
        binding.editText.setText(tasks[position].text)
        positionEdit = position
        isEditMode = true
    }

    private fun addTask() {
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        tasks.add(TaskViewModel(binding.editText.text.toString(), false, formatter.format(Date())))
        adapter.notifyItemInserted(tasks.size - 1)
        imm.hideSoftInputFromWindow(binding.editText.windowToken, 0)
        binding.floatingEditContainer.visibility = View.GONE
        binding.containerOpacity.visibility = View.GONE
        binding.editText.setText("")
    }

    private fun editTask() {
        tasks[positionEdit].text = binding.editText.text.toString()
        adapter.notifyItemChanged(positionEdit)
        binding.floatingEditContainer.visibility = View.GONE
        binding.containerOpacity.visibility = View.GONE
        imm.hideSoftInputFromWindow(binding.editText.windowToken, 0)
        offEditMode()
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
                    if(isEditMode){
                        offEditMode()
                    }
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
    private fun offEditMode () {
        isEditMode = false
        positionEdit = 0
        binding.editText.setText("")
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
                if (isEditMode) {
                    editTask()
                } else {
                    addTask()
                }
            }
        }
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

    private fun setItemTouchHelper() {
        ItemTouchHelper(object : ItemTouchHelper.Callback() {

            private var openedViewHolder: TaskAdapter.ViewHolder? = null
            private var currentScrollX = 0
            private var currentScrollXWhenInActive = 0
            private var initXWhenInActive = 0f
            private var firstInActive = false

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = 0
                val swipeFlags = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                return makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = true

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                return Integer.MAX_VALUE.toFloat()
            }

            override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
                return Integer.MAX_VALUE.toFloat()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val holder = viewHolder as TaskAdapter.ViewHolder
                val limitScrollX = viewHolder.backgroundButtons.width

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    if (openedViewHolder != null && openedViewHolder != holder) {
                        openedViewHolder?.closeItemView()
                        openedViewHolder = null
                    }
                    if (dX == 0f) {
                        currentScrollX = viewHolder.itemView.scrollX
                        firstInActive = true
                    }
                    if (isCurrentlyActive) {
                        //swipe with finger
                        var scrollOffset = (currentScrollX + (-dX)).toInt()
                        if (scrollOffset > limitScrollX) {
                            scrollOffset = limitScrollX
                        } else if (scrollOffset < 0) {
                            scrollOffset = 0
                        }
                        viewHolder.itemView.scrollTo(scrollOffset, 0)
                    } else {
                        if (firstInActive) {
                            firstInActive = false
                            currentScrollXWhenInActive = viewHolder.itemView.scrollX
                            initXWhenInActive = dX
                        }
                        if (viewHolder.itemView.scrollX < limitScrollX) {
                            viewHolder.itemView.scrollTo(
                                (currentScrollXWhenInActive * dX / initXWhenInActive).toInt(),
                                0
                            )
                        }
                    }
                    if (viewHolder.itemView.scrollX > 0) {
                        openedViewHolder = holder
                    }
                }
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)
                val holder = viewHolder as TaskAdapter.ViewHolder
                val limitScrollX = viewHolder.backgroundButtons.width

                if (viewHolder.itemView.scrollX > limitScrollX) {
                    viewHolder.itemView.scrollTo(limitScrollX, 0)
                } else if (viewHolder.itemView.scrollX < 0) {
                    viewHolder.itemView.scrollTo(0, 0)
                }
                if (viewHolder.itemView.scrollX == 0 && openedViewHolder == viewHolder) {
                    openedViewHolder = null
                }
            }

        }).apply { attachToRecyclerView(binding.recyclerTask) }
    }
}