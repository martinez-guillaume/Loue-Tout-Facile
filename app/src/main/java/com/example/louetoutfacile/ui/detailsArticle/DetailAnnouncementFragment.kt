package com.example.louetoutfacile.ui.detailsArticle

import android.annotation.SuppressLint
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

        // Configuration du RecyclerView pour les réservations
        val adapter = ReservationListAdapter { reservationId ->
            onDeleteReservationClicked(reservationId, equipmentId)
        }

        binding.rvReservationDetailAnnouncementFragment.adapter = adapter
        binding.rvReservationDetailAnnouncementFragment.layoutManager = LinearLayoutManager(context)

        viewModel.loadEquipmentDetails(equipmentId)
        viewModel.loadReservationDetails(equipmentId)


        binding.ivEditDetailsAnnouncementFragment.setOnClickListener {
            findNavController().navigate(
                DetailAnnouncementFragmentDirections.actionDetailAnnouncementFragmentToEditAnnouncementFragment(
                    equipmentId
                )
            )
        }

        // affichage des données
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
                getString(R.string.status_in_store) -> ContextCompat.getColor(requireContext(), R.color.green)
                getString(R.string.status_on_rental) -> ContextCompat.getColor(requireContext(), R.color.red)
                getString(R.string.status_out_of_store) -> ContextCompat.getColor(requireContext(), R.color.orange)
                //user
                getString(R.string.status_not_available_today) -> ContextCompat.getColor(
                    requireContext(),
                    R.color.orange
                )

                else -> ContextCompat.getColor(requireContext(), R.color.green)
            }
            binding.tvStatusDetailAnnouncementFragment.setTextColor(statusColor)
        }

        // affichage des icons d'édition , de suppression selon is admin ou pas.
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
                    .setTitle(getString(R.string.reservation_confirmed_title))
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.ok), null)
                    .show()
            }
        }

        binding.btnReservationDetailsAnnouncementFragment.setOnClickListener {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val startDateText = binding.datePickerButton.text.toString()
            val endDateText = binding.datePickerButton2.text.toString()

            if (startDateText == getString(R.string.start_date_placeholder) || endDateText == getString(R.string.end_date_placeholder)) {
                Toast.makeText(
                    context,
                    getString(R.string.toast_please_select_valid_dates),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            try {
                val startDate = dateFormat.parse(startDateText)
                val endDate = dateFormat.parse(endDateText)
                val currentDate = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                if (startDate == null || endDate == null) {
                    Toast.makeText(context, getString(R.string.toast_invalid_date_format), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Vérifie si les dates sont valides
                if (startDate.before(currentDate) || endDate.before(currentDate) || endDate.before(
                        startDate
                    )
                ) {
                    Toast.makeText(
                        context,
                        getString(R.string.toast_select_valid_dates),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }


                //affichage des toast selon le choix des dates
                viewModel.isEquipmentAvailable(equipmentId, startDate, endDate) {
                        isAvailable, unavailableDatesInfo ->
                    if (isAvailable) {
                        AlertDialog.Builder(requireContext())
                            .setTitle(getString(R.string.alert_dialog_confirm_reservation_title))
                            .setMessage(getString(R.string.alert_dialog_confirm_reservation_message, startDateText, endDateText))
                            .setPositiveButton(getString(R.string.alert_dialog_yes)) { _, _ ->
                                viewModel.reserveEquipment(equipmentId, startDate, endDate)
                            }
                            .setNegativeButton(getString(R.string.alert_dialog_no), null)
                            .show()
                    } else {
                        val dialogMessage = when (unavailableDatesInfo.first) {
                            "closedDays" -> unavailableDatesInfo.second
                            else -> getString(R.string.alert_dialog_unavailable_dates, unavailableDatesInfo.second)
                        }
                        AlertDialog.Builder(requireContext())
                            .setTitle(getString(R.string.information))
                            .setMessage(dialogMessage)
                            .setPositiveButton(getString(R.string.ok), null)
                            .show()
                    }
                }
            }catch (e: ParseException) {
                Toast.makeText(context, getString(R.string.toast_invalid_date_format), Toast.LENGTH_SHORT).show()
            }
        }


        // affichage de la gestion des réservation selon admin ou pas.
        viewModel.reservationDetails.observe(viewLifecycleOwner) { details ->
            adapter.submitList(details)

            if (viewModel.isAdmin()) {
                binding.tvTitleDetailReservationDetailAnnouncementFragment.visibility = View.VISIBLE
                if (details.isNullOrEmpty()) {
                    binding.rvReservationDetailAnnouncementFragment.visibility = View.GONE
                    binding.tvNoReservationDetailFragment.visibility = View.VISIBLE
                } else {
                    binding.rvReservationDetailAnnouncementFragment.visibility = View.VISIBLE
                    binding.tvNoReservationDetailFragment.visibility = View.GONE
                }
            }else {
                binding.rvReservationDetailAnnouncementFragment.visibility = View.GONE
                binding.tvNoReservationDetailFragment.visibility = View.GONE
                binding.tvTitleDetailReservationDetailAnnouncementFragment.visibility = View.GONE
            }
        }
        //suppression d'une réservation
        viewModel.singleReservationDeletionSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, getString(R.string.toast_reservation_deleted), Toast.LENGTH_SHORT).show()
            }
        }

        // suppression d'un article
        binding.ivDeleteDetailsAnnouncementFragment.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.alert_dialog_confirm_deletion))
                .setMessage(getString(R.string.alert_dialog_confirm_deletion_message))
                .setPositiveButton(getString(R.string.alert_dialog_yes)) { _, _ -> viewModel.deleteEquipment(equipmentId) }
                .setNegativeButton(getString(R.string.alert_dialog_no), null)
                .show()
        }
        viewModel.deletionSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context,  getString(R.string.toast_announcement_deleted), Toast.LENGTH_SHORT).show()
                DetailAnnouncementFragmentDirections.actionDetailAnnouncementFragmentToMainFragment().let {
                    findNavController().popBackStack(R.id.mainFragment, false)

                }
            }
        }

        return binding.root
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
            .setTitle(getString(R.string.confirm_deletion_title))
            .setMessage(getString(R.string.confirm_deletion_message))
            .setPositiveButton(getString(R.string.alert_dialog_yes)) { _, _ ->
                viewModel.deleteReservation(reservationId, equipmentId)
            }
            .setNegativeButton(getString(R.string.alert_dialog_no), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}