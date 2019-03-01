package com.reyzeny.phoneauth_firebase

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.FragmentManager

class GlobalFunctions {
    companion object {
        fun changeStatusBarColor(activity: Activity){
            if (Build.VERSION.SDK_INT >= 21) {
                activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = activity.window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
            }
        }

        fun showAuthFragment(container: Int, fragmentManager: FragmentManager?, fragment_tag: String) {
            val authFragment = AuthFragment()
            val bundle = Bundle()
            bundle.putInt(Constant.CONTAINER_ID, container)
            bundle.putString(Constant.TAG, fragment_tag)
            authFragment.arguments = bundle
            fragmentManager?.beginTransaction()?.replace(container, authFragment)?.commit()
        }
    }
}