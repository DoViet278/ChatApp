package com.example.chatapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.data.model.User
import com.example.chatapplication.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    fun loadUser(uid: String){
        viewModelScope.launch {
            _user.value = userRepo.getUser(uid)
        }
    }

    fun updateUser(user: User){
        viewModelScope.launch {
            userRepo.updateUser(user)
            _user.value = user
        }
    }

    fun uploadAvatar(inputSteam: InputStream, ext: String){
        viewModelScope.launch {
            try {
                _isUploading.value = true
                val url = userRepo.uploadAvatar(inputSteam, ext)
                val updatedUser = _user.value?.copy(avtUrl = url)
                if (updatedUser != null) {
                    updateUser(updatedUser)
                    _user.value = updatedUser
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isUploading.value = false
            }
        }
    }
}