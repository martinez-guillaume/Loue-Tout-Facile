package com.example.louetoutfacile.ui.detailsArticle

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.louetoutfacile.databinding.ItemReservationBinding
import com.example.louetoutfacile.network.ReservationDetail
import com.example.louetoutfacile.ui.main.AnnouncementListAdapter

class ReservationListAdapter(

    private val onDeleteClicked: (Long) -> Unit

) : ListAdapter<ReservationDetail, ReservationListAdapter.ReservationViewHolder>(DiffCallback()) {

    class ReservationViewHolder(

        private val binding: ItemReservationBinding,
        private val onDeleteClicked: (Long) -> Unit

    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reservationDetail: ReservationDetail) {
            val fullName = "${reservationDetail.userName} ${reservationDetail.firstname}"
            binding.tvReservationNameAndFirstnameDetailAnnouncementFragment.text = fullName

            val formattedDates = "Du : ${reservationDetail.startDate} au ${reservationDetail.endDate}"
            binding.tvReservationStartAndEndDateDetailAnnouncementFragment.text = formattedDates

            // l'ImageView de suppression (poubelle rouge)
            binding.ivDeleteReservationDetailAnnouncementFragment.setOnClickListener {
                onDeleteClicked(reservationDetail.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val binding = ItemReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReservationViewHolder(binding,onDeleteClicked)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ReservationDetail>() {
        override fun areItemsTheSame(oldItem: ReservationDetail, newItem: ReservationDetail): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ReservationDetail, newItem: ReservationDetail): Boolean {
            return oldItem == newItem
        }
    }
}
