package com.example.demoblutooth

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.demoblutooth.databinding.LoginBinding

class UserMainActivity: AppCompatActivity() {
    private lateinit var binding: LoginBinding
    private lateinit var viewmodel : LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        //khoi tao viewmodel
        viewmodel = LoginViewModel()
        //inflating bo cuc rang buoc su dụng generic
        binding = DataBindingUtil.setContentView<LoginBinding>(this, R.layout.login)

        //lk viewmodel vs bocuc
        binding.loginViewModel = viewmodel
        binding.lifecycleOwner = this //cho phep livedata quan sat trong hoaạt động
    }
}