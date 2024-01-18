package com.example.louetoutfacile.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.feedarticlesjetpack.ui.login.LoginViewModel
import com.example.louetoutfacile.R
import com.example.louetoutfacile.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.tvRegisterLoginFragment.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signInFragment)
        }

        binding.btnSubmitLoginFragment.setOnClickListener {
            val login = binding.etLoginLoginFragment.text.toString()
            val password = binding.etPasswordLoginFragment.text.toString()

            var isValid = true

            if (login.isBlank()) {
                binding.etLoginLoginFragment.error = "L'identifiant est requis"
                isValid = false
            }
            if (password.isBlank()) {
                binding.etPasswordLoginFragment.error = "Le mot de passe est requis"
                isValid = false
            }

            if (isValid) {
                viewModel.loginUser(login, password)
            }
        }

        viewModel.loginState.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
            } else {
                Toast.makeText(requireContext(), "Non connecter", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}