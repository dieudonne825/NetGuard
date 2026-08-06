package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.components.ClientProfileData
import com.example.ui.registration.DjangoRegistrationScreen as RegistrationScreenImpl

@Composable
fun DjangoRegistrationScreen(
    initialProfile: ClientProfileData?,
    isAlreadyRegistered: Boolean,
    onCompleteRegistration: (ClientProfileData) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    RegistrationScreenImpl(
        initialProfile = initialProfile,
        isAlreadyRegistered = isAlreadyRegistered,
        onCompleteRegistration = onCompleteRegistration,
        onDismissRequest = onDismissRequest,
        modifier = modifier
    )
}
