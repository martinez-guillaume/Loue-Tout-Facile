package com.example.louetoutfacile.ui.creaArticle

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.louetoutfacile.R
import com.example.louetoutfacile.network.Equipment
import com.example.louetoutfacile.network.EquipmentDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreaAnnouncementViewModel @Inject constructor (

    private val equipmentDao: EquipmentDao

) : ViewModel() {

    private val _insertionSuccess = MutableLiveData<Boolean>()
    val insertionSuccess: LiveData<Boolean> = _insertionSuccess

    fun createAnnouncement(title: String, content: String, imageUrl: String, categoryId: Int, status: Int, price: Double) {
        Log.d("CreaAnnouncementVM", "Début de la création d'une annonce")
        viewModelScope.launch(Dispatchers.IO) {
            try {
               val equipment = Equipment(0, title, content, categoryId, status, price, imageUrl)
               equipmentDao.insert(equipment)
                _insertionSuccess.postValue(true)
            } catch (e: Exception) {
                _insertionSuccess.postValue(false)
            }
        }
    }
    fun submitAnnouncement(title: String, content: String, imageUrl: String, categoryIdButtonId: Int, statusButtonId: Int, price: Double) {
        val categoryId = getCategoryFromRadioButton(categoryIdButtonId)
        val statusId = getStatusFromRadioButton(statusButtonId)

        createAnnouncement(title, content, imageUrl, categoryId, statusId, price)
    }

    private fun getCategoryFromRadioButton(buttonId: Int): Int {
        return when (buttonId) {
            R.id.rb_manutention_crea_announcement_fragment -> 1
            R.id.rb_outillage_crea_announcement_fragment -> 2
            R.id.rb_gardening_crea_article_fragment -> 3
            else -> 1 // Valeur par défaut ou gestion d'erreur
        }
    }

    private fun getStatusFromRadioButton(buttonId: Int): Int {
        return when (buttonId) {
            R.id.rb_status_rented_crea_announcement_fragment -> 1
            R.id.rb_status_reserved_crea_announcement_fragment -> 2
            R.id.rb_status_available_crea_article_fragment -> 3
            else -> 1
        }
    }

}