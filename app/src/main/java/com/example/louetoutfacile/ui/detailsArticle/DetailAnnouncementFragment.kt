
package com.example.louetoutfacile.ui.detailsArticle

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.louetoutfacile.ui.detailsArticle.DetailsAnnouncementViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.louetoutfacile.R
import com.example.louetoutfacile.databinding.FragmentDetailAnnouncementBinding
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.text.ParseException
import java.util.Calendar
import java.util.Date

@AndroidEntryPoint
class DetailAnnouncementFragment : Fragment() {

    private var _binding: FragmentDetailAnnouncementBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailsAnnouncementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailAnnouncementBinding.inflate(inflater, container, false)

        val equipmentId = arguments?.getLong("equipmentId") ?: return binding.root
        viewModel.loadEquipmentDetails(equipmentId)

        binding.ivEditDetailsAnnouncementFragment.setOnClickListener {
            val equipmentId = arguments?.getLong("equipmentId") ?: return@setOnClickListener
            findNavController().navigate(
                DetailAnnouncementFragmentDirections.actionDetailAnnouncementFragmentToEditAnnouncementFragment(
                    equipmentId
                )
            )
        }


        viewModel.equipmentDetails.observe(viewLifecycleOwner) { equipment ->
            binding.tvTitleDetailsAnnouncementFragment.text = equipment.title
            binding.tvContentDetailsAnnouncementFragment.text = equipment.description
            binding.tvPriceDetailsAnnouncementFragement.text =
                String.format("%.2f €", equipment.price)
            Picasso.get().load(equipment.imageUrl).into(binding.ivDetailsAnnouncementFragment)
        }

        viewModel.statusName.observe(viewLifecycleOwner) { statusName ->
            binding.tvStatusDetailAnnouncementFragment.text = statusName

            // Changer la couleur en fonction du nouveau statut
            val statusColor = when (statusName) {
                "En magasin" -> ContextCompat.getColor(requireContext(), R.color.green)
                "En location" -> ContextCompat.getColor(requireContext(), R.color.red)
                else -> ContextCompat.getColor(requireContext(), R.color.green)
            }
            binding.tvStatusDetailAnnouncementFragment.setTextColor(statusColor)
        }

        if (viewModel.isAdmin()) {
            binding.ivDeleteDetailsAnnouncementFragment.visibility = View.VISIBLE
            binding.ivEditDetailsAnnouncementFragment.visibility = View.VISIBLE
        } else {
            binding.ivDeleteDetailsAnnouncementFragment.visibility = View.GONE
            binding.ivEditDetailsAnnouncementFragment.visibility = View.GONE
        }
        binding.datePickerButton.setOnClickListener {
            showDatePickerDialog(binding.datePickerButton)
        }

        binding.datePickerButton2.setOnClickListener {
            showDatePickerDialog(binding.datePickerButton2)
        }



        viewModel.reservationMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        binding.btnReservationDetailsAnnouncementFragment.setOnClickListener {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val startDateText = binding.datePickerButton.text.toString()
            val endDateText = binding.datePickerButton2.text.toString()

            if (startDateText == "Début" || endDateText == "Fin") {
                Toast.makeText(context, "Veuillez choisir une date de début et de fin de location.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val startDate = dateFormat.parse(startDateText)
                val endDate = dateFormat.parse(endDateText)
                val currentDate = Date()

                if (startDate == null || endDate == null) {
                    Toast.makeText(context, "Erreur de format de date.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Vérifier si les dates sont valides
                if (startDate.before(currentDate) || endDate.before(currentDate) || endDate.before(startDate)) {
                    Toast.makeText(context, "Veuillez sélectionner des dates valides.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                viewModel.isEquipmentAvailable(equipmentId, startDate, endDate) { isAvailable, unavailableDatesInfo ->
                    if (isAvailable) {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Confirmer la réservation")
                            .setMessage("Voulez-vous réserver du $startDateText au $endDateText ?")
                            .setPositiveButton("Oui") { _, _ ->
                                viewModel.reserveEquipment(equipmentId, startDate, endDate)
                            }
                            .setNegativeButton("Non", null)
                            .show()
                    } else {
                        val dialogMessage = "Les dates sélectionnées ne sont pas disponibles. \n\nRéservations existantes du : $unavailableDatesInfo"
                        AlertDialog.Builder(requireContext())
                            .setTitle("Information")
                            .setMessage(dialogMessage)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            } catch (e: ParseException) {
                Toast.makeText(context, "Format de date invalide.", Toast.LENGTH_SHORT).show()
            }
        }


        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val equipmentId = arguments?.getLong("equipmentId") ?: return

        binding.ivDeleteDetailsAnnouncementFragment.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmer la suppression")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette annonce ?")
                .setPositiveButton("Oui") { _, _ -> viewModel.deleteEquipment(equipmentId) }
                .setNegativeButton("Non", null)
                .show()
        }

        viewModel.deletionSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Annonce supprimée", Toast.LENGTH_SHORT).show()
                findNavController().navigate(DetailAnnouncementFragmentDirections.actionDetailAnnouncementFragmentToMainFragment())
            }
        }
        viewModel.loadEquipmentDetails(equipmentId)
        viewModel.loadReservationDetails(equipmentId)

        viewModel.reservationDetails.observe(viewLifecycleOwner) { details ->
            binding.tvDateReservationAndNameDetailAnnouncementFragment.text = details
        }

        if (viewModel.isAdmin()) {
            binding.tvDateReservationAndNameDetailAnnouncementFragment.visibility = View.VISIBLE
            binding.tvTitleDetailReservationDetailAnnouncementFragment.visibility = View.VISIBLE
            binding.btnReinitialisationDetailAnnouncementFragment.visibility = View.VISIBLE
            adjustButtonMargin()
        } else {
            binding.tvDateReservationAndNameDetailAnnouncementFragment.visibility = View.GONE
            binding.tvTitleDetailReservationDetailAnnouncementFragment.visibility = View.GONE
            binding.btnReinitialisationDetailAnnouncementFragment.visibility = View.GONE
            adjustButtonMargin()
        }

        binding.btnReinitialisationDetailAnnouncementFragment.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmer la réinitialisation")
                .setMessage("Êtes-vous sûr de vouloir réinitialiser la liste des réservations ?")
                .setPositiveButton("Oui") { _, _ ->
                    viewModel.deleteReservationsForEquipment(
                        equipmentId
                    )
                }
                .setNegativeButton("Non", null)
                .show()
        }
        viewModel.reservationDeletionSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Les réservations ont bien été réinitialisées.", Toast.LENGTH_SHORT).show()
                viewModel.loadReservationDetails(equipmentId)
            }
        }
    }





    private fun showDatePickerDialog(datePickerButton: Button) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            datePickerButton.text = selectedDate
        }, year, month, day)

        dpd.show()
    }
    private fun adjustButtonMargin() {
        val layoutParams = binding.btnReservationDetailsAnnouncementFragment.layoutParams as ConstraintLayout.LayoutParams

        if (binding.tvDateReservationAndNameDetailAnnouncementFragment.visibility == View.VISIBLE) {
            layoutParams.bottomMargin = 0 // Pas de marge
        } else {
            layoutParams.bottomMargin = resources.getDimensionPixelSize(R.dimen.margin_20dp) // Marge de 20dp
        }

        binding.btnReservationDetailsAnnouncementFragment.layoutParams = layoutParams
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}