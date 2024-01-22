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
import com.example.louetoutfacile.ui.register.SignInViewModel
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
                binding?.etNameSignInFragment?.error = getString(R.string.error_name_required)
                isValid = false
            }
            if (firstname.isBlank()) {
                binding?.etFirstnameSignInFragment?.error = getString(R.string.error_firstname_required)
                isValid = false
            }
            if (login.isBlank()) {
                binding?.etLoginRegisterFragment?.error = getString(R.string.error_login_required)
                isValid = false
            }
            if (password.isBlank()) {
                binding?.etPasswordRegisterFragment?.error = getString(R.string.error_password_required)
                isValid = false
            } else if (!isPasswordValid(password)) {
                binding?.etPasswordRegisterFragment?.error =
                    getString(R.string.error_password_invalid)
                isValid = false
            }
            if (confirmPassword.isBlank()) {
                binding?.etConfirmPasswordRegisterFragment?.error =
                    getString(R.string.error_confirm_password_required)
                isValid = false
            }
            if (password != confirmPassword) {
                binding?.etConfirmPasswordRegisterFragment?.error =
                    getString(R.string.error_passwords_do_not_match)
                isValid = false
            }

            if (isValid) {
                viewModel.inscrireUtilisateur(name, firstname, login, password)
            }
        }

        viewModel.inscriptionState.observe(viewLifecycleOwner) { state ->
            if (state) {
                Toast.makeText(context, getString(R.string.toast_registration_successful), Toast.LENGTH_LONG).show()
                findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToHomeFragment())
            }
        }

        return binding?.root
    }


    private fun isPasswordValid(password: String): Boolean {
        // regex : au moins 8 caractères, une lettre et un chiffre
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$".toRegex()
        return password.matches(passwordRegex)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}