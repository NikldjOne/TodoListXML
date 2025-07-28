package com.example.todolistxml

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class KeyboardVisibilityObserver(
    private val rootView: View
) : DefaultLifecycleObserver {

    private val _isKeyboardVisible = MutableLiveData(false)
    val isKeyboardVisible: LiveData<Boolean> get() = _isKeyboardVisible

    private var lastVisible = false

    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = rootView.rootView.height

        val keypadHeight = screenHeight - rect.bottom
        val isOpenNow = keypadHeight > screenHeight * 0.15

        if (isOpenNow != lastVisible) {
            lastVisible = isOpenNow
            _isKeyboardVisible.value = isOpenNow
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        rootView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    override fun onPause(owner: LifecycleOwner) {
        rootView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
    }
}
