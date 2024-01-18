package com.example.louetoutfacile.ui.creaArticle

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.louetoutfacile.R
import com.example.louetoutfacile.databinding.FragmentCreaAnnouncementBinding
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreaAnnouncementFragment : Fragment() {

    private var _binding: FragmentCreaAnnouncementBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreaAnnouncementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreaAnnouncementBinding.inflate(inflater, container, false)

        binding.etPictureCreaAnnouncementFragment.addTextChangedListener(object : TextWatcher {
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
                        .into(binding.ivCreaAnnouncementFragment)
                }
            }
        })

        binding.btnSubmitCreaArticleActivity.setOnClickListener {
            val title = binding.etTitleCreaAnnouncementFragment.text.toString()
            val content = binding.etContentCreaFragment.text.toString()
            val imageUrl = binding.etPictureCreaAnnouncementFragment.text.toString()
            val categoryIdButtonId = binding.radioGroup.checkedRadioButtonId
            val statusButtonId = binding.radioGroup2.checkedRadioButtonId
            val price =
                binding.etPriceCreaAnnouncementFragment.text.toString().toDoubleOrNull() ?: 0.0

            viewModel.submitAnnouncement(
                title,
                content,
                imageUrl,
                categoryIdButtonId,
                statusButtonId,
                price
            )
        }


        viewModel.insertionSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Votre annonce a bien été crée.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack(R.id.mainFragment, false)
            } else {
                Toast.makeText(
                    context,
                    "Échec de la création , veuillez réessayer ultérieurement.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}