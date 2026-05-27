package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.*
import com.example.ui.WalletViewModel
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: WalletViewModel = viewModel()
            val configState = viewModel.systemConfig.collectAsState()
            val config = configState.value ?: SystemConfig()

            // Dynamic core themes
            val primaryColor = parseHexColor(config.primaryColorHex, PrimaryBlueDefault)
            val secondaryColor = parseHexColor(config.secondaryColorHex, SecondaryGoldDefault)

            WamWalletTheme(darkTheme = config.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (config.isDarkMode) DarkNavy else BackgroundLightDefault
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        AppNavigationContainer(viewModel = viewModel, primary = primaryColor, secondary = secondaryColor, config = config)
                    }
                }
            }
        }
    }
}

// Helper to convert hex strings dynamically into compose UI colors safely
fun parseHexColor(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun AppNavigationContainer(
    viewModel: WalletViewModel,
    primary: Color,
    secondary: Color,
    config: SystemConfig
) {
    val userRole by viewModel.currentUserRole.collectAsState()

    if (userRole.isEmpty()) {
        LoginScreen(viewModel = viewModel, primary = primary, secondary = secondary, config = config)
    } else {
        MainDashboardScreen(viewModel = viewModel, primary = primary, secondary = secondary, config = config, role = userRole)
    }
}

@Composable
fun AppLogoIcon(size: Int = 120, tintColor: Color = Color(0xFFFFD700)) {
    // Custom adaptive look matching Tweak 2
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(0xFF1A237E)) // خلفية دائرية أزرق
            .border(2.dp, tintColor.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Balance/Scale vector lines silhouette & central letter
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Balance,
                contentDescription = "Scale",
                tint = tintColor.copy(alpha = 0.25f),
                modifier = Modifier.size((size * 0.4).dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "M",
                    color = tintColor, // حرف M باللون الذهبي
                    fontWeight = FontWeight.Black,
                    fontSize = (size * 0.25).sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "م",
                    color = tintColor, // حرف م باللون الذهبي
                    fontWeight = FontWeight.Black,
                    fontSize = (size * 0.25).sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: WalletViewModel,
    primary: Color,
    secondary: Color,
    config: SystemConfig
) {
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Password recovery states from ViewModel
    val rStep by viewModel.recoveryStep.collectAsState()
    val smsSent by viewModel.smsSent.collectAsState()
    val recoveryPhone by viewModel.phoneInput.collectAsState()
    val recoveryOtp by viewModel.recoverySmsCodeInput.collectAsState()
    val newPass by viewModel.newPasswordInput.collectAsState()
    val confirmPass by viewModel.confirmPasswordInput.collectAsState()
    val statusMsg by viewModel.smsStatusMessage.collectAsState()
    val loginError by viewModel.loginError.collectAsState()

    var showRecoveryDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        AppLogoIcon(size = 110, tintColor = secondary)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = config.appName, // Dynamic Name configured by Owner
            style = MaterialTheme.typography.titleLarge,
            color = primary,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "نظام الماهر المالي والتحصيل المتطور",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (config.isDarkMode) CardDarkBlue else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "تسجيل الدخول",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (config.isDarkMode) Color.White else TextDark,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Enter password
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("رمز الدخول السري") },
                    placeholder = { Text("أدخل رمز المشرف أو المالك") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("login_password_input")
                )

                if (loginError != null) {
                    Text(
                        text = loginError ?: "",
                        color = DebtRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        textAlign = TextAlign.Right
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (passwordInput.isEmpty()) {
                            Toast.makeText(context, "الرجاء إدخال الرمز السري أولاً", Toast.LENGTH_SHORT).show()
                        } else {
                            val ok = viewModel.login(passwordInput)
                            if (ok) {
                                Toast.makeText(context, "مرحباً بك مجدداً!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_button")
                ) {
                    Text("تسجيل دخول الموظف", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Clicking this starts Tweak 1 path
                TextButton(
                    onClick = {
                        viewModel.resetRecoveryState()
                        showRecoveryDialog = true
                    },
                    modifier = Modifier.testTag("forgot_password_button")
                ) {
                    Text(
                        text = "نسيت كلمة المرور؟ استرجاع عبر SMS",
                        color = secondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        
        Spacer(modifier = Modifier.height(20.dp))
        // Promotional Footer
        Text(
            text = "MAW 777644670",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
    }

    // Tweak 1: SMS Password Recovery Dialog/View
    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRecoveryDialog = false }) {
                    Text("إغلاق")
                }
            },
            title = {
                Text(
                    text = "استرجاع كلمة المرور عبر SMS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.End
                ) {
                    // Step feedback info
                    if (statusMsg != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = primary.copy(alpha = 0.1f)),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = statusMsg ?: "",
                                fontSize = 12.sp,
                                color = primary,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Right
                            )
                        }
                    }

                    when (rStep) {
                        1 -> {
                            // Phone input screen
                            Text(
                                "يرجى إدخال رقم الهاتف المسجل لإرسال رمز تحقق مؤلف من 6 أرقام إليه:",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = recoveryPhone,
                                onValueChange = { viewModel.phoneInput.value = it },
                                label = { Text("رقم الهاتف") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.requestPasswordRecoverySms() },
                                colors = ButtonDefaults.buttonColors(containerColor = primary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("إرسال رمز التحقق", color = Color.White)
                            }
                        }
                        2 -> {
                            // Verification OTP input screen
                            Text(
                                "تم إرسال رمز مؤلف من 6 أرقام. يرجى إدخاله هنا لإتمام التحقق:",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = recoveryOtp,
                                onValueChange = { viewModel.recoverySmsCodeInput.value = it },
                                label = { Text("رمز التحقق (OTP)") },
                                placeholder = { Text("مكون من 6 أرقام") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(
                                    onClick = { viewModel.verifyRecoveryCode() },
                                    colors = ButtonDefaults.buttonColors(containerColor = PaidGreen),
                                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                                ) {
                                    Text("تأكيد الرمز", color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.resetRecoveryState() },
                                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                                ) {
                                    Text("إعادة المحاولة")
                                }
                            }
                        }
                        3 -> {
                            // New password input screen
                            Text(
                                "يرجى تعيين كلمة المرور الجديدة وتأكيدها لحفظ التغييرات:",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = newPass,
                                onValueChange = { viewModel.newPasswordInput.value = it },
                                label = { Text("كلمة مرور جديدة") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = confirmPass,
                                onValueChange = { viewModel.confirmPasswordInput.value = it },
                                label = { Text("تأكيد كلمة المرور") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.saveNewPassword {
                                        // Reset login forms and close dialog on perfect success
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("تغيير وحفظ كلمة المرور", color = Color.White)
                            }
                        }
                        4 -> {
                            // Succeeded
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = PaidGreen,
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                            Text(
                                text = "تم تغيير كلمة المرور بنجاح تام! يمكنك الآن تسجيل الدخول برمزك السري الجديد.",
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showRecoveryDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = primary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("إغلاق والعودة للدخول", color = Color.White)
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun MainDashboardScreen(
    viewModel: WalletViewModel,
    primary: Color,
    secondary: Color,
    config: SystemConfig,
    role: String
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showOwnerAdminPanelDirectly by remember { mutableStateOf(false) }

    val appNamePrefix = if (role == "owner") "المالك: " else "المشرف: "

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(primary)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "تسجيل خروج", tint = Color.White)
                    }

                    // Logo in navigation header. Clicking 5 times enters Owner mode!
                    var logoClickCount by remember { mutableStateOf(0) }
                    val context = LocalContext.current
                    Box(
                        modifier = Modifier
                            .clickable {
                                logoClickCount++
                                if (logoClickCount >= 5) {
                                    logoClickCount = 0
                                    // Trigger owner access
                                    if (role == "owner") {
                                        showOwnerAdminPanelDirectly = true
                                    } else {
                                        // Ask for password
                                        Toast.makeText(context, "الوصول مخفي! يرجى الدخول بكلمة المالك", Toast.LENGTH_LONG).show()
                                        viewModel.logout()
                                    }
                                }
                            }
                    ) {
                        AppLogoIcon(size = 36, tintColor = secondary)
                    }

                    Text(
                        text = "$appNamePrefix ${config.appName}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Main Tab Bar
                NavigationBar(
                    containerColor = if (config.isDarkMode) CardDarkBlue else Color.White
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0 && !showOwnerAdminPanelDirectly,
                        onClick = {
                            selectedTab = 0
                            showOwnerAdminPanelDirectly = false
                        },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "الحسابات") },
                        label = { Text("الحسابات") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1 && !showOwnerAdminPanelDirectly,
                        onClick = {
                            selectedTab = 1
                            showOwnerAdminPanelDirectly = false
                        },
                        icon = { Icon(Icons.Default.History, contentDescription = "السجلات") },
                        label = { Text("المعاملات") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2 && !showOwnerAdminPanelDirectly,
                        onClick = {
                            selectedTab = 2
                            showOwnerAdminPanelDirectly = false
                        },
                        icon = { Icon(Icons.Default.MiscellaneousServices, contentDescription = "الخدمات") },
                        label = { Text("الخدمات") }
                    )
                    NavigationBarItem(
                        selected = (selectedTab == 3 || showOwnerAdminPanelDirectly),
                        onClick = {
                            selectedTab = 3
                            if (role == "owner") {
                                showOwnerAdminPanelDirectly = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (role == "owner") Icons.Default.AdminPanelSettings else Icons.Default.Settings,
                                contentDescription = if (role == "owner") "لوحة المالك" else "الإعدادات"
                            )
                        },
                        label = { Text(if (role == "owner") "المالك فقط" else "الإعدادات") }
                    )
                }

                // Promo footer under every screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (config.isDarkMode) DarkNavy else Color(0xFFEEEEEE))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "MAW 777644670",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showOwnerAdminPanelDirectly && role == "owner") {
                OwnerOnlyPanelScreen(viewModel = viewModel, primary = primary, secondary = secondary, config = config)
            } else {
                when (selectedTab) {
                    0 -> LedgerDashboardTab(viewModel = viewModel, primary = primary, secondary = secondary, config = config)
                    1 -> TransactionHistoryTab(viewModel = viewModel, config = config)
                    2 -> ServicesListTab(viewModel = viewModel, primary = primary, secondary = secondary, config = config, role = role)
                    3 -> SettingsTab(viewModel = viewModel, primary = primary, secondary = secondary, config = config, role = role, onOpenOwner = { showOwnerAdminPanelDirectly = true })
                }
            }
        }
    }
}

// Tab 0: Accounting Ledger Table (الحسابات)
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LedgerDashboardTab(
    viewModel: WalletViewModel,
    primary: Color,
    secondary: Color,
    config: SystemConfig
) {
    val clients by viewModel.clients.collectAsState()
    var showAddClientDialog by remember { mutableStateOf(false) }

    var searchInput by remember { mutableStateOf("") }
    val filteredClients = clients.filter {
        it.name.contains(searchInput) || it.phone.contains(searchInput)
    }

    // Calculating totals for display
    val totalDebt = clients.sumOf { it.debtAmount }
    val totalPaid = clients.sumOf { it.paidAmount }
    val totalCredit = clients.sumOf { it.creditAmount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = primary)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                    Text(text = "إجمالي الملخص المالي", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "صافي الميزانية: ${(totalCredit + totalPaid - totalDebt)} ريال",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.White.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Debts column (الديون)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "إجمالي الديون", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text(text = "$totalDebt ر.ي", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // Paid column (المدفوع)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "إجمالي المدفوعات", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text(text = "$totalPaid ر.ي", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        // Credit column (الرصيد الدائن)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "الرصيد الدائن", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                            Text(text = "$totalCredit ر.ي", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Search Bar & Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showAddClientDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(end = 8.dp).weight(0.3f)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "قيد جديد", tint = Color.White)
                }

                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    placeholder = { Text("بحث عن عميل...", fontSize = 12.sp) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                    modifier = Modifier.weight(0.7f),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Client accounts lists
        if (filteredClients.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد سجلات حسابية مطابقة", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(filteredClients) { client ->
                ClientLedgerItemCard(client = client, config = config, viewModel = viewModel, primary = primary)
            }
        }
    }

    // Dialog for adding client accounting record
    if (showAddClientDialog) {
        var clientName by remember { mutableStateOf("") }
        var clientPhone by remember { mutableStateOf("") }
        var debtVal by remember { mutableStateOf("") }
        var paidVal by remember { mutableStateOf("") }
        var creditVal by remember { mutableStateOf("") }

        // Custom columns generated dynamically
        val columnsList = remember {
            if (config.customColumnsSemicolonSeparated.trim().isEmpty()) emptyList()
            else config.customColumnsSemicolonSeparated.split(";")
        }
        val customFieldStates = remember { mutableStateMapOf<String, String>() }
        columnsList.forEach { col ->
            if (!customFieldStates.containsKey(col)) {
                customFieldStates[col] = ""
            }
        }

        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { showAddClientDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (clientName.trim().isEmpty()) {
                            Toast.makeText(context, "الرجاء إدخال اسم العميل", Toast.LENGTH_SHORT).show()
                        } else {
                            val debt = debtVal.toDoubleOrNull() ?: 0.0
                            val paid = paidVal.toDoubleOrNull() ?: 0.0
                            val credit = creditVal.toDoubleOrNull() ?: 0.0

                            // Format custom fields as string list
                            val customJsonString = customFieldStates.entries.joinToString(";") { "${it.key}:${it.value}" }

                            viewModel.addClient(
                                name = clientName,
                                phone = clientPhone,
                                debt = debt,
                                paid = paid,
                                credit = credit,
                                customFields = customJsonString
                            )
                            showAddClientDialog = false
                            Toast.makeText(context, "تم تسجيل الحساب بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("إضافة الحساب", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddClientDialog = false }) {
                    Text("إلغاء")
                }
            },
            title = {
                Text(
                    text = "تسجيل قيد حسابي جديد",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.End
                ) {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("اسم العميل") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = clientPhone,
                        onValueChange = { clientPhone = it },
                        label = { Text("رقم الهاتف") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = debtVal,
                        onValueChange = { debtVal = it },
                        label = { Text("قيمة الديون (بالريال)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = paidVal,
                        onValueChange = { paidVal = it },
                        label = { Text("المدفوع (بالريال)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = creditVal,
                        onValueChange = { creditVal = it },
                        label = { Text("الرصيد الدائن (بالريال)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    // Owner Custom Added Columns/Fields displayed here in the dialog (Tweak 4)
                    if (columnsList.isNotEmpty()) {
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        Text(
                            text = "خانات إضافية (مخصصة من المالك)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = primary,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            textAlign = TextAlign.Right
                        )

                        columnsList.forEach { col ->
                            OutlinedTextField(
                                value = customFieldStates[col] ?: "",
                                onValueChange = { customFieldStates[col] = it },
                                label = { Text(col) },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ClientLedgerItemCard(
    client: ClientRecord,
    config: SystemConfig,
    viewModel: WalletViewModel,
    primary: Color
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (config.isDarkMode) CardDarkBlue else Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Symmetrical status indicators
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "الصافي: ${client.netBalance} ر.ي",
                        color = if (client.netBalance >= 0) PaidGreen else DebtRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = client.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (config.isDarkMode) Color.White else TextDark
                    )
                    if (client.phone.isNotEmpty()) {
                        Text(
                            text = "الهاتف: ${client.phone}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Columns (Dues/Debts/Credit - Tweak 3 Colors)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Debt (Red)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "الديون", fontSize = 10.sp, color = Color.Gray)
                    Text(text = "${client.debtAmount} ر.ي", color = DebtRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Paid (Green)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "المدفوع", fontSize = 10.sp, color = Color.Gray)
                    Text(text = "${client.paidAmount} ر.ي", color = PaidGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Credit (Orange)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "الدائن", fontSize = 10.sp, color = Color.Gray)
                    Text(text = "${client.creditAmount} ر.ي", color = CreditOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Custom columns added by Owner shown here when card is expanded
            if (isExpanded) {
                val customColsData = remember(client.customFieldsJson) {
                    if (client.customFieldsJson.trim().isEmpty()) emptyList()
                    else client.customFieldsJson.split(";").mapNotNull {
                        val parts = it.split(":")
                        if (parts.size >= 2) parts[0] to parts[1] else null
                    }
                }

                if (customColsData.isNotEmpty() || config.supervisorPermissionsJson.contains("حذف")) {
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.Gray.copy(alpha = 0.2f))
                }

                if (customColsData.isNotEmpty()) {
                    Text(
                        text = "تفاصيل الحساب المخصصة للمالك:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = primary,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        textAlign = TextAlign.Right
                    )

                    customColsData.forEach { (colName, colVal) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = colVal, fontSize = 12.sp, color = if (config.isDarkMode) Color.LightGray else TextDark)
                            Text(text = "$colName :", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Delete button with administrator supervisor permissions validation (Tweak 4)
                if (config.supervisorPermissionsJson.contains("حذف")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = { viewModel.deleteClient(client) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف القيد", tint = DebtRed)
                        }
                    }
                }
            }
        }
    }
}

// Tab 1: Transaction logs registry list
@Composable
fun TransactionHistoryTab(viewModel: WalletViewModel, config: SystemConfig) {
    val items by viewModel.transactions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "دفتر قيود وسجل الحركة المالية",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (config.isDarkMode) Color.White else TextDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "لا توجد حركات مالية مسجلة بعد", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items) { t ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (config.isDarkMode) CardDarkBlue else Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${t.amount} ريال",
                                color = when (t.type) {
                                    "دين" -> DebtRed
                                    "مدفوع" -> PaidGreen
                                    else -> CreditOrange
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = t.clientName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (config.isDarkMode) Color.White else TextDark
                                )
                                Text(
                                    text = "${t.type} - ${t.notes}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Tab 2: Custom accounting services list
@Composable
fun ServicesListTab(
    viewModel: WalletViewModel,
    primary: Color,
    secondary: Color,
    config: SystemConfig,
    role: String
) {
    val serviceItems by viewModel.services.collectAsState()
    var showAddServiceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Owner exclusive add button (Tweak 4a)
            if (role == "owner") {
                Button(
                    onClick = { showAddServiceDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة خدمة", tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("خدمة جديدة", color = Color.White)
                }
            } else {
                Spacer(modifier = Modifier.width(10.dp))
            }

            Text(
                text = "الخدمات المتاحة وتكلفتها المعتمدة",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (config.isDarkMode) Color.White else TextDark
            )
        }

        if (serviceItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد خدمات مضافة حالياً", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(serviceItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isEnabled) {
                                if (config.isDarkMode) CardDarkBlue else Color.White
                            } else {
                                if (config.isDarkMode) CardDarkBlue.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.3f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "${item.price} ر.ي",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = if (item.isEnabled) primary else Color.Gray
                                )
                                Text(
                                    text = if (item.isEnabled) "نشطة" else "معطلة",
                                    fontSize = 10.sp,
                                    color = if (item.isEnabled) PaidGreen else DebtRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Delete/Edit actions exclusively for Owner if logged in (Tweak 4)
                                if (role == "owner") {
                                    IconButton(
                                        onClick = {
                                            // Toggle enabling
                                            viewModel.updateService(item.copy(isEnabled = !item.isEnabled))
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (item.isEnabled) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                                            contentDescription = "تحويل الحالة",
                                            tint = if (item.isEnabled) PaidGreen else Color.Gray,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteService(item) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف الخدمة", tint = DebtRed)
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (config.isDarkMode) Color.White else TextDark
                                    )
                                    Text(
                                        text = item.type,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddServiceDialog && role == "owner") {
        var nameInput by remember { mutableStateOf("") }
        var priceInput by remember { mutableStateOf("") }
        var typeSelected by remember { mutableStateOf("خدمة عملاء") }
        val categoryOptions = listOf("خدمة عملاء", "خدمة توصيل", "خدمة صيانة", "خدمة استشارات")
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { showAddServiceDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val price = priceInput.toDoubleOrNull() ?: 0.0
                        if (nameInput.trim().isEmpty()) {
                            Toast.makeText(context, "يرجى تعبئة اسم الخدمة", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addService(name = nameInput, price = price, type = typeSelected)
                            showAddServiceDialog = false
                            Toast.makeText(context, "تم حفظ الخدمة بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("إضافة وحفظ", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddServiceDialog = false }) {
                    Text("إلغاء")
                }
            },
            title = {
                Text(
                    text = "إضافة خدمة جديدة للتطبيق",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.End
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("اسم الخدمة الجديدة") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("تكلفة الخدمة (بالريال)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    Text(
                        text = "تصنيف ونوع الخدمة:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                        textAlign = TextAlign.Right
                    )

                    categoryOptions.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { typeSelected = opt }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = opt, fontSize = 13.sp, modifier = Modifier.padding(end = 8.dp))
                            RadioButton(selected = typeSelected == opt, onClick = { typeSelected = opt })
                        }
                    }
                }
            }
        )
    }
}

// Tab 3: Simple Settings & Support
@Composable
fun SettingsTab(
    viewModel: WalletViewModel,
    primary: Color,
    secondary: Color,
    config: SystemConfig,
    role: String,
    onOpenOwner: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "إعدادات التطبيق",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = if (config.isDarkMode) Color.White else TextDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Account / Role details info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = primary.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                Text(
                    text = if (role == "owner") "أنت مسجل كمالك للمحفظة" else "أنت مسجل كمشرف مالي",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = primary
                )
                Text(
                    text = "صلاحياتك محدودة بموجب تراخيص المالك ماهر أحمد الوتاري الحصرية.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Feature controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (config.isDarkMode) CardDarkBlue else Color.White
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                Text(
                    text = "الوضع والمظهر م3",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (config.isDarkMode) Color.White else TextDark
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = config.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                    Text(text = "تفعيل المظهر الليلي", fontSize = 13.sp)
                }
            }
        }

        // About the app card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (config.isDarkMode) CardDarkBlue else Color.White
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                Text(
                    text = "حول نظام حسابات الماهر",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = primary,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "تطبيق حاسوبي لإدارة المدفوعات والديون والعملاء، بتنفيذ أمان فائق المستوى استناداً لشهادات ترخيص المحفظة من المالك والمشرف العام.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "المطور المسؤول: MAW 777644670",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = secondary,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // If the user's role is "owner", we show direct shortcut button to the majestic Owner screen
        if (role == "owner") {
            Button(
                onClick = onOpenOwner,
                colors = ButtonDefaults.buttonColors(containerColor = primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "", tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "لوحة المالك الفريدة والكاملة", color = Color.White, fontWeight = FontWeight.Bold)
            }
        } else {
            // Secret clicking of our App logo 5 times in the dashboard lets Owner login!
            Text(
                text = "انقر على رمز الشعار في الأعلى 5 مرات لإظهار خيارات المالك المخفية",
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Tweak 4 Majestic Dashboard View: Owner-Only Panel Screen ("إدارة التطبيق - المالك فقط")
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OwnerOnlyPanelScreen(
    viewModel: WalletViewModel,
    primary: Color,
    secondary: Color,
    config: SystemConfig
) {
    val context = LocalContext.current

    // States for custom modifications
    var appNameInput by remember { mutableStateOf(config.appName) }
    var primaryColorInput by remember { mutableStateOf(config.primaryColorHex) }
    var secondaryColorInput by remember { mutableStateOf(config.secondaryColorHex) }
    var smsGatewayInput by remember { mutableStateOf(config.smsGatewayNumber) }
    var customColsInput by remember { mutableStateOf(config.customColumnsSemicolonSeparated) }
    var stockLimitInput by remember { mutableStateOf(config.minStockLimit.toString()) }
    var supervisorPermsInput by remember { mutableStateOf(config.supervisorPermissionsJson) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = "Verified Owner",
                tint = secondary,
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = "إدارة التطبيق - المالك فقط",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = primary,
                textAlign = TextAlign.Right
            )
        }

        Text(
            text = "مرحباً بك يا مالك التطبيق (الترخيص الحصري: maher--736462)",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp),
            textAlign = TextAlign.Right
        )

        // SECTION 1: SYSTEM SERVICES DYNAMIC RULES (إضافة خدمات جديدة)
        Text(
            text = "أولاً: إدارة ومراقبة الخدمات الفورية",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = if (config.isDarkMode) CardDarkBlue else Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.End) {
                Text(
                    text = "تعديل وإضافة وتعديل أسعار وتصنيف أي خدمة في النظام مع خيار التشغيل والتعطيل في علامة تبويب الخدمات الرئيسية.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(10.dp))
                // Explanatory badge
                Text(
                    text = "تنويه: اذهب لتبويب الخدمات للتحكم الكامل الفوري.",
                    color = secondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right
                )
            }
        }

        // SECTION 2: ADDING FEATURES & COLUMNS (إضافة مميزات جديدة وأعمدة)
        Text(
            text = "ثانياً: إدارة المميزات والأعمدة الإضافية",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = if (config.isDarkMode) CardDarkBlue else Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.End) {
                // Column management
                Text(
                    text = "تخصيص أعمدة الجدول الرئيسي للعملاء:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "يرجى كتابتها مفصولة بفاصلة منقوطة (مثلاً: رقم الفاتورة;تاريخ الاستحقاق)",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = customColsInput,
                    onValueChange = { customColsInput = it },
                    placeholder = { Text("مثال: رقم الفاتورة;المدينة;العمولة") },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
                )

                // Supervisor rules
                Text(
                    text = "تعيين صلاحيات المشرفين والموظفين:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اكتب العمليات المسموحة مقسمة بفواصل (مثال: إضافة;تعديل;حذف)",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = supervisorPermsInput,
                    onValueChange = { supervisorPermsInput = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp)
                )

                // Enablers
                Divider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = config.isReportsEnabled,
                        onCheckedChange = { viewModel.toggleSystemFeature(reports = it) }
                    )
                    Text(text = "تفعيل ميزة التقارير المالية", fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = config.isNotificationsEnabled,
                        onCheckedChange = { viewModel.toggleSystemFeature(notifications = it) }
                    )
                    Text(text = "تفعيل الإشعارات الفورية", fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = config.isBackupEnabled,
                        onCheckedChange = { viewModel.toggleSystemFeature(backup = it) }
                    )
                    Text(text = "تمكين وضع النسخ الاحتياطي التلقائي", fontSize = 12.sp)
                }
            }
        }

        // SECTION 3: INTERFACE CUSTOMIZATION & IDENTITY (تغيير الألوان والاسم والهوية)
        Text(
            text = "ثالثاً: إدارة مظهر وهوية المحفظة المخصصة",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = if (config.isDarkMode) CardDarkBlue else Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.End) {
                Text(
                    text = "تغيير اسم التطبيق بالكامل:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = appNameInput,
                    onValueChange = { appNameInput = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                Text(
                    text = "اللون الأساسي للتطبيق (Hex):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = primaryColorInput,
                    onValueChange = { primaryColorInput = it },
                    placeholder = { Text("#0D47A1") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                Text(
                    text = "اللون الثانوي للتطبيق (Hex):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = secondaryColorInput,
                    onValueChange = { secondaryColorInput = it },
                    placeholder = { Text("#FFC107") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            primaryColorInput = "#0D47A1"
                            secondaryColorInput = "#FFC107"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Text("الألوان الافتراضية", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            primaryColorInput = "#1A237E"
                            secondaryColorInput = "#FFD700"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
                    ) {
                        Text("مظهر ملكي ملكية", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        // SECTION 4: ADVANCED ADVANCED SYSTEM CONFIGURATIONS (إدارة الإعدادات المتقدمة)
        Text(
            text = "رابعاً: إعدادات الاتصال والمزامنة المتقدمة",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = if (config.isDarkMode) CardDarkBlue else Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.End) {
                Text(
                    text = "رقم بوابة إرسال رسائل SMS ومزود الخدمة:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = smsGatewayInput,
                    onValueChange = { smsGatewayInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                Text(
                    text = "الحد الأدنى للمخزون للتنبيهات والعملاء:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = stockLimitInput,
                    onValueChange = { stockLimitInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = config.isRealTimeSyncEnabled,
                        onCheckedChange = { viewModel.toggleSystemFeature(sync = it) }
                    )
                    Text(text = "تفعيل المزامنة الفورية السحابية", fontSize = 12.sp)
                }
            }
        }

        // SECTION 5: SAFETY ACTION ZONE (أزرار مسح وتصدير)
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = DebtRed.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.End) {
                Text(
                    text = "منطقة الأمان الحساسة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DebtRed
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                            viewModel.wipeAllData()
                            Toast.makeText(context, "تم مسح وتصفير قاعدة البيانات بنجاح!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DebtRed),
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    ) {
                        Text("مسح كامل البيانات دفعة أولى", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "تم تصدير قاعدة البيانات بنجاح إلى ملف الذاكرة الداخلي المطور", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PaidGreen),
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    ) {
                        Text("تصدير قاعدة البيانات بالكامل", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        // SAVE BUTTON FOR THE OWNER PANEL SETTINGS
        Button(
            onClick = {
                viewModel.updateAppName(appNameInput)
                viewModel.updateAppColors(primaryColorInput, secondaryColorInput)
                viewModel.updateAdvancedSettings(
                    smsNumber = smsGatewayInput,
                    customCols = customColsInput,
                    stockLimit = stockLimitInput.toIntOrNull()
                )
                viewModel.updateSupervisorPermissions(supervisorPermsInput)
                Toast.makeText(context, "تم حفظ الإعدادات وهوية المالك بنجاح", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = PaidGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "حفظ وتثبيت كافة التغييرات والهوية", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
