package com.example.louetoutfacile.ui.splash

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.louetoutfacile.network.UserRepository
import com.example.louetoutfacile.R
import com.example.louetoutfacile.network.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : Fragment() {

    @Inject
    lateinit var user : UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       lifecycleScope.launch {
           try {
               delay(5000)
               if (user.getUserId() != -1L) {
                   SplashFragmentDirections.actionSplashFragmentToMainFragment().let {
                       findNavController().navigate(it)
                   }
               } else {
                   SplashFragmentDirections.actionSplashFragmentToLoginFragment().let {
                       findNavController().navigate(it)
                   }
               }
           } catch (e: Exception) {
               SplashFragmentDirections.actionSplashFragmentToLoginFragment().let {
                   findNavController().navigate(it)
               }
           }
       }
   }
}