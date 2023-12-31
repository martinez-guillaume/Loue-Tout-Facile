package com.example.feedarticlesjetpack.ui.register

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feedarticlesjetpack.network.UserRepository
import com.example.louetoutfacile.network.User
import com.example.louetoutfacile.network.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {

    private val _inscriptionState = MutableLiveData<Boolean>()
    val inscriptionState: LiveData<Boolean> get() = _inscriptionState

    fun inscrireUtilisateur(name: String, firstname: String, login: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val hashedPassword = hashPassword(password)
                val user = User(0, name, firstname, login, hashedPassword, isAdmin = false)
                userDao.insert(user)
                Log.d("SignInViewModel", "User inserted successfully")

                withContext(Dispatchers.Main) {
                    _inscriptionState.value = true
                }
            } catch (e: Exception) {
                Log.e("SignInViewModel", "Error inserting user", e)
                withContext(Dispatchers.Main) {
                    _inscriptionState.value = false
                }
            }
        }
    }
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
