package com.productcatalogue.feature.enquiry.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.productcatalogue.core.designsystem.theme.CatalogueTheme
import com.productcatalogue.feature.enquiry.domain.model.EnquiryField
import com.productcatalogue.feature.enquiry.domain.model.FieldProblem
import com.productcatalogue.feature.enquiry.domain.usecase.ValidateEnquiryUseCase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The enquiry form on a device: fields, error messages and the button behave as the ViewModel
 * tests assume. Drives the stateless content with a tiny state holder that applies the real
 * validation rules, so no network or Hilt is needed.
 */
@RunWith(AndroidJUnit4::class)
class EnquiryContentTest {
    @get:Rule val compose = createComposeRule()

    private val validate = ValidateEnquiryUseCase()
    private var state by mutableStateOf(EnquiryUiState(productTitle = "Mens Cotton Jacket"))
    private var submits = 0

    private fun show() = compose.setContent {
        CatalogueTheme {
            EnquiryContent(
                state = state,
                onBack = {},
                onFieldChange = { field, value ->
                    val draft = when (field) {
                        EnquiryField.NAME -> state.draft.copy(name = value)
                        EnquiryField.EMAIL -> state.draft.copy(email = value)
                        EnquiryField.PHONE -> state.draft.copy(phone = value)
                        EnquiryField.MESSAGE -> state.draft.copy(message = value)
                    }
                    state = state.copy(draft = draft, fieldProblems = state.fieldProblems - field)
                },
                onSubmit = {
                    submits++
                    state = state.copy(fieldProblems = validate(state.draft).mapValues { FieldProblem.Rule(it.value) })
                },
            )
        }
    }

    @Test
    fun submittingAnEmptyFormShowsWhatIsMissing() {
        show()
        compose.onNodeWithText("Mens Cotton Jacket").assertExists()
        compose.onNodeWithTag("submit-enquiry").performScrollTo().performClick()

        compose.onNodeWithText("Enter your name").assertExists()
        compose.onNodeWithText("Enter your email address").assertExists()
        assertEquals(1, submits)
    }

    @Test
    fun typingIntoAFieldClearsItsError() {
        show()
        compose.onNodeWithTag("submit-enquiry").performScrollTo().performClick()
        compose.onNodeWithTag("field-name").performTextInput("Ada")

        compose.onNodeWithText("Enter your name").assertDoesNotExist()
        compose.onNodeWithText("Enter your email address").assertExists()
    }

    @Test
    fun whileSendingTheButtonIsDisabledAndAFailureIsShown() {
        state = state.copy(isSubmitting = true)
        show()
        compose.onNodeWithTag("submit-enquiry").assertIsNotEnabled()

        state = state.copy(isSubmitting = false, submitError = "We couldn't send your enquiry just now. Please try again.")
        compose.onNodeWithTag("submit-error").performScrollTo().assertExists()
    }
}
