package com.example.demoblutooth

import android.text.TextUtils
import android.util.Patterns

class User(private var password: String, private var email: String) {

    fun getEmail(): String {
        return email
    }

    fun getPassword(): String {
        return password
    }

    fun setEmail(newEmail: String) {
        email = newEmail
    }

    fun setPassword(newPassword: String) {
        password = newPassword
    }

    fun isVidEmail(): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isVidPassword(): Boolean {
        // Yêu cầu mật khẩu dài ít nhất 6 ký tự
        return !TextUtils.isEmpty(password) && password.length >= 6
    }


}
