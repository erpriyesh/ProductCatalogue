package com.productcatalogue.feature.enquiry.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.productcatalogue.core.designsystem.component.BackTopBar
import com.productcatalogue.feature.enquiry.domain.model.EnquiryField
import com.productcatalogue.feature.enquiry.domain.usecase.ValidateEnquiryUseCase

@Composable
internal fun EnquiryScreen(
    onBack: () -> Unit,
    onSent: (reference: String) -> Unit,
    viewModel: EnquiryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.sentReference) {
        state.sentReference?.let(onSent)
    }
    EnquiryContent(state, onBack, viewModel::onFieldChange, viewModel::submit)
}

@Composable
internal fun EnquiryContent(
    state: EnquiryUiState,
    onBack: () -> Unit,
    onFieldChange: (EnquiryField, String) -> Unit,
    onSubmit: () -> Unit,
) {
    Scaffold(topBar = { BackTopBar("Enquire", onBack) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProductSummary(state.productTitle)
            Text(
                "Leave your details and our sales team will get back to you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FormField(
                state, EnquiryField.NAME, "Name", Icons.Filled.Person, state.draft.name, onFieldChange,
                KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            )
            FormField(
                state, EnquiryField.EMAIL, "Email", Icons.Filled.Email, state.draft.email, onFieldChange,
                KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            )
            FormField(
                state, EnquiryField.PHONE, "Phone (optional)", Icons.Filled.Phone, state.draft.phone, onFieldChange,
                KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            )
            FormField(
                state, EnquiryField.MESSAGE, "Message (optional)", Icons.AutoMirrored.Filled.Message,
                state.draft.message, onFieldChange,
                KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = false,
                hint = "${state.draft.message.length} / ${ValidateEnquiryUseCase.MESSAGE_MAX}",
            )

            if (state.submitError != null) SubmitErrorBanner(state.submitError)
            SubmitButton(isSubmitting = state.isSubmitting, onClick = onSubmit)
            PrivacyNote()
        }
    }
}

@Composable
private fun ProductSummary(productTitle: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Inventory2, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("You're asking about", style = MaterialTheme.typography.labelMedium)
                Text(productTitle, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun PrivacyNote() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "We only use your details to reply to this enquiry.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FormField(
    state: EnquiryUiState,
    field: EnquiryField,
    label: String,
    icon: ImageVector,
    value: String,
    onChange: (EnquiryField, String) -> Unit,
    keyboard: KeyboardOptions,
    singleLine: Boolean = true,
    hint: String? = null,
) {
    val error = state.fieldProblems[field]?.message(field)
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(field, it) },
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        shape = MaterialTheme.shapes.medium,
        isError = error != null,
        supportingText = (error ?: hint)?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 4,
        enabled = !state.isSubmitting,
        keyboardOptions = keyboard,
        modifier = Modifier.fillMaxWidth().testTag("field-${field.key}"),
    )
}

@Composable
private fun SubmitErrorBanner(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("submit-error")
            // Read out by TalkBack as soon as it appears.
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Text(message, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun SubmitButton(isSubmitting: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isSubmitting,
        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("submit-enquiry"),
    ) {
        if (isSubmitting) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text("Send enquiry")
        }
    }
}
