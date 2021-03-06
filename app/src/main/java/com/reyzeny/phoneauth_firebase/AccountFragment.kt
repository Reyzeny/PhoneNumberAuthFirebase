package com.reyzeny.phoneauth_firebase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.reyzeny.phoneauth_firebase.GlobalFunctions.Companion.showAuthFragment

class AccountFragment: Fragment() {
    private var home_view: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!LocalData.is_user_authenticated(context!!)){
            showAuthFragment(R.id.main_container, fragmentManager, Constant.PROFILE_FRAGMENT_TAG)
            return null
        }
        if (home_view==null)
            home_view = inflater.inflate(R.layout.account, container, false)
        return home_view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}