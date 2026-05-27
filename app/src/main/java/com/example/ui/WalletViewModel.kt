package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = WalletRepository(database.walletDao())

    // Flow states from DB
    val systemConfig = repository.systemConfigFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), SystemConfig()
    )

    val clients = repository.allClientsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val services = repository.allServicesFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val transactions = repository.allTransactionsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Logged-In state: "" or "manager" or "owner"
    private val _currentUserRole = MutableStateFlow("")
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    // Login errors
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // SMS Password Recovery state
    val phoneInput = MutableStateFlow("")
    val recoverySmsCodeInput = MutableStateFlow("")
    val newPasswordInput = MutableStateFlow("")
    val confirmPasswordInput = MutableStateFlow("")

    private val _smsSent = MutableStateFlow(false)
    val smsSent: StateFlow<Boolean> = _smsSent.asStateFlow()

    private val _generatedCode = MutableStateFlow("")
    val generatedCode: StateFlow<String> = _generatedCode.asStateFlow()

    private val _recoveryStep = MutableStateFlow(1) // 1: Phone, 2: OTP, 3: New Password, 4: Done
    val recoveryStep: StateFlow<Int> = _recoveryStep.asStateFlow()

    private val _smsStatusMessage = MutableStateFlow<String?>(null)
    val smsStatusMessage: StateFlow<String?> = _smsStatusMessage.asStateFlow()

    init {
        // Pre-create database entry
        viewModelScope.launch {
            if (repository.getSystemConfig() == null) {
                repository.saveSystemConfig(SystemConfig())
            }
        }
    }

    // Attempt Login
    fun login(password: String): Boolean {
        _loginError.value = null
        return if (password == "maher--736462") {
            _currentUserRole.value = "owner"
            true
        } else if (password == "WAM2026") {
            _currentUserRole.value = "manager"
            true
        } else {
            // Check stored general master password to see if it matches normal manager configuration
            val storedPass = systemConfig.value?.appPasswordHash ?: "123456"
            if (password == storedPass) {
                _currentUserRole.value = "manager"
                true
            } else {
                _loginError.value = "رمز الدخول غير صحيح!"
                false
            }
        }
    }

    fun logout() {
        _currentUserRole.value = ""
        resetRecoveryState()
    }

    // Reset password via SMS flow
    fun requestPasswordRecoverySms() {
        _smsStatusMessage.value = null
        val phone = phoneInput.value.trim()
        if (phone.length < 8) {
            _smsStatusMessage.value = "الرجاء إدخال رقم هاتف صحيح ومكتمل"
            return
        }

        // Generate 6 digit code
        val pin = String.format("%06d", Random.nextInt(100000, 999999))
        _generatedCode.value = pin
        _smsSent.value = true
        _recoveryStep.value = 2
        
        // Simulating the actual SMS delivery inside Android. Show it for testing/verification ease!
        _smsStatusMessage.value = "تم إرسال رمز التحقق بنجاح إلى: $phone\nالرمز التجريبي المستلم هو: $pin"
    }

    fun verifyRecoveryCode() {
        _smsStatusMessage.value = null
        val input = recoverySmsCodeInput.value.trim()
        val correctPin = _generatedCode.value

        if (input == correctPin) {
            _recoveryStep.value = 3
            _smsStatusMessage.value = "تم التحقق من الرمز بنجاح. يرجى إدخال كلمة المرور الجديدة."
        } else {
            _smsStatusMessage.value = "الرمز الذي أدخلته غير صحيح. يرجى المحاولة مرة أخرى."
        }
    }

    fun saveNewPassword(onSuccess: () -> Unit) {
        _smsStatusMessage.value = null
        val newPass = newPasswordInput.value.trim()
        val confirmPass = confirmPasswordInput.value.trim()

        if (newPass.isEmpty()) {
            _smsStatusMessage.value = "حقل كلمة المرور الجديدة لا يمكن أن يكون فارغاً"
            return
        }
        if (newPass != confirmPass) {
            _smsStatusMessage.value = "كلمتا المرور غير متطابقتين!"
            return
        }

        viewModelScope.launch {
            val current = systemConfig.value ?: SystemConfig()
            val updated = current.copy(appPasswordHash = newPass)
            if (newPass == "maher--736462") {
                // Cannot overwrite standard bypass, but can store as hash
            }
            repository.saveSystemConfig(updated)
            _recoveryStep.value = 4
            _smsStatusMessage.value = "تم تغيير كلمة المرور بنجاح! يمكنك الآن تسجيل الدخول بها."
            onSuccess()
        }
    }

    fun resetRecoveryState() {
        phoneInput.value = ""
        recoverySmsCodeInput.value = ""
        newPasswordInput.value = ""
        confirmPasswordInput.value = ""
        _smsSent.value = false
        _generatedCode.value = ""
        _recoveryStep.value = 1
        _smsStatusMessage.value = null
    }

    // --- Ledger Records management (Clients) ---
    fun addClient(name: String, phone: String, debt: Double, paid: Double, credit: Double, customFields: String = "") {
        viewModelScope.launch {
            val client = ClientRecord(
                name = name,
                phone = phone,
                debtAmount = debt,
                paidAmount = paid,
                creditAmount = credit,
                customFieldsJson = customFields
            )
            val clientId = repository.addClient(client)

            // Auto-log initial logs
            if (debt > 0) {
                repository.addTransaction(TransactionLog(clientId = clientId, clientName = name, amount = debt, type = "دين", notes = "قيد ابتدائي"))
            }
            if (paid > 0) {
                repository.addTransaction(TransactionLog(clientId = clientId, clientName = name, amount = paid, type = "مدفوع", notes = "قيد ابتدائي"))
            }
            if (credit > 0) {
                repository.addTransaction(TransactionLog(clientId = clientId, clientName = name, amount = credit, type = "رصيد دائن", notes = "قيد ابتدائي"))
            }
        }
    }

    fun updateClient(client: ClientRecord) {
        viewModelScope.launch {
            repository.updateClient(client)
        }
    }

    fun deleteClient(client: ClientRecord) {
        viewModelScope.launch {
            repository.deleteClient(client)
        }
    }

    // --- Services management (Owner Only) ---
    fun addService(name: String, price: Double, type: String, isEnabled: Boolean = true) {
        viewModelScope.launch {
            repository.addService(AppService(name = name, price = price, type = type, isEnabled = isEnabled))
        }
    }

    fun updateService(service: AppService) {
        viewModelScope.launch {
            repository.updateService(service)
        }
    }

    fun deleteService(service: AppService) {
        viewModelScope.launch {
            repository.deleteService(service)
        }
    }

    // --- Dynamic configurations (Owner Only) ---
    fun updateAppName(newName: String) {
        viewModelScope.launch {
            val current = systemConfig.value ?: SystemConfig()
            repository.saveSystemConfig(current.copy(appName = newName))
        }
    }

    fun updateAppColors(primaryHex: String, secondaryHex: String) {
        viewModelScope.launch {
            val current = systemConfig.value ?: SystemConfig()
            repository.saveSystemConfig(current.copy(
                primaryColorHex = primaryHex,
                secondaryColorHex = secondaryHex
            ))
        }
    }

    fun toggleDarkMode(darkMode: Boolean) {
        viewModelScope.launch {
            val current = systemConfig.value ?: SystemConfig()
            repository.saveSystemConfig(current.copy(isDarkMode = darkMode))
        }
    }

    fun toggleSystemFeature(reports: Boolean? = null, notifications: Boolean? = null, backup: Boolean? = null, sync: Boolean? = null) {
        viewModelScope.launch {
            val current = systemConfig.value ?: SystemConfig()
            repository.saveSystemConfig(current.copy(
                isReportsEnabled = reports ?: current.isReportsEnabled,
                isNotificationsEnabled = notifications ?: current.isNotificationsEnabled,
                isBackupEnabled = backup ?: current.isBackupEnabled,
                isRealTimeSyncEnabled = sync ?: current.isRealTimeSyncEnabled
            ))
        }
    }

    fun updateAdvancedSettings(smsNumber: String, customCols: String = "", stockLimit: Int? = null) {
        viewModelScope.launch {
            val current = systemConfig.value ?: SystemConfig()
            repository.saveSystemConfig(current.copy(
                smsGatewayNumber = smsNumber,
                customColumnsSemicolonSeparated = customCols,
                minStockLimit = stockLimit ?: current.minStockLimit
            ))
        }
    }

    fun updateSupervisorPermissions(permissions: String) {
        viewModelScope.launch {
            val current = systemConfig.value ?: SystemConfig()
            repository.saveSystemConfig(current.copy(
                supervisorPermissionsJson = permissions
            ))
        }
    }

    // Wipe all data (Owner only)
    fun wipeAllData() {
        viewModelScope.launch {
            repository.clearAllDatabase()
        }
    }
}
