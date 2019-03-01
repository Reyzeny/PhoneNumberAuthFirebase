package com.reyzeny.phoneauth_firebase

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {
    private var homeFragment = HomeFragment()
    private var contactFragment = ContactFragment()
    private var profileFragment = AccountFragment()
    private var adminFragment = AdminFragment()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                showHomeFragment()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_contact -> {
                showContactFragment()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile -> {
                showProfileFragment()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_admin -> {
                showAdminFragment()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalFunctions.changeStatusBarColor(this)
        setContentView(R.layout.main_activity)
        showHomeFragment()

    }

    override fun onResume() {
        super.onResume()
        setUpBottomNav()
    }

    private fun setUpBottomNav() {
        navigation.menu.findItem(R.id.navigation_admin).isVisible = (LocalData.is_user_authenticated(this) && LocalData.is_user_admin(this))
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
    private fun showHomeFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.main_container, homeFragment).commit()
    }
    private fun showContactFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.main_container, contactFragment).commit()
    }
    private fun showProfileFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.main_container, profileFragment).commit()
    }
    private fun showAdminFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.main_container, adminFragment).commit()
    }
}
