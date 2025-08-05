package com.example.todolistxml

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.todolistxml.data.ui.features.tasks.adapter.TaskAdapter
import com.example.todolistxml.data.ui.features.tasks.model.TaskViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val tasks = mutableListOf<TaskViewModel>()
    private val adapter by lazy { TaskAdapter(tasks) }
    private var isKeyboardVisibleNow: Boolean = false
    private val imm by lazy { getSystemService(InputMethodManager::class.java) }
    private val swipeRefreshLayout: SwipeRefreshLayout by lazy {
        binding.swipeRefreshLayout
    }

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
        setItemTouchHelper()
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

    private fun setItemTouchHelper() {
        ItemTouchHelper(object : ItemTouchHelper.Callback() {

//            private val limitScrollX = holder.backgroundButtons.width.toFloat() // tidi
            private var currentScrollX = 0
            private var currentScrollWhenInActive = 0
            private var initXWhenActive = 0f
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
//            private val limitScrollX = recyclerView.backgroundButtons.width.toFloat() // tidi

                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    if(dX == 0f){
                        currentScrollX = viewHolder.itemView.scrollX
                        firstInActive = true
                    }
                    if(isCurrentlyActive){
                        //swipe with finger
                        val scrollOfset = currentScrollX + (-dX).toInt()
                        if(scrollOfset > limit)
                    }else{

                    }
                }
            }

        }).apply { attachToRecyclerView(binding.recyclerTask) }
    }

    private fun dipToPx(dipValue: Float,context: Context): Int {
        return (dipValue * context.resources.displayMetrics.density).toInt()
    }
}