package com.obrago.app.ui.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.obrago.app.data.model.AppData
import com.obrago.app.data.model.Role
import com.obrago.app.data.model.User
import com.obrago.app.data.repository.OtpEvent
import com.obrago.app.data.repository.PhoneAuthRepository
import com.obrago.app.data.repository.SessionManager
import com.obrago.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

import com.obrago.app.notifications.NotificationHelper

enum class AuthMode { LOGIN, REGISTER }
enum class AuthNavTarget { HOME_CUSTOMER, HOME_WORKER, ADMIN_PANEL }

data class AuthUiState(
    val role: Role = Role.CUSTOMER,
    val authMode: AuthMode = AuthMode.LOGIN,
    val step: Int = 1, // worker registration has 2 steps, like the web app

    val country: String = "Pakistan",
    val city: String = "Lahore",

    val name: String = "",
    val email: String = "",
    val phoneRaw: String = "",
    val password: String = "",
    val address: String = "",
    val idCard: String = "",
    val profilePicBase64: String? = null,
    val idFrontPicBase64: String? = null,
    val idBackPicBase64: String? = null,

    val termsAgreed: Boolean = false,

    // OTP state
    val showOtpModal: Boolean = false,
    val fallbackCode: String = "123456",
    val userOtpInput: String = "",
    val isPhoneVerified: Boolean = false,
    val otpError: String? = null,
    val loadingOtp: Boolean = false,
    val verificationId: String? = null,

    // Forgot password
    val forgotPasswordMode: Boolean = false,
    val newPassword: String = "",
    val confirmPassword: String = "",

    val errorMsg: String? = null,
    val isSubmitting: Boolean = false,
    val navigateTo: AuthNavTarget? = null,
    val resetPasswordDone: Boolean = false
) {
    val availableCountries get() = AppData.COUNTRIES
    val availableCities get() = AppData.CITIES_BY_COUNTRY[country] ?: AppData.CITIES_BY_COUNTRY["Pakistan"]!!
    val selectedCountryDialCode get() = AppData.COUNTRIES.firstOrNull { it.name == country }?.dialCode ?: "+92"
    val fullPhoneNumber: String
        get() {
            val clean = phoneRaw.trim().replace(" ", "").replace("-", "")
            if (clean.startsWith("+")) return clean
            if (clean.startsWith("00")) return "+" + clean.substring(2)
            return "$selectedCountryDialCode${clean.trimStart('0')}"
        }
}

class AuthViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val phoneAuthRepository: PhoneAuthRepository = PhoneAuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private var allUsers: List<User> = emptyList()

    init {
        // Equivalent to the onSnapshot(collection(db,'users')) live subscription in store.tsx
        userRepository.observeAllUsers()
            .onEach { allUsers = it }
            .launchIn(viewModelScope)
    }

    fun setRole(role: Role) = update { it.copy(role = role, errorMsg = null) }
    fun setAuthMode(mode: AuthMode) = update { it.copy(authMode = mode, step = 1, errorMsg = null) }
    fun setCountry(country: String) = update {
        val defaultCity = AppData.CITIES_BY_COUNTRY[country]?.firstOrNull() ?: "Other"
        it.copy(country = country, city = defaultCity)
    }
    fun setCity(city: String) = update { it.copy(city = city) }
    fun setName(v: String) = update { it.copy(name = v) }
    fun setEmail(v: String) = update { it.copy(email = v) }
    fun setPhoneRaw(v: String) = update { it.copy(phoneRaw = v, isPhoneVerified = false) }
    fun setPassword(v: String) = update { it.copy(password = v) }
    fun setAddress(v: String) = update { it.copy(address = v) }
    fun setIdCard(v: String) = update { it.copy(idCard = v) }
    fun setProfilePic(base64: String?) = update { it.copy(profilePicBase64 = base64) }
    fun setIdFrontPic(base64: String?) = update { it.copy(idFrontPicBase64 = base64) }
    fun setIdBackPic(base64: String?) = update { it.copy(idBackPicBase64 = base64) }
    fun setTermsAgreed(v: Boolean) = update { it.copy(termsAgreed = v) }
    fun setUserOtpInput(v: String) = update { it.copy(userOtpInput = v) }
    fun setForgotPasswordMode(v: Boolean) = update {
        it.copy(forgotPasswordMode = v, errorMsg = null, isPhoneVerified = false)
    }
    fun setNewPassword(v: String) = update { it.copy(newPassword = v) }
    fun setConfirmPassword(v: String) = update { it.copy(confirmPassword = v) }
    fun dismissOtpModal() = update { it.copy(showOtpModal = false) }
    fun consumeNavigation() = update { it.copy(navigateTo = null) }
    fun consumeResetPasswordDone() = update { it.copy(resetPasswordDone = false) }
    fun goToWorkerStep1() = update { it.copy(step = 1, errorMsg = null) }

    private inline fun update(block: (AuthUiState) -> AuthUiState) {
        _uiState.value = block(_uiState.value)
    }

    /** Mirrors handleSendOtp() in AuthFlow.tsx */
    fun sendOtp(activity: Activity) {
        val state = _uiState.value
        if (state.phoneRaw.isBlank()) {
            update { it.copy(errorMsg = "Please enter a valid phone number before requesting OTP code.") }
            return
        }
        update { it.copy(errorMsg = null, loadingOtp = true) }

        val targetPhone = state.fullPhoneNumber

        phoneAuthRepository.sendOtp(activity, targetPhone)
            .onEach { event ->
                when (event) {
                    is OtpEvent.CodeSent -> {
                        update {
                            it.copy(
                                loadingOtp = false,
                                showOtpModal = true,
                                userOtpInput = "",
                                otpError = null,
                                verificationId = event.verificationId
                            )
                        }
                        NotificationHelper.showNotification(
                            activity,
                            "Obrago OTP Sent",
                            "A 6-digit verification SMS was sent to $targetPhone."
                        )
                    }
                    is OtpEvent.FallbackMode -> {
                        update {
                            it.copy(
                                loadingOtp = false,
                                showOtpModal = true,
                                userOtpInput = "",
                                otpError = null,
                                fallbackCode = event.fallbackCode,
                                verificationId = null
                            )
                        }
                        NotificationHelper.showNotification(
                            activity,
                            "Obrago OTP Verification",
                            "Your verification code is: ${event.fallbackCode}"
                        )
                    }
                    is OtpEvent.AutoVerified -> update {
                        it.copy(loadingOtp = false, isPhoneVerified = true, showOtpModal = false)
                    }
                    is OtpEvent.Failed -> update {
                        it.copy(loadingOtp = false, errorMsg = event.message)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    /** Mirrors handleVerifyOtp() in AuthFlow.tsx */
    fun verifyOtp() {
        val state = _uiState.value
        if (state.userOtpInput.trim().length < 6) {
            update { it.copy(otpError = "Please enter the 6-digit OTP code received on your mobile phone via SMS.") }
            return
        }
        update { it.copy(loadingOtp = true, otpError = null) }
        viewModelScope.launch {
            val result = phoneAuthRepository.verifyOtp(state.verificationId, state.userOtpInput.trim(), state.fallbackCode)
            result.onSuccess {
                update { it.copy(loadingOtp = false, isPhoneVerified = true, showOtpModal = false, otpError = null, errorMsg = null) }
            }.onFailure {
                update { s ->
                    s.copy(
                        loadingOtp = false,
                        otpError = "Incorrect or expired 6-digit OTP code. Please check the SMS received on your phone."
                    )
                }
            }
        }
    }

    /** Mirrors handleLoginSubmit() in AuthFlow.tsx */
    fun submitLogin() {
        val state = _uiState.value
        val emailOrPhone = state.email.ifBlank { state.phoneRaw }
        when (val result = userRepository.evaluateLogin(emailOrPhone, state.password, state.role, allUsers)) {
            is UserRepository.LoginResult.AdminSuccess ->
                update { it.copy(errorMsg = null, navigateTo = AuthNavTarget.ADMIN_PANEL) }
            is UserRepository.LoginResult.Success -> {
                SessionManager.login(result.user)
                update {
                    it.copy(
                        errorMsg = null,
                        navigateTo = if (result.user.role == Role.WORKER.value) AuthNavTarget.HOME_WORKER else AuthNavTarget.HOME_CUSTOMER
                    )
                }
            }
            is UserRepository.LoginResult.Error ->
                update { it.copy(errorMsg = result.message) }
        }
    }

    /** Mirrors handleResetPassword() in AuthFlow.tsx */
    fun submitResetPassword() {
        val state = _uiState.value
        if (state.newPassword.isBlank() || state.confirmPassword.isBlank()) {
            update { it.copy(errorMsg = "Both password fields are required!") }
            return
        }
        if (state.newPassword != state.confirmPassword) {
            update { it.copy(errorMsg = "Passwords do not match!") }
            return
        }
        update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val result = userRepository.resetPassword(state.phoneRaw, state.newPassword, allUsers)
            result.onSuccess {
                update {
                    it.copy(
                        isSubmitting = false,
                        forgotPasswordMode = false,
                        authMode = AuthMode.LOGIN,
                        isPhoneVerified = false,
                        password = "",
                        newPassword = "",
                        confirmPassword = "",
                        errorMsg = null,
                        resetPasswordDone = true
                    )
                }
            }.onFailure { e ->
                update { it.copy(isSubmitting = false, errorMsg = e.message) }
            }
        }
    }

    /** Mirrors handleCustomerRegisterSubmit() in AuthFlow.tsx */
    fun submitCustomerRegister() {
        val state = _uiState.value
        val err = validateCustomerForm(state)
        if (err != null) {
            update { it.copy(errorMsg = err) }
            return
        }

        update { it.copy(isSubmitting = true, errorMsg = null) }
        viewModelScope.launch {
            try {
                val result = userRepository.registerCustomer(
                    name = state.name.trim(),
                    email = state.email.trim(),
                    phone = state.fullPhoneNumber,
                    password = state.password.trim(),
                    country = state.country,
                    city = state.city,
                    allUsers = allUsers
                )
                result.onSuccess { newUser ->
                    SessionManager.login(newUser)
                    update { it.copy(isSubmitting = false, errorMsg = null, navigateTo = AuthNavTarget.HOME_CUSTOMER) }
                }.onFailure { e ->
                    update { it.copy(isSubmitting = false, errorMsg = e.message ?: "Registration failed") }
                }
            } catch (e: Exception) {
                update { it.copy(isSubmitting = false, errorMsg = e.message ?: "Registration failed") }
            }
        }
    }

    private fun validateCustomerForm(s: AuthUiState): String? = when {
        s.name.isBlank() -> "Full Name is required!"
        s.email.isBlank() -> "Email Address is required!"
        s.phoneRaw.isBlank() -> "Phone Number is required!"
        s.password.isBlank() -> "Password is required!"
        !s.termsAgreed -> "You MUST check and accept the Terms & Conditions and Privacy Policy to proceed."
        else -> null
    }

    /** Mirrors handleWorkerNextStep() in AuthFlow.tsx (step 1 -> step 2) */
    fun submitWorkerStep1() {
        val state = _uiState.value
        val err = when {
            state.name.isBlank() -> "Full Name is required!"
            state.phoneRaw.isBlank() -> "Phone Number is required!"
            state.address.isBlank() -> "Address is required!"
            state.password.isBlank() -> "Password is required!"
            state.profilePicBase64 == null -> "Profile Picture upload is strictly MANDATORY for Worker registration!"
            else -> null
        }
        if (err != null) {
            update { it.copy(errorMsg = err) }
            return
        }
        update { it.copy(errorMsg = null, step = 2) }
    }

    /** Mirrors handleWorkerRegisterSubmit() in AuthFlow.tsx (step 2, final submit) */
    fun submitWorkerRegister() {
        val state = _uiState.value
        val err = when {
            state.idCard.isBlank() -> "CNIC / ID Card Number is required!"
            state.idFrontPicBase64 == null -> "CNIC Front Picture upload is strictly MANDATORY!"
            state.idBackPicBase64 == null -> "CNIC Back Picture upload is strictly MANDATORY!"
            !state.termsAgreed -> "You MUST check and accept the Terms & Conditions and Privacy Policy to proceed."
            else -> null
        }
        if (err != null) {
            update { it.copy(errorMsg = err) }
            return
        }

        update { it.copy(isSubmitting = true, errorMsg = null) }
        viewModelScope.launch {
            try {
                val result = userRepository.registerWorker(
                    name = state.name.trim(),
                    phone = state.fullPhoneNumber,
                    country = state.country,
                    city = state.city,
                    address = state.address.trim(),
                    cnic = state.idCard.trim(),
                    password = state.password.trim(),
                    avatar = state.profilePicBase64,
                    idFrontPic = state.idFrontPicBase64,
                    idBackPic = state.idBackPicBase64,
                    allUsers = allUsers
                )
                result.onSuccess { newUser ->
                    SessionManager.login(newUser)
                    update { it.copy(isSubmitting = false, errorMsg = null, navigateTo = AuthNavTarget.HOME_WORKER) }
                }.onFailure { e ->
                    update { it.copy(isSubmitting = false, errorMsg = e.message ?: "Registration failed") }
                }
            } catch (e: Exception) {
                update { it.copy(isSubmitting = false, errorMsg = e.message ?: "Registration failed") }
            }
        }
    }
}
