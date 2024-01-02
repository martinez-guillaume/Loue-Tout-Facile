package com.example.louetoutfacile.ui.detailsArticle

import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.feedarticlesjetpack.network.UserRepository
import com.example.louetoutfacile.network.Equipment
import com.example.louetoutfacile.network.EquipmentDao
import com.example.louetoutfacile.network.Reservation
import com.example.louetoutfacile.network.ReservationDao
import com.example.louetoutfacile.network.StatusDao
import com.example.louetoutfacile.network.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class DetailsAnnouncementViewModel @Inject constructor(

    private val equipmentDao: EquipmentDao,
    private val statusDao: StatusDao,
    private val reservationDao: ReservationDao,
    private val sharedPreferences: SharedPreferences,
    private val userRepository: UserRepository,
    private val userDao: UserDao,

    ) : ViewModel() {

    private val _equipmentDetails = MutableLiveData<Equipment>()
    val equipmentDetails: LiveData<Equipment> = _equipmentDetails

    private val _statusName = MutableLiveData<String>()
    val statusName: LiveData<String> = _statusName

    private val _deletionSuccess = MutableLiveData<Boolean>()
    val deletionSuccess: LiveData<Boolean> = _deletionSuccess

    private val _reservationDetails = MutableLiveData<String>()
    val reservationDetails: LiveData<String> = _reservationDetails

    private val _reservationDeletionSuccess = MutableLiveData<Boolean>()
    val reservationDeletionSuccess: LiveData<Boolean> = _reservationDeletionSuccess

    private val _reservationMessage = MutableLiveData<String>()
    val reservationMessage: LiveData<String> = _reservationMessage

    fun loadEquipmentDetails(equipmentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val equipment = equipmentDao.findById(equipmentId)
                _equipmentDetails.postValue(equipment)

                val statusName = statusDao.getStatusNameById(equipment.status)
                val displayStatus = if (isAdmin()) {
                    // Pour un admin, changer le nom du statut
                    when (statusName) {
                        "Disponible", "Réservé" -> "En magasin"
                        "Loué" -> "En dehors du magasin"
                        else -> statusName
                    }
                } else {
                    // Pour un utilisateur non admin, laisser le statut tel quel
                    if (statusName == "Réservé") "Disponible" else statusName
                }
                _statusName.postValue(displayStatus)
            } catch (e: Exception) {
                Log.e("DetailsAnnouncementVM", "Error loading equipment details", e)
            }
        }
    }


    fun isAdmin(): Boolean {
        // return sharedPreferences.getBoolean("isAdmin", false)
        val isAdmin = sharedPreferences.getBoolean("isAdmin", false)
        Log.d("MainViewModel", "Is Admin: $isAdmin")
        return isAdmin
    }

    fun deleteEquipment(equipmentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val equipment = equipmentDao.findById(equipmentId)
                equipment.let {
                    equipmentDao.delete(it)
                    _deletionSuccess.postValue(true)
                }
            } catch (e: Exception) {
                _deletionSuccess.postValue(false)
            }
        }
    }

    fun reserveEquipment(equipmentId: Long, startDate: Date, endDate: Date) {

        val userId = userRepository.getUserId()
        if (userId == -1L) {
            // utilisateur non connecté
            _reservationMessage.postValue("Veuillez vous connecter.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val equipment = equipmentDao.findById(equipmentId)
                equipment.let {
                    it.status = 2
                    equipmentDao.update(it)

                    val newReservation = Reservation(0, userId, equipmentId, startDate, endDate)
                    reservationDao.insert(newReservation)
                    _reservationMessage.postValue("Réservation confirmée")
                }
            } catch (e: Exception) {
                Log.e("DetailsAnnouncementVM", "Erreur lors de la réservation", e)
                _reservationMessage.postValue("Erreur lors de la réservation")
            }
        }
    }

    fun isEquipmentAvailable(equipmentId: Long, startDate: Date, endDate: Date, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existingReservations = reservationDao.getReservationsForEquipment(equipmentId)
                val conflictingReservations = existingReservations.filter {
                    (startDate.before(it.end_date) && endDate.after(it.start_date)) ||
                            startDate == it.start_date || endDate == it.end_date
                }

                val formattedConflicts = conflictingReservations.joinToString("\n et du : ") { reservation ->
                    val formattedStart = formatDate(reservation.start_date)
                    val formattedEnd = formatDate(reservation.end_date)
                    "$formattedStart au $formattedEnd"
                }

                val isAvailable = conflictingReservations.isEmpty()

                withContext(Dispatchers.Main) {
                    callback(isAvailable, formattedConflicts)
                }
            } catch (e: Exception) {
                Log.e("DetailsAnnouncementVM", "Erreur lors de la vérification des disponibilités", e)
                withContext(Dispatchers.Main) {
                    callback(false, "")
                }
            }
        }
    }


    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }

    fun loadReservationDetails(equipmentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val reservations = reservationDao.getReservationsForEquipment(equipmentId)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

            if (reservations.isEmpty()) {
                _reservationDetails.postValue("Aucune réservation pour le moment...")
            } else {
                val detailsStringBuilder = StringBuilder()
                reservations.forEach { reservation ->
                    val user = userDao.findById(reservation.id_user)
                    val startDateFormatted = dateFormat.format(reservation.start_date)
                    val endDateFormatted = dateFormat.format(reservation.end_date)
                    detailsStringBuilder.append("${user.name} ${user.firstname},\na réserver du $startDateFormatted au $endDateFormatted\n\n")
                }
                _reservationDetails.postValue(detailsStringBuilder.toString())
            }
        }
    }

    fun deleteReservationsForEquipment(equipmentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                reservationDao.deleteReservationsByEquipmentId(equipmentId)
                _reservationDeletionSuccess.postValue(true)
            } catch (e: Exception) {
                _reservationDeletionSuccess.postValue(false)
            }
        }
    }
}