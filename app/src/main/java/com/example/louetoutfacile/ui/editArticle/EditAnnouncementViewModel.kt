package com.example.louetoutfacile.ui.editArticle

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.louetoutfacile.network.Equipment
import com.example.louetoutfacile.network.EquipmentDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditAnnouncementViewModel @Inject constructor(

    private val equipmentDao: EquipmentDao

) : ViewModel() {

    private val _equipmentDetails = MutableLiveData<Equipment>()
    val equipmentDetails: LiveData<Equipment> = _equipmentDetails

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess


    fun loadEquipmentDetails(equipmentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val equipment = equipmentDao.findById(equipmentId)
                _equipmentDetails.postValue(equipment)
            } catch (e: Exception) {
                Log.e("EditAnnouncementVM", "Error loading equipment details", e)
            }
        }
    }

    fun updateEquipment(equipment: Equipment) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                equipmentDao.update(equipment)
                _updateSuccess.postValue(true)
            } catch (e: Exception) {
                _updateSuccess.postValue(false)
            }
        }
    }
}
