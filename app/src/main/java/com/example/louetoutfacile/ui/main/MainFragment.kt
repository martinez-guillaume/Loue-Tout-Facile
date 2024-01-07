package com.example.louetoutfacile.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import android.Manifest
import com.example.louetoutfacile.R
import com.example.louetoutfacile.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!


    companion object {
        private const val REQUEST_PHONE_CALL = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivShoppingCartMainFragment.setOnClickListener {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:0665669921") // Remplacez par votre numéro de téléphone

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), REQUEST_PHONE_CALL)
            } else {
                startActivity(callIntent)
            }
        }


        // recupére l'état d'administrateur depuis le ViewModel
        viewModel.isAdmin()

        val adapter = AnnouncementListAdapter(
            viewModel = viewModel,
            lifecycleOwner = viewLifecycleOwner,
            onItemClicked = { equipmentId ->
                val action = MainFragmentDirections.actionMainFragmentToDetailAnnouncementFragment(equipmentId)
                findNavController().navigate(action)
            },
            isAdmin = viewModel.isAdmin() // Passer le bon paramètre
        )

        binding.recyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context)
        }

        binding.recyclerView.adapter = adapter

        binding.recyclerView.layoutManager = LinearLayoutManager(context)


        viewModel.equipments.observe(viewLifecycleOwner) { equipments ->
            adapter.submitList(equipments)
        }

        binding.tvAgencyPresentationMainFragment.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_agencyPresentation)
        }

        binding.tvRentalConditionsMainFragment.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_rentalConditionFragment)
        }

        if (viewModel.isAdmin()) {

            binding.btnAddMainfragment.visibility = View.VISIBLE
            binding.btnAddMainfragment.setOnClickListener {
                findNavController().navigate(R.id.action_mainFragment_to_creaAnnouncementFragment)
            }

            binding.tvAgencyPresentationMainFragment.visibility = View.GONE
            binding.tvRentalConditionsMainFragment.visibility = View.GONE

        } else {
            binding.btnAddMainfragment.visibility = View.GONE

            binding.tvAgencyPresentationMainFragment.visibility = View.VISIBLE
            binding.tvRentalConditionsMainFragment.visibility = View.VISIBLE
        }

        binding.ivLogoutMainFragment.setOnClickListener {
            viewModel.logoutUser()
            findNavController().navigate(R.id.action_mainFragment_to_loginFragment)
        }

        binding.categoryAutocomplete.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString()
                viewModel.searchEquipmentsByTitle(searchText)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // demande de permission pour utiliser le telephone
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PHONE_CALL && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:0123456789")
            startActivity(callIntent)
        }
    }
}
