package com.example.louetoutfacile.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.louetoutfacile.network.UserDao
import com.example.louetoutfacile.network.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(

    private val userDao: UserDao,
    private val user: UserRepository,

    ) : ViewModel() {

    private val _loginState = MutableLiveData<Boolean>()
    val loginState: LiveData<Boolean> get() = _loginState

    fun loginUser(login: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val hashedPassword = hashPassword(password)
                val user = userDao.findByLogin(login)
                if (user != null && user.password == hashedPassword) {
                    this@LoginViewModel.user.saveUserDetails(user.id, user.name, user.login, user.isAdmin)
                    _loginState.postValue(true)
                } else {
                    _loginState.postValue(false)
                }
            } catch (e: Exception) {
                _loginState.postValue(false)
            }
        }
    }
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}

