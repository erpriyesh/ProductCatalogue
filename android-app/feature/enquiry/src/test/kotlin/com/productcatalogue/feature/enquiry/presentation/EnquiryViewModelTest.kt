package com.productcatalogue.feature.enquiry.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.testing.invoke
import com.productcatalogue.core.model.result.DataError
import com.productcatalogue.core.model.result.DataResult
import com.productcatalogue.core.testing.MainDispatcherRule
import com.productcatalogue.feature.enquiry.domain.model.EnquiryField
import com.productcatalogue.feature.enquiry.domain.model.EnquiryFieldError
import com.productcatalogue.feature.enquiry.domain.model.EnquiryReceipt
import com.productcatalogue.feature.enquiry.domain.model.FieldProblem
import com.productcatalogue.feature.enquiry.domain.usecase.SubmitEnquiryUseCase
import com.productcatalogue.feature.enquiry.domain.usecase.ValidateEnquiryUseCase
import com.productcatalogue.feature.enquiry.presentation.navigation.EnquiryRoute
import com.productcatalogue.feature.enquiry.testing.FakeEnquiryRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Robolectric because the ViewModel reads its route with SavedStateHandle.toRoute(). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EnquiryViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val repository = FakeEnquiryRepository()
    private val viewModel = EnquiryViewModel(
        SavedStateHandle(route = EnquiryRoute(productId = 7, productTitle = "Cotton Jacket")),
        SubmitEnquiryUseCase(ValidateEnquiryUseCase(), repository),
    )

    private fun fillValidForm() {
        viewModel.onFieldChange(EnquiryField.NAME, "Ada Lovelace")
        viewModel.onFieldChange(EnquiryField.EMAIL, "ada@example.com")
    }

    @Test
    fun `invalid input shows field problems and sends nothing`() = runTest {
        viewModel.onFieldChange(EnquiryField.EMAIL, "not-an-email")
        viewModel.submit()

        assertEquals(
            mapOf(
                EnquiryField.NAME to FieldProblem.Rule(EnquiryFieldError.REQUIRED),
                EnquiryField.EMAIL to FieldProblem.Rule(EnquiryFieldError.INVALID_FORMAT),
            ),
            viewModel.uiState.value.fieldProblems,
        )
        assertTrue(repository.submitted.isEmpty())
    }

    @Test
    fun `editing a field clears only that field's problem`() = runTest {
        viewModel.submit()
        viewModel.onFieldChange(EnquiryField.NAME, "Ada")

        assertEquals(setOf(EnquiryField.EMAIL), viewModel.uiState.value.fieldProblems.keys)
    }

    @Test
    fun `a valid enquiry is sent and its reference shown`() = runTest {
        fillValidForm()
        viewModel.submit()

        assertEquals(1, repository.submitted.size)
        assertEquals("ENQ-1", viewModel.uiState.value.sentReference)
    }

    @Test
    fun `while sending, the form is busy and a second tap sends nothing more`() = runTest {
        repository.gate = CompletableDeferred()
        fillValidForm()

        viewModel.submit()
        assertTrue(viewModel.uiState.value.isSubmitting)
        viewModel.submit()
        assertEquals(1, repository.submitted.size)

        repository.gate!!.complete(Unit)
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertEquals("ENQ-1", viewModel.uiState.value.sentReference)
    }

    @Test
    fun `an email failure shows a retryable message and no thank you`() = runTest {
        repository.result = DataResult.Failure(DataError.Server("EMAIL_DELIVERY_FAILED"))
        fillValidForm()
        viewModel.submit()

        assertNull(viewModel.uiState.value.sentReference)
        assertEquals(
            "We couldn't send your enquiry just now. Please try again.",
            viewModel.uiState.value.submitError,
        )

        repository.result = DataResult.Success(EnquiryReceipt("ENQ-2"))
        viewModel.submit()
        assertEquals("ENQ-2", viewModel.uiState.value.sentReference)
        assertNull(viewModel.uiState.value.submitError)
    }
}
