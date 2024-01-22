package com.example.louetoutfacile.ui.detailsArticle

import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.louetoutfacile.network.UserRepository
import com.example.louetoutfacile.network.Equipment
import com.example.louetoutfacile.network.EquipmentDao
import com.example.louetoutfacile.network.Reservation
import com.example.louetoutfacile.network.ReservationDao
import com.example.louetoutfacile.network.ReservationDetail
import com.example.louetoutfacile.network.StatusDao
import com.example.louetoutfacile.network.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.withContext
import java.util.Calendar
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

    private val _reservationDeletionSuccess = MutableLiveData<Boolean>()
    val reservationDeletionSuccess: LiveData<Boolean> = _reservationDeletionSuccess

    private val _reservationMessage = MutableLiveData<String>()
    val reservationMessage: LiveData<String> = _reservationMessage

    private val _reservationDetails = MutableLiveData<List<ReservationDetail>>()
    val reservationDetails: LiveData<List<ReservationDetail>> = _reservationDetails

    private val _noReservationsMessage = MutableLiveData<String>()
    val noReservationsMessage: LiveData<String> = _noReservationsMessage

    private val _singleReservationDeletionSuccess = MutableLiveData<Boolean>()
    val singleReservationDeletionSuccess: LiveData<Boolean> = _singleReservationDeletionSuccess

    // à finir de mettre en vairable dans strings.xml   --------------
    fun loadEquipmentDetails(equipmentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val equipment = equipmentDao.findById(equipmentId)
                _equipmentDetails.postValue(equipment)

                val statusName = statusDao.getStatusNameById(equipment.status)
                val displayStatus = if (isAdmin()) {
                    when (statusName) {
                        "Disponible", "Réservé" -> "En magasin"
                        "Loué" -> "En dehors du magasin"
                        else -> statusName
                    }
                } else {
                    if (statusName == "Réservé" || statusName == "Disponible") "Disponible" else "Non disponible aujourd'hui"
                }
                _statusName.postValue(displayStatus)
            } catch (e: Exception) {
                Log.e("DetailsAnnouncementVM", "Error loading equipment details", e)
            }
        }
    }


    fun isAdmin(): Boolean {
        val isAdmin = sharedPreferences.getBoolean("isAdmin", false)
        return isAdmin
    }

    fun deleteEquipment(equipmentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                equipmentDao.findById(equipmentId).let {
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
            _reservationMessage.postValue("Veuillez vous connecter.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                equipmentDao.findById(equipmentId).let {
                    it.status = 2
                    equipmentDao.update(it)

                    val newReservation = Reservation(0, userId, equipmentId, startDate, endDate)
                    reservationDao.insert(newReservation)

                    val startDateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(startDate)
                    _reservationMessage.postValue("Félicitations !!!\n\nVotre réservation est bien confirmée !!!\n\nNous vous attendons dans notre agence de Salon de Provence le $startDateFormatted pour retirer votre équipement.")

                    loadReservationDetails(equipmentId)
                }
            } catch (e: Exception) {
                _reservationMessage.postValue("Erreur lors de la réservation")
            }
        }
    }

    private fun isClosedDay(date: Date): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.MONDAY
    }

    fun isEquipmentAvailable(equipmentId: Long, startDate: Date, endDate: Date, callback: (Boolean, Pair<String, String>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            // Vérification si la date tombe un dimanche ou un lundi
            if (isClosedDay(startDate) || isClosedDay(endDate)) {
                withContext(Dispatchers.Main) {
                    callback(false, Pair("closedDays", "L'agence est fermée les dimanches et lundis."))
                }
                return@launch
            }
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
                    if (isAvailable) {
                        callback(true, Pair("available", ""))
                    } else {
                        callback(false, Pair("conflict", formattedConflicts))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, Pair("error", "Erreur lors de la vérification des disponibilités"))
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
            try {
                val reservations = reservationDao.getReservationsForEquipment(equipmentId)
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val validReservations = reservations.filter {
                    it.end_date.after(today) || it.end_date.equals(today)
                }
                val reservationDetails = validReservations.map { reservation ->
                    // Exemple de création d'un objet ReservationDetail
                    val user = userDao.findById(reservation.id_user)
                    val startDateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(reservation.start_date)
                    val endDateFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(reservation.end_date)
                    ReservationDetail(
                        id = reservation.id,
                        userName = user.name,
                        firstname = user.firstname,
                        startDate = startDateFormatted,
                        endDate = endDateFormatted
                    )
                }
                _reservationDetails.postValue(reservationDetails)
            } catch (e: Exception) {
                Log.e("DetailsAnnouncementVM", "Error loading reservation details", e)
            }
        }
    }


    fun deleteReservation(reservationId: Long,equipmentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                try {
                    reservationDao.deleteReservationById(reservationId)
                    _singleReservationDeletionSuccess.postValue(true)
                    loadReservationDetails(equipmentId)
                } catch (e: Exception) {
                    _singleReservationDeletionSuccess.postValue(false)
                }
            } catch (e: Exception) {
                _reservationDeletionSuccess.postValue(false)
            }
        }
    }
}