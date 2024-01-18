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

        binding?.btnSubmitRegisterFragment?.setOnClickListener {
            val name = binding?.etNameSignInFragment?.text.toString()
            val firstname = binding?.etFirstnameSignInFragment?.text.toString()
            val login = binding?.etLoginRegisterFragment?.text.toString()
            val password = binding?.etPasswordRegisterFragment?.text.toString()
            val confirmPassword = binding?.etConfirmPasswordRegisterFragment?.text.toString()

            var isValid = true

            if (name.isBlank()) {
                binding?.etNameSignInFragment?.error = "Le nom est requis"
                isValid = false
            }
            if (firstname.isBlank()) {
                binding?.etFirstnameSignInFragment?.error = "Le prénom est requis"
                isValid = false
            }
            if (login.isBlank()) {
                binding?.etLoginRegisterFragment?.error = "L'identifiant est requis"
                isValid = false
            }
            if (password.isBlank()) {
                binding?.etPasswordRegisterFragment?.error = "Le mot de passe est requis"
                isValid = false
            } else if (!isPasswordValid(password)) {
                binding?.etPasswordRegisterFragment?.error =
                    "Le mot de passe doit contenir au moins 8 caractères, une lettre et un chiffre"
                isValid = false
            }
            if (confirmPassword.isBlank()) {
                binding?.etConfirmPasswordRegisterFragment?.error =
                    "La confirmation du mot de passe est requise"
                isValid = false
            }
            if (password != confirmPassword) {
                binding?.etConfirmPasswordRegisterFragment?.error =
                    "Les mots de passe ne correspondent pas"
                isValid = false
            }

            if (isValid) {
                viewModel.inscrireUtilisateur(name, firstname, login, password)
            }
        }

        viewModel.inscriptionState.observe(viewLifecycleOwner) { state ->
            if (state) {
                Toast.makeText(context, "Inscription réussie", Toast.LENGTH_LONG).show()
                findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToHomeFragment())
            }
        }

        return binding?.root
    }


    private fun isPasswordValid(password: String): Boolean {
        // Exemple de regex : au moins 8 caractères, une lettre et un chiffre
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$".toRegex()
        return password.matches(passwordRegex)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}