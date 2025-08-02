package com.stocktrading.app.ui.common

import androidx.lifecycle.ViewModel
import com.stocktrading.app.data.models.NetworkResult
import kotlinx.coroutines.flow.*


abstract class BaseViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    protected fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    protected fun setError(message: String?) {
        _errorMessage.value = message
    }

    protected fun setSuccess(message: String?) {
        _successMessage.value = message
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _successMessage.value = null
    }

    protected fun <T> handleNetworkResult(
        result: NetworkResult<T>,
        onSuccess: (T) -> Unit,
        onError: ((String) -> Unit)? = null,
        onLoading: ((Boolean) -> Unit)? = null
    ) {
        when (result) {
            is NetworkResult.Loading -> {
                if (onLoading != null) {
                    onLoading(result.isLoading)
                } else {
                    setLoading(result.isLoading)
                }
            }

            is NetworkResult.Success -> {
                setLoading(false)
                setError(null)
                onSuccess(result.data)
            }

            is NetworkResult.Error -> {
                setLoading(false)
                if (onError != null) {
                    onError(result.message)
                } else {
                    setError(result.message)
                }
            }
        }
    }

}

