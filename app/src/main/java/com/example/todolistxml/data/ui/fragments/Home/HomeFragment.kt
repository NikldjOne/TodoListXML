package com.example.todolistxml.data.ui.fragments.Home

import android.graphics.Canvas
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.todolistxml.KeyboardVisibilityObserver
import com.example.todolistxml.MyApp
import com.example.todolistxml.R
import com.example.todolistxml.data.remote.model.TaskDto
import com.example.todolistxml.data.repository.TaskRepository
import com.example.todolistxml.data.ui.features.tasks.adapter.TaskActionListener
import com.example.todolistxml.data.ui.features.tasks.adapter.TaskAdapter
import com.example.todolistxml.data.ui.features.tasks.model.TaskViewModel
import com.example.todolistxml.data.ui.features.tasks.model.TaskViewModelFactory
import com.example.todolistxml.databinding.FragmentHomeBinding
import kotlin.getValue
import androidx.fragment.app.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText


class HomeFragment : Fragment(), TaskActionListener {
    private lateinit var binding: FragmentHomeBinding
    private val tasks = mutableListOf<TaskDto>()
    private val adapter by lazy { TaskAdapter(tasks, this) }
    private var isKeyboardVisibleNow: Boolean = false
    private val imm by lazy { requireActivity().getSystemService(InputMethodManager::class.java) }
    private lateinit var backCallback: OnBackPressedCallback
    private val swipeRefreshLayout: SwipeRefreshLayout by lazy {
        binding.swipeRefreshLayout
    }
    private val viewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(TaskRepository(MyApp.taskApi))
    }
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var iconBtn: FloatingActionButton
    private lateinit var containerLoading: LinearLayout
    private lateinit var editText: EditText
    private lateinit var floatingEditContainer: LinearLayout
    private lateinit var containerOpacity: LinearLayout
    private lateinit var bottom_navigation: BottomNavigationView


    private var isEditMode = false
    private var positionEdit = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        floatingActionButton = requireActivity().findViewById(R.id.floatingActionButton)
        containerLoading = requireActivity().findViewById(R.id.container_loading)
        editText = requireActivity().findViewById(R.id.editText)
        floatingEditContainer = requireActivity().findViewById(R.id.floatingEditContainer)
        containerOpacity = requireActivity().findViewById(R.id.containerOpacity)
        iconBtn = requireActivity().findViewById(R.id.iconBtn)
        bottom_navigation = requireActivity().findViewById(R.id.bottom_navigation)

        setupInsetsListener()
        setupRecycler()
        setupKeyboardObserver()
        setupTextWatcher()
        setupFab()
        setupIconBtn()
        setupContainerOpacity()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
    }

    private fun setupInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            view.updatePadding(bottom = navBarHeight)
            insets
        }
    }

    private fun setupRecycler() {
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            containerLoading.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.tasks.observe(viewLifecycleOwner) { dtos ->
            tasks.clear()
            tasks.addAll(dtos.map { TaskDto(it.id, it.text, it.isChecked, it.date) })
            adapter.notifyDataSetChanged()
        }

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
        }
        binding.recyclerTask.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
        }
        val dividerItemDecoration = DividerItemDecoration(
            binding.recyclerTask.context,
            LinearLayoutManager.VERTICAL
        )
        binding.recyclerTask.addItemDecoration(dividerItemDecoration)

        setItemTouchHelper()
    }

    override fun onDelete(id: String) {
        val dto = viewModel.getTask(id) ?: return
        viewModel.delete(dto.id)
    }

    override fun onEdit(id: String) {
        showEditor()
        val dto = viewModel.getTask(id)
        editText.setText(dto?.text)
        editText.setSelection(editText.text?.length ?: 0)
        positionEdit = dto?.id ?: ""
        isEditMode = true
    }

    override fun onCheckBox(id: String) {
        val dto = viewModel.getTask(id) ?: return
        viewModel.edit(id, dto.text, !dto.isChecked)
    }

    private fun addTask() {
        val text = editText.text.toString()
        if (text.isNotBlank()) {
            viewModel.add(text)
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
            floatingEditContainer.visibility = View.GONE
            containerOpacity.visibility = View.GONE
            editText.setText("")
        }
    }

    private fun editTask() {
        val text = editText.text.toString()
        viewModel.edit(positionEdit, text)
        floatingEditContainer.visibility = View.GONE
        containerOpacity.visibility = View.GONE
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
        offEditMode()
    }


    private fun setupKeyboardObserver() {
        val observer = KeyboardVisibilityObserver(binding.root)
        lifecycle.addObserver(observer)
        observer.isKeyboardVisible.observe(viewLifecycleOwner) { isVisible ->
            isKeyboardVisibleNow = isVisible
            if (isVisible && floatingEditContainer.isVisible) {
                floatingEditContainer.translationY = 0f
            } else {
                centerFloatingContainer()
            }
        }
    }

    private fun centerFloatingContainer() {
        val rootHeight = binding.rootContainer.height
        val viewHeight = floatingEditContainer.height
        val bottomBar = bottom_navigation.height
        floatingEditContainer.translationY = (((rootHeight / 2) + bottomBar/2) - viewHeight / 2).toFloat()
    }

    private fun handleBackPress() {

        when {
            isKeyboardVisibleNow -> {
                imm.hideSoftInputFromWindow(editText.windowToken, 0)
            }

            floatingEditContainer.isVisible -> {
                hideEditor()
                if (isEditMode) {
                    offEditMode()
                }
            }

            else -> {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun hideEditor() {
        floatingEditContainer.visibility = View.GONE
        containerOpacity.visibility = View.GONE
        floatingActionButton.setImageResource(R.drawable.ic_add)
    }

    private fun offEditMode() {
        isEditMode = false
        positionEdit = ""
        editText.setText("")
    }

    private fun setupTextWatcher() {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(c: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun onTextChanged(c: CharSequence?, start: Int, before: Int, count: Int) {
                iconBtn.isEnabled = count > 0
            }
        })
    }

    private fun setupFab() {
        floatingActionButton.setOnClickListener {
            if (floatingEditContainer.isGone) {
                showEditor()
            } else {
                addTask()
            }
        }
    }

    private fun showEditor() {
        floatingEditContainer.visibility = View.VISIBLE
        containerOpacity.visibility = View.VISIBLE
        editText.requestFocus()
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setupIconBtn() {
        iconBtn.setOnClickListener {
            if (editText.text?.isNotEmpty() ?: false) {
                if (isEditMode) {
                    editTask()
                } else {
                    addTask()
                }
            }
        }
    }


    private fun setupContainerOpacity() {
        containerOpacity.setOnClickListener {
            if (containerOpacity.isVisible) {
                containerOpacity.visibility = View.GONE
                floatingEditContainer.visibility = View.GONE
                imm.hideSoftInputFromWindow(editText.windowToken, 0)
                editText.setText("")
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