
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
import androidx.recyclerview.widget.LinearLayoutManager
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
    ): View {
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

            // Changer la couleur en fonction du statut de réservation
            val statusColor = when (statusName) {
                //admin
                "En magasin" -> ContextCompat.getColor(requireContext(), R.color.green)
                "En location" -> ContextCompat.getColor(requireContext(), R.color.red)
                "En dehors du magasin" -> ContextCompat.getColor(requireContext(), R.color.orange)
                //user
                "Non disponible aujourd'hui" -> ContextCompat.getColor(
                    requireContext(),
                    R.color.orange
                )

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
            if (message.isNotEmpty()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("\uD83C\uDF89 Réservation Confirmée ! \uD83C\uDF89")
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        binding.btnReservationDetailsAnnouncementFragment.setOnClickListener {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val startDateText = binding.datePickerButton.text.toString()
            val endDateText = binding.datePickerButton2.text.toString()

            if (startDateText == "Début" || endDateText == "Fin") {
                Toast.makeText(
                    context,
                    "Veuillez choisir une date de début et de fin de location.",
                    Toast.LENGTH_SHORT
                ).show()
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
                if (startDate.before(currentDate) || endDate.before(currentDate) || endDate.before(
                        startDate
                    )
                ) {
                    Toast.makeText(
                        context,
                        "Veuillez sélectionner des dates valides.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }


                //affichage des toast selon le choix des dates
                viewModel.isEquipmentAvailable(equipmentId, startDate, endDate) {
                        isAvailable, unavailableDatesInfo ->
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
                        val dialogMessage = when (unavailableDatesInfo.first) {
                            "closedDays" -> unavailableDatesInfo.second
                            else -> "Les dates sélectionnées ne sont pas disponibles. \n\nRéservations existantes du : ${unavailableDatesInfo.second}"
                        }
                        AlertDialog.Builder(requireContext())
                            .setTitle("Information")
                            .setMessage(dialogMessage)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            }catch (e: ParseException) {
                Toast.makeText(context, "Format de date invalide.", Toast.LENGTH_SHORT).show()
            }
        }


        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val equipmentId = arguments?.getLong("equipmentId") ?: return


        // Configuration du RecyclerView
        val adapter = ReservationListAdapter { reservationId ->
            onDeleteReservationClicked(reservationId, equipmentId)
        }

        binding.rvReservationDetailAnnouncementFragment.adapter = adapter
        binding.rvReservationDetailAnnouncementFragment.layoutManager = LinearLayoutManager(context)

        viewModel.loadEquipmentDetails(equipmentId)
        viewModel.loadReservationDetails(equipmentId)



        // affichage de la gestion des réservation selon admin ou pas.
        viewModel.reservationDetails.observe(viewLifecycleOwner) { details ->
            adapter.submitList(details)

            if (viewModel.isAdmin()) {
                if (details.isNullOrEmpty()) {
                    binding.rvReservationDetailAnnouncementFragment.visibility = View.GONE
                    binding.tvNoReservationDetailFragment.visibility = View.VISIBLE
                    binding.btnReinitialisationDetailAnnouncementFragment.visibility = View.GONE
                } else {
                    binding.rvReservationDetailAnnouncementFragment.visibility = View.VISIBLE
                    binding.tvNoReservationDetailFragment.visibility = View.GONE
                    binding.btnReinitialisationDetailAnnouncementFragment.visibility = View.VISIBLE
                }
            }else {
                binding.rvReservationDetailAnnouncementFragment.visibility = View.GONE
                binding.btnReinitialisationDetailAnnouncementFragment.visibility = View.GONE
                binding.tvNoReservationDetailFragment.visibility = View.GONE
            }
        }


        // suppression d'un article
            binding.ivDeleteDetailsAnnouncementFragment.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirmer la suppression")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette annonce ?")
                .setPositiveButton("Oui") { _, _ -> viewModel.deleteEquipment(equipmentId) }
                .setNegativeButton("Non", null)
                .show()
        }
        viewModel.singleReservationDeletionSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Réservation supprimée", Toast.LENGTH_SHORT).show()
            }
        }


        // affichage selon
        if (viewModel.isAdmin()) {
            binding.tvTitleDetailReservationDetailAnnouncementFragment.visibility = View.VISIBLE
            binding.btnReinitialisationDetailAnnouncementFragment.visibility = View.VISIBLE
        } else {
            binding.tvTitleDetailReservationDetailAnnouncementFragment.visibility = View.GONE
            binding.btnReinitialisationDetailAnnouncementFragment.visibility = View.GONE
        }


        // Suppression de toutes les réservations
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


    private fun onDeleteReservationClicked(reservationId: Long, equipmentId: Long) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmer la suppression")
            .setMessage("Êtes-vous sûr de vouloir supprimer cette réservation ?")
            .setPositiveButton("Oui") { _, _ ->
                viewModel.deleteReservation(reservationId, equipmentId)
            }
            .setNegativeButton("Non", null)
            .show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}