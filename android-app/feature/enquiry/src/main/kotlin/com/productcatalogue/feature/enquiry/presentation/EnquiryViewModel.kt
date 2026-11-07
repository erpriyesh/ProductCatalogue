package com.productcatalogue.feature.enquiry.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.productcatalogue.core.ui.userMessage
import com.productcatalogue.feature.enquiry.domain.model.EnquiryField
import com.productcatalogue.feature.enquiry.domain.model.SubmitEnquiryResult
import com.productcatalogue.feature.enquiry.domain.usecase.SubmitEnquiryUseCase
import com.productcatalogue.feature.enquiry.presentation.navigation.EnquiryRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Holds the form and reacts to user actions. The rules (validation, what to send, how to read
 * the server answer) live in [SubmitEnquiryUseCase]; this class only maps results to screen state.
 */
@HiltViewModel
class EnquiryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val submitEnquiry: SubmitEnquiryUseCase,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<EnquiryRoute>()

    private val _uiState = MutableStateFlow(EnquiryUiState(productTitle = route.productTitle))
    val uiState: StateFlow<EnquiryUiState> = _uiState.asStateFlow()

    fun onFieldChange(field: EnquiryField, value: String) {
        _uiState.update { state ->
            val draft = when (field) {
                EnquiryField.NAME -> state.draft.copy(name = value)
                EnquiryField.EMAIL -> state.draft.copy(email = value)
                EnquiryField.PHONE -> state.draft.copy(phone = value)
                EnquiryField.MESSAGE -> state.draft.copy(message = value)
            }
            // Editing a field clears its error; the others stay until the next submit.
            state.copy(draft = draft, fieldProblems = state.fieldProblems - field)
        }
    }

    fun submit() {
        val current = _uiState.value
        // A second tap while sending must not send a second email.
        if (current.isSubmitting || current.sentReference != null) return

        _uiState.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            val result = submitEnquiry(route.productId, current.draft)
            _uiState.update { state ->
                when (result) {
                    is SubmitEnquiryResult.Sent ->
                        state.copy(isSubmitting = false, fieldProblems = emptyMap(), sentReference = result.reference)
                    is SubmitEnquiryResult.Rejected ->
                        state.copy(isSubmitting = false, fieldProblems = result.problems)
                    is SubmitEnquiryResult.Failed ->
                        state.copy(isSubmitting = false, submitError = result.error.userMessage())
                }
            }
        }
    }
}
