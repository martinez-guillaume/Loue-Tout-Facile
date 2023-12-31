package com.example.louetoutfacile.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.louetoutfacile.R
import com.example.louetoutfacile.databinding.ItemRvAnnouncementBinding
import com.example.louetoutfacile.network.Equipment
import com.squareup.picasso.Picasso

class AnnouncementListAdapter(
    private val onItemClicked: (Long) -> Unit,
    private val viewModel: MainViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val isAdmin: Boolean
) : ListAdapter<Equipment, AnnouncementListAdapter.ArticleViewHolder>(DiffCallback()) {

    class ArticleViewHolder(
        private val binding: ItemRvAnnouncementBinding,
        private val viewModel: MainViewModel,
        private val lifecycleOwner: LifecycleOwner,
        private val onItemClicked: (Long) -> Unit,
        private val isAdmin: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(equipment: Equipment) {
            itemView.setOnClickListener { onItemClicked(equipment.id) }

            binding.tvTitleItemRv.text = if (equipment.title.length > 24) {
                equipment.title.substring(0, 24) + "..."
            } else {
                equipment.title
            }

            val isAdmin = viewModel.isAdmin()
            if (isAdmin) {
                // Affichage uniquement pour les administrateurs
                viewModel.getReservationCount(equipment.id).observe(lifecycleOwner) { count ->
                    val reservationText = "(${count.coerceAtLeast(0)}) Réservations"
                    binding.textArticleItemRv.text = reservationText
                    binding.textArticleItemRv.setTextColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.red
                        )
                    )
                }
            } else {
                // Affichage standard pour les utilisateurs
                binding.textArticleItemRv.text = if (equipment.description.length > 27) {
                    equipment.description.substring(0, 27) + "..."
                } else {
                    equipment.description
                }
            }

            if (isAdmin) {
                when (equipment.status) {
                    1 -> {
                        binding.priceItemRv.text = "En location"
                        binding.priceItemRv.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red)) // Utilisez le bon ID de couleur rouge
                    }
                    2, 3 -> {
                        binding.priceItemRv.text = "En magasin"
                        binding.priceItemRv.setTextColor(ContextCompat.getColor(binding.root.context, R.color.green)) // Utilisez le bon ID de couleur verte
                    }
                    else -> {
                        binding.priceItemRv.text = "Statut inconnu"
                    }
                }
            } else {
                //binding.priceItemRv.text = "${equipment.price} €"
                binding.priceItemRv.text = String.format("%.2f €/jour", equipment.price)
            }
           // binding.priceItemRv.text = String.format("%.2f €/jour", equipment.price)




            if (!equipment.imageUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(equipment.imageUrl)
                    .placeholder(R.drawable.logo_loue_tout_facile)
                    .error(R.drawable.logo_loue_tout_facile)
                    .into(binding.pictureItemRv)
            } else {
                binding.pictureItemRv.setImageResource(R.drawable.logo_loue_tout_facile)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemRvAnnouncementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding, viewModel, lifecycleOwner, onItemClicked, isAdmin) // Ajouter le paramètre isAdmin ici
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Equipment>() {
        override fun areItemsTheSame(oldItem: Equipment, newItem: Equipment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Equipment, newItem: Equipment): Boolean {
            return oldItem == newItem
        }
    }

}

