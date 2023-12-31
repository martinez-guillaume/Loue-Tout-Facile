package com.example.louetoutfacile.ui.register

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.feedarticlesjetpack.ui.register.SignInViewModel
import com.example.louetoutfacile.R
import com.example.louetoutfacile.databinding.FragmentSignInBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignInFragment : Fragment() {

    private val viewModel: SignInViewModel by viewModels()
    private var binding: FragmentSignInBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.btnSubmitRegisterFragment?.setOnClickListener {
            val name = binding?.etNameSignInFragment?.text.toString()
            val firstname = binding?.etFirstnameSignInFragment?.text.toString()
            val login = binding?.etLoginRegisterFragment?.text.toString()
            val password = binding?.etPasswordRegisterFragment?.text.toString()
            val confirmPassword = binding?.etConfirmPasswordRegisterFragment?.text.toString()

            if (password == confirmPassword) {
                viewModel.inscrireUtilisateur(name, firstname, login, password)
            } else {
                Toast.makeText(requireContext(), "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.inscriptionState.observe(viewLifecycleOwner) { state ->
            if (state) {
                Toast.makeText(context, "Inscription réussie", Toast.LENGTH_LONG).show()
                findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToHomeFragment())
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}