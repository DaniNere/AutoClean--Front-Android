package com.example.autoclean.ui.auth.verification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VerificationViewModel : ViewModel(){

    private val _phoneNumber = MutableLiveData<String>()
    val phoneNumber: LiveData<String> get()= _phoneNumber

    private val _role = MutableLiveData<String>()
    val role: LiveData<String> get() = _role

    private val _displayName = MutableLiveData<String>()
    val displayName: LiveData<String> get() = _displayName

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _uid = MutableLiveData<String>()
    val uid: LiveData<String> get() = _uid

    private val _photoUrl = MutableLiveData<String>()
    val photoUrl: LiveData<String> get() = _photoUrl

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> get() = _password


    fun updatePhoneNumber(phone: String) { _phoneNumber.value = phone }
    fun updateRole(role: String) { _role.value = role }
    fun updateDisplayName(name: String) { _displayName.value = name }
    fun updateEmail(email: String) { _email.value = email }
    fun updateUid(uid: String) { _uid.value = uid }
    fun updatePhotoUrl(url: String) { _photoUrl.value = url }
    fun updatePassword(pass: String) { _password.value = pass }
}