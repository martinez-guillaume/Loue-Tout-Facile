package com.example.louetoutfacile.network

import android.content.SharedPreferences
import javax.inject.Inject

class UserRepository  @Inject constructor(private val sharedPreferences: SharedPreferences){

    fun saveUserDetails(userId: Long, userName: String, userLogin: String,userIsAdmin: Boolean) {
        sharedPreferences.edit().apply {
            putLong("userId", userId)
            putString("userName", userName)
            putString("userLogin", userLogin)
            putBoolean("isAdmin", userIsAdmin)
            apply()
        }
    }
    fun getUserId(): Long {
        return sharedPreferences.getLong("userId", -1) // -1 est une valeur par défaut indiquant qu'aucun utilisateur n'est connecté.
    }
}

class AdminRepository @Inject constructor(

) {

}

