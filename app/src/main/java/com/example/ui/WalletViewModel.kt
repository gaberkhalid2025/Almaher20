package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.max

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = WalletRepository(db.walletDao())

    // UI States
    val allUsers: StateFlow<List<User>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionLog>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = repository.getAllAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appSettings: StateFlow<List<AppSetting>> = repository.getAppSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Computed App settings derived reactively
    val appTitle: StateFlow<String> = appSettings.map { list ->
        list.find { it.key == "app_name" }?.value ?: "الماهر موني"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "الماهر موني")

    val welcomePhrase: StateFlow<String> = appSettings.map { list ->
        list.find { it.key == "welcome_phrase" }?.value ?: "مرحباً بك في جيل المال الذكي"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "مرحباً بك في جيل المال الذكي")

    val p2pFeePercent: StateFlow<Double> = appSettings.map { list ->
        list.find { it.key == "p2p_fee_percent" }?.value?.toDoubleOrNull() ?: 1.5
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.5)

    val systemFrozen: StateFlow<Boolean> = appSettings.map { list ->
        list.find { it.key == "system_frozen" }?.value == "true"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val digitalAssetsEnabled: StateFlow<Boolean> = appSettings.map { list ->
        list.find { it.key == "enable_digital_assets" }?.value == "true"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Logged in user session
    private val _currentUserPhone = MutableStateFlow<String?>(null)
    val currentUser: StateFlow<User?> = _currentUserPhone.flatMapLatest { phone ->
        if (phone == null) flowOf(null) else repository.getUser(phone)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // User Lockout Security
    private val _loginAttempts = MutableStateFlow(0)
    val loginAttempts: StateFlow<Int> = _loginAttempts.asStateFlow()

    private val _userLockoutTime = MutableStateFlow<Long>(0)
    val userLockoutTime: StateFlow<Long> = _userLockoutTime.asStateFlow()

    // Admin Lockout Security
    private val _adminLoginAttempts = MutableStateFlow(0)
    val adminLoginAttempts: StateFlow<Int> = _adminLoginAttempts.asStateFlow()

    private val _adminLockoutTime = MutableStateFlow<Long>(0)
    val adminLockoutTime: StateFlow<Long> = _adminLockoutTime.asStateFlow()

    // Current Navigation Route
    private val _currentRoute = MutableStateFlow("splash")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    // Transaction History for Logged in User
    val userTransactions: StateFlow<List<TransactionLog>> = _currentUserPhone.flatMapLatest { phone ->
        if (phone == null) flowOf(emptyList()) else repository.getTransactionsForUser(phone)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Advisor Chat States
    private val _aiChatMessages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("أهلاً بك! أنا مستشارك المالي الذكي WAM 🤖 برعاية المطور ماهر أحمد الوتاري. كيف يمكنني مساعدتك في تخطيط ميزانيتك أو تحويلاتك المالية اليوم؟", false)
    ))
    val aiChatMessages: StateFlow<List<ChatMessage>> = _aiChatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // In-app Notification list (Simulated/FCM alternative for instant preview)
    private val _userNotifications = MutableStateFlow<List<String>>(listOf(
        "مرحباً بك في محفظة الماهر موني WAM! حسابك المعتمد جاهز ومؤمن بالكامل بالبروتوكول الذكي."
    ))
    val userNotifications: StateFlow<List<String>> = _userNotifications.asStateFlow()

    // Navigation trigger
    fun navigateTo(route: String) {
        _currentRoute.value = route
    }

    // User Registration
    suspend fun registerUser(
        fullName: String,
        phone: String,
        email: String,
        pass: String
    ): SignUpResult {
        if (fullName.trim().split(" ").size < 3) {
            return SignUpResult.Error("يرجى إدخال الاسم الكامل ثلاثياً على الأقل.")
        }
        if (phone.length < 9) {
            return SignUpResult.Error("رقم الهاتف يجب أن يكون 9 أرقام على الأقل (مثال: 777644670).")
        }
        if (pass.length < 8) {
            return SignUpResult.Error("كلمة المرور يجب أن لا تقل عن 8 خانات لأمان حسابك.")
        }

        // Check if phone exists
        val existing = repository.getUserSync(phone)
        if (existing != null) {
            return SignUpResult.Error("رقم الهاتف هذا مسجل بالفعل في محفظة WAM.")
        }

        val hashedPassword = repository.hashPassword(pass)
        val newUser = User(
            phone = phone,
            fullName = fullName,
            email = email,
            passwordHash = hashedPassword,
            kycStatus = "PENDING"
        )

        repository.insertUser(newUser)
        repository.insertAuditLog("SIGNUP", "تم إنشاء حساب جديد بنجاح للرقم: $phone")
        
        // Log in instantly
        _currentUserPhone.value = phone
        return SignUpResult.Success
    }

    // User Authentication
    suspend fun loginUser(phone: String, pass: String): LoginResult {
        val currentTime = System.currentTimeMillis()
        if (_userLockoutTime.value > currentTime) {
            val remainingSecs = (_userLockoutTime.value - currentTime) / 1000
            return LoginResult.Locked(remainingSecs)
        }

        val user = repository.getUserSync(phone)
        if (user == null) {
            handleFailedLogin()
            return LoginResult.Error("رقم الهاتف أو كلمة المرور غير صحيحة.")
        }

        val typedHash = repository.hashPassword(pass)
        if (user.passwordHash != typedHash) {
            handleFailedLogin()
            return LoginResult.Error("رقم الهاتف أو كلمة المرور غير صحيحة.")
        }

        if (user.isBlocked) {
            return LoginResult.Error("حسابك معلق حالياً من قبل الإدارة. يرجى التواصل برقم الدعم 777644670.")
        }

        // Clear attempts
        _loginAttempts.value = 0
        _userLockoutTime.value = 0
        _currentUserPhone.value = phone
        repository.insertAuditLog("LOGIN_SUCCESS", "تسجيل دخول ناجح للرقم: $phone")
        
        // Check KYC status and route
        return if (user.kycStatus == "PENDING" && phone != "777644670") {
            LoginResult.SuccessNeedKYC
        } else {
            LoginResult.SuccessHome
        }
    }

    private suspend fun handleFailedLogin() {
        val attempts = _loginAttempts.value + 1
        _loginAttempts.value = attempts
        repository.insertAuditLog("LOGIN_FAIL", "محاولة دخول فاشلة للمحفظة. إجمالي المحاولات: $attempts")
        if (attempts >= 3) {
            // Lock for 15 minutes (15 * 60 * 1000)
            _userLockoutTime.value = System.currentTimeMillis() + (15 * 60 * 1000)
            _loginAttempts.value = 0
        }
    }

    fun logout() {
        _currentUserPhone.value = null
        navigateTo("selection")
    }

    // Submit KYC Information
    fun submitKYC(idPath: String, facePath: String) {
        val phone = _currentUserPhone.value ?: return
        viewModelScope.launch {
            val user = repository.getUserSync(phone)
            if (user != null) {
                repository.updateUser(user.copy(
                    kycStatus = "SUBMITTED",
                    idCardPath = idPath,
                    selfiePath = facePath
                ))
                repository.insertAuditLog("KYC_SUBMIT", "تم رفع مستندات الهوية والصورة الشخصية بنجاح للرقم $phone للتحقق")
                navigateTo("home")
            }
        }
    }

    // Admin Authentication Gateway
    suspend fun loginAdmin(user: String, pass: String): AdminLoginResult {
        val currentTime = System.currentTimeMillis()
        if (_adminLockoutTime.value > currentTime) {
            val remainingSecs = (_adminLockoutTime.value - currentTime) / 1000
            return AdminLoginResult.Locked(remainingSecs)
        }

        if (user == "WAM2026" && pass == "maher--736462") {
            _adminLoginAttempts.value = 0
            _adminLockoutTime.value = 0
            repository.insertAuditLog("ADMIN_LOGIN_SUCCESS", "دخول ناجح للوحة تحكم الإدمن الوتاري")
            return AdminLoginResult.Success
        } else {
            val attempts = _adminLoginAttempts.value + 1
            _adminLoginAttempts.value = attempts
            repository.insertAuditLog("ADMIN_LOGIN_FAIL", "محاولة دخول فاشلة للإدمن بقيم اسم المستخدم: $user")
            if (attempts >= 3) {
                // Lock administrator for 30 minutes
                _adminLockoutTime.value = System.currentTimeMillis() + (30 * 60 * 1000)
                _adminLoginAttempts.value = 0
            }
            return AdminLoginResult.Error("بيانات الدخول غير صحيحة. المحاولات الفاشلة: $attempts/3")
        }
    }

    // USSD Secret Bypass to instantly unlock/reset admin portal: *777644670#
    fun executeUSSDBypass() {
        _adminLoginAttempts.value = 0
        _adminLockoutTime.value = 0
        _loginAttempts.value = 0
        _userLockoutTime.value = 0
        viewModelScope.launch {
            repository.insertAuditLog("USSD_BYPASS", "تم إدخال الرمز السري USSD لفك قفل جميع الأنظمة بنجاح")
        }
    }

    // P2P Transfer Process
    suspend fun transferP2P(receiverPhone: String, amount: Double, currency: String): TransferResult {
        val sender = _currentUserPhone.value ?: return TransferResult.Error("رقم المرسِل لم يتم تحديده")
        return repository.performP2PTransfer(sender, receiverPhone, amount, currency)
    }

    // Bill payments flow
    suspend fun payBill(type: String, amount: Double, provider: String): TransferResult {
        val phone = _currentUserPhone.value ?: return TransferResult.Error("غير مسجل الدخول")
        return repository.performBillPayment(phone, type, amount, provider)
    }

    // Mobile recharge flow
    suspend fun rechargeMobile(network: String, amount: Double, target: String): TransferResult {
        val phone = _currentUserPhone.value ?: return TransferResult.Error("غير مسجل الدخول")
        return repository.performMobileRecharge(phone, network, amount, target)
    }

    // Cash transactions
    suspend fun executeDepositOrWithdraw(isDeposit: Boolean, currency: String, amount: Double, agent: String): TransferResult {
        val phone = _currentUserPhone.value ?: return TransferResult.Error("غير مسجل الدخول")
        return repository.performDepositOrWithdraw(phone, isDeposit, currency, amount, agent)
    }

    // --- ADMIN SYSTEM CONTROLS ---

    fun adminToggleBlockUser(userPhone: String, block: Boolean) {
        viewModelScope.launch {
            val user = repository.getUserSync(userPhone)
            if (user != null) {
                repository.updateUser(user.copy(isBlocked = block))
                repository.insertAuditLog(
                    "ADMIN_USER_BLOCK",
                    "قام الإدمن بـ ${if (block) "حظر" else "إلغاء حظر"} الحساب رقم: $userPhone"
                )
            }
        }
    }

    fun adminAdjustUserBalance(userPhone: String, amountYer: Double, amountUsd: Double) {
        viewModelScope.launch {
            val user = repository.getUserSync(userPhone)
            if (user != null) {
                repository.updateUser(user.copy(
                    balanceYer = amountYer,
                    balanceUsd = amountUsd
                ))
                repository.insertAuditLog(
                    "ADMIN_BALANCE_ADJUST",
                    "تعديل رصيد حساب $userPhone يدوياً إلى: YER $amountYer | USD $amountUsd"
                )
            }
        }
    }

    fun adminApproveUserKYC(userPhone: String) {
        viewModelScope.launch {
            val user = repository.getUserSync(userPhone)
            if (user != null) {
                repository.updateUser(user.copy(kycStatus = "APPROVED"))
                repository.insertAuditLog("ADMIN_KYC_APPROVE", "الموافقة الرسمية على وثائق هوية الحساب: $userPhone")
            }
        }
    }

    fun adminSaveSettings(
        customAppName: String,
        welcomeMsg: String,
        p2pFee: Double,
        isFrozen: Boolean,
        digitalEnabled: Boolean
    ) {
        viewModelScope.launch {
            repository.insertAppSetting("app_name", customAppName)
            repository.insertAppSetting("welcome_phrase", welcomeMsg)
            repository.insertAppSetting("p2p_fee_percent", p2pFee.toString())
            repository.insertAppSetting("system_frozen", isFrozen.toString())
            repository.insertAppSetting("enable_digital_assets", digitalEnabled.toString())
            
            repository.insertAuditLog("ADMIN_SETTINGS_UPDATE", "قام الإدمن بتحديث الإعدادات العامة للمحفظة والرسوم")
        }
    }

    // Send notification to users
    fun adminDispatchNotification(message: String) {
        if (message.isBlank()) return
        _userNotifications.update { current ->
            listOf(message) + current
        }
        viewModelScope.launch {
            repository.insertAuditLog("ADMIN_NOTIFICATION", "تم توزيع إشعار عام للمستخدمين: $message")
        }
    }

    // --- GEMINI AI CHAT LOGIC ---

    fun askAiAdvisor(prompt: String) {
        if (prompt.isBlank()) return
        
        // Append user chat message
        _aiChatMessages.update { current ->
            current + ChatMessage(prompt, true)
        }
        _isAiLoading.value = true

        viewModelScope.launch {
            val userObj = currentUser.value
            val userName = userObj?.fullName ?: "ضيف WAM"
            
            val responseText = GeminiClient.generateFinancialAdvice(prompt, userName)
            
            _aiChatMessages.update { current ->
                current + ChatMessage(responseText, false)
            }
            _isAiLoading.value = false
        }
    }
}

// Result classes
sealed class SignUpResult {
    object Success : SignUpResult()
    data class Error(val message: String) : SignUpResult()
}

sealed class LoginResult {
    object SuccessHome : LoginResult()
    object SuccessNeedKYC : LoginResult()
    data class Error(val message: String) : LoginResult()
    data class Locked(val remainingSeconds: Long) : LoginResult()
}

sealed class AdminLoginResult {
    object Success : AdminLoginResult()
    data class Error(val message: String) : AdminLoginResult()
    data class Locked(val remainingSeconds: Long) : AdminLoginResult()
}

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
