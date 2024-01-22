package com.example.louetoutfacile.ui.editArticle

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.louetoutfacile.R
import com.example.louetoutfacile.databinding.FragmentEditAnnouncementBinding
import com.example.louetoutfacile.network.Equipment
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditAnnouncementFragment : Fragment() {

    private var _binding: FragmentEditAnnouncementBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditAnnouncementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditAnnouncementBinding.inflate(inflater, container, false)

        val equipmentId = arguments?.getLong("equipmentId") ?: return binding.root
        viewModel.loadEquipmentDetails(equipmentId)

        viewModel.equipmentDetails.observe(viewLifecycleOwner) { equipment ->
            binding.etTitleEditAnnouncementFragment.setText(equipment.title)
            binding.etContentEditFragment.setText(equipment.description)
            binding.etPictureEditAnnouncementFragment.setText(equipment.imageUrl)
            binding.etPriceEditAnnouncementFragment.setText(equipment.price.toString())
        }

        binding.btnSubmitEditArticleActivity.setOnClickListener {

            val title = binding.etTitleEditAnnouncementFragment.text.toString()
            val description = binding.etContentEditFragment.text.toString()
            val imageUrl = binding.etPictureEditAnnouncementFragment.text.toString()
            val price =
                binding.etPriceEditAnnouncementFragment.text.toString().toDoubleOrNull() ?: 0.0

            var isValid = true

            if (title.isBlank()) {
                binding.etTitleEditAnnouncementFragment.error = getString(R.string.error_title_required)
                isValid = false
            }
            if (description.isBlank()) {
                binding.etContentEditFragment.error = getString(R.string.error_description_required)
                isValid = false
            }
            if (imageUrl.isBlank()) {
                binding.etPictureEditAnnouncementFragment.error = getString(R.string.error_image_url_required)
                isValid = false
            }
            if (price <= 0.0) {
                binding.etPriceEditAnnouncementFragment.error = getString(R.string.error_valid_price_required)
                isValid = false
            }

            // Pour les valeurs de RadioButton, category
            val categoryId = when (binding.radioGroup3.checkedRadioButtonId) {
                R.id.rb_manutention_edit_announcement_fragment -> 1
                R.id.rb_outillage_edit_announcement_fragment -> 2
                R.id.rb_gardening_edit_article_fragment -> 3
                else -> 1
            }

            if (isValid) {
                val statusId = when (binding.radioGroup4.checkedRadioButtonId) {
                    R.id.rb_status_rented_edit_announcement_fragment -> 1
                    R.id.rb_status_reserved_edit_announcement_fragment -> 2
                    R.id.rb_status_available_edit_article_fragment -> 3
                    else -> 3
                }

                val updatedEquipment = Equipment(
                    id = equipmentId,
                    title = title,
                    description = description,
                    price = price.toString().toDoubleOrNull()
                        ?: 0.0,
                    category = categoryId,
                    status = statusId,
                    imageUrl = imageUrl
                )
                viewModel.updateEquipment(updatedEquipment)
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context,  getString(R.string.toast_update_successful), Toast.LENGTH_SHORT).show()
                findNavController().popBackStack(R.id.mainFragment, false)

            } else {
                Toast.makeText(context, getString(R.string.toast_update_failed), Toast.LENGTH_SHORT).show()
            }
        }

        binding.etPictureEditAnnouncementFragment.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val imageUrl = s?.toString()
                if (!imageUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.logo_loue_tout_facile)
                        .error(R.drawable.logo_loue_tout_facile)
                        .into(binding.ivEditAnnouncementFragment)
                }
            }
        })

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}