package com.example.louetoutfacile.ui.main

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.louetoutfacile.network.Equipment
import com.example.louetoutfacile.network.EquipmentDao
import com.example.louetoutfacile.network.ReservationDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (

    private val sharedPreferences: SharedPreferences,
    private val equipmentDao: EquipmentDao,
    private val reservationDao: ReservationDao

) : ViewModel() {

    private val _equipments = MutableLiveData<List<Equipment>>()
    val equipments: LiveData<List<Equipment>> = _equipments

    init {
        loadEquipments()
    }

    private fun loadEquipments() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val equipmentsList = equipmentDao.getAll()
                _equipments.postValue(equipmentsList)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erreur lors du chargement des équipements", e)
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            // Effacer les données de session
            with(sharedPreferences.edit()) {
                clear()
                apply()
            }
        }
    }
    fun isAdmin(): Boolean {
       // return sharedPreferences.getBoolean("isAdmin", false)
        val isAdmin = sharedPreferences.getBoolean("isAdmin", false)
        Log.d("MainViewModel", "Is Admin: $isAdmin")
        return isAdmin
    }

    fun searchEquipmentsByTitle(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val filteredEquipments = equipmentDao.searchByTitle("%$title%")
                _equipments.postValue(filteredEquipments)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erreur lors de la recherche des équipements", e)
            }
        }
    }

    fun getReservationCount(equipmentId: Long): LiveData<Int> {
        val count = MutableLiveData<Int>()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                val reservationCount = reservationDao.countActiveReservationsForEquipment(equipmentId, today)
                count.postValue(reservationCount)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erreur lors de la récupération du nombre de réservations", e)
            }
        }
        return count
    }

}