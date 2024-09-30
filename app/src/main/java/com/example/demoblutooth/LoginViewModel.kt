package com.example.demoblutooth

import android.util.Log
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.ObservableField

class LoginViewModel : BaseObservable() {
    private var email: String = ""
    private var password: String = ""

    // Thông báo cho người dùng
    public val messageLogin: ObservableField<String> = ObservableField()

    @Bindable
    fun getEmail(): String {
        return email
    }

    @Bindable
    fun getPassword(): String {
        return password
    }

    fun setEmail(newEmail: String) {
        email = newEmail
        notifyPropertyChanged(BR.email)
    }

    fun setPassword(newPassword: String) {
        password = newPassword
        notifyPropertyChanged(BR.password)
    }

    fun onClickLogin() {
        // Log thông tin người dùng
        Log.d("LoginViewModel", "Attempting to log in with email: $email and password: $password")

        val user = User(getEmail(), getPassword())
        if (user.isVidEmail() && user.isVidPassword()) {
            messageLogin.set("Login success")
            Log.d("LoginViewModel", "Login successful for email: $email")
        } else {
            messageLogin.set("Email or password invalid")
            Log.d("LoginViewModel", "Login failed for email: $email")
        }
    }
}
