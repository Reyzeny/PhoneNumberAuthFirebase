package com.reyzeny.phoneauth_firebase

import android.content.Context
import android.content.SharedPreferences


    object LocalData {
        private val PREF_NAME = "app_shared_pref"
        private val PRIVATE_MODE = 0
        private val FIRST_TIME = "first_time"
        private val USER_AUTHENTICATED = "user_authenticated"
        private val ADMIN_USER = "admin_user"
        private val USER_ID = "user_id"


        private fun get_shared_preference(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        }

        private fun get_preference_editor(context: Context): SharedPreferences.Editor {
            return get_shared_preference(context).edit()
        }

        fun set_is_first_time(context: Context, first_time: Boolean) {
            get_preference_editor(context).putBoolean(FIRST_TIME, first_time).commit()
        }

        fun get_is_first_time(context: Context): Boolean {
            return get_shared_preference(context).getBoolean(FIRST_TIME, true)
        }

        fun set_user_authenticated(context: Context, user_authenticated: Boolean) {
            get_preference_editor(context).putBoolean(USER_AUTHENTICATED, user_authenticated).commit()
        }

        fun is_user_authenticated(context: Context): Boolean {
            return get_shared_preference(context).getBoolean(USER_AUTHENTICATED, false)
        }

        fun set_user_id(context: Context, user_phone_number: String) {
            get_preference_editor(context).putString(USER_ID, user_phone_number).commit()
        }

        fun get_user_id(context: Context): String? {
            return get_shared_preference(context).getString(USER_ID, null)
        }

        fun set_user_admin(context: Context, user_is_admin: Boolean) {
            get_preference_editor(context).putBoolean(ADMIN_USER, user_is_admin).commit()
        }

        fun is_user_admin(context: Context): Boolean {
            return get_shared_preference(context).getBoolean(ADMIN_USER, false)
        }



    }
