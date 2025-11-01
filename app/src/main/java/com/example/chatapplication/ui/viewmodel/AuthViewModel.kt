package com.example.chatapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapplication.data.model.User
import com.example.chatapplication.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
): ViewModel() {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        viewModelScope.launch {
            val user = repository.getCurrentUser()
            _currentUser.value = user
        }
    }
    fun register(name: String, email: String, password: String) {
        _error.value = null
        viewModelScope.launch {
            val result = repository.registerUser(name, email, password)
            result.onSuccess {
                _currentUser.value = it
            }.onFailure {
                _error.value = it.message
            }
        }
    }


    fun login(email: String, password: String) {
        viewModelScope.launch {
            val result = repository.loginUser(email, password)
            result.onSuccess {
                _currentUser.value = it
            }.onFailure {
                _error.value = it.message
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _currentUser.value = null
        }
    }

    fun clearError() {
        _error.value = null
    }

}