package com.example.ui

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.data.AuditLog
import com.example.data.TransactionLog
import com.example.data.User
import com.example.data.TransferResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Custom Color constants matching Maher Alwatari's theme specs
val PrimaryBlueVal = Color(0xFF0A2463)
val SecondaryBlueVal = Color(0xFF3A7CA5)
val AccentElectricBlueVal = Color(0xFF00B4D8)
val DarkBackgroundVal = Color(0xFF0D0D0D)
val CardDarkBlueVal = Color(0xFF1A1A2E)
val AlertSuccessGreen = Color(0xFF00E676)
val AlertErrorRed = Color(0xFFFF3B30)

// Standard Gradient Brush for background/aesthetic cards
val DarkBlueGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0F1E36), DarkBackgroundVal)
)
val ActiveCardGradient = Brush.linearGradient(
    colors = listOf(PrimaryBlueVal, Color(0xFF1E3A8A), AccentElectricBlueVal)
)

// -------------------------------------------------------------
// 1. Splash Screen
// -------------------------------------------------------------
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackgroundVal),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // WAM Logo styled with dynamic pulse glow
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryBlueVal, AccentElectricBlueVal)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "WAM",
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "WAM | الماهر موني",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "ماهر أحمد الوتاري",
                color = AccentElectricBlueVal,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "الجيل الجديد للأمان المالي الذكي",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// -------------------------------------------------------------
// 2. Selection Screen
// -------------------------------------------------------------
@Composable
fun SelectionScreen(
    viewModel: WalletViewModel,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onAdminSuccess: () -> Unit
) {
    var logoTapCount by remember { mutableStateOf(0) }
    var showAdminDialog by remember { mutableStateOf(false) }

    // Admin fields
    var adminUser by remember { mutableStateOf("") }
    var adminPass by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var dialogLockedTimeLeft by remember { mutableStateOf<Long>(0) }
    
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlueGradient)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Spacing / App Header
            Spacer(modifier = Modifier.height(40.dp))

            // Center Logo and Branding Interactive Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    logoTapCount++
                    if (logoTapCount >= 5) {
                        logoTapCount = 0
                        showAdminDialog = true
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(ActiveCardGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "WAM",
                        color = Color.White,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "الماهر موني",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "بروتوكول مالي ذكي وسريع",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Bottom Buttons & Support details
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueVal),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("تسجيل الدخول", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onSignUpClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentElectricBlueVal),
                    border = BorderStroke(1.5.dp, AccentElectricBlueVal),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("إنشاء حساب جديد", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Support section conforming to owner constraints
                Text(
                    text = "رقم الدعم: 777644670",
                    color = Color.LightGray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "جميع الحقوق محفوظة WAM 2026 ©",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }

    // Hidden Admin Portal Authentication Modal Dialog
    if (showAdminDialog) {
        AlertDialog(
            onDismissRequest = { showAdminDialog = false },
            containerColor = CardDarkBlueVal,
            title = {
                Text(
                    text = "بوابة الإدمن السرية WAM",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "هذه البوابة مخصصة حصراً للأستاذ ماهر أحمد الوتاري لإدارة حسابات وإعدادات الشبكة.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = adminUser,
                        onValueChange = { adminUser = it },
                        label = { Text("اسم المستخدم", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentElectricBlueVal,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = adminPass,
                        onValueChange = { adminPass = it },
                        label = { Text("كلمة المرور", color = Color.Gray) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentElectricBlueVal,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(msg, color = AlertErrorRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    if (dialogLockedTimeLeft > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "بوابة الإدمن مقفلة أمنياً لـ $dialogLockedTimeLeft ثانية بسبب محاولات خاطئة متكررة.",
                            color = AlertErrorRed,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val result = viewModel.loginAdmin(adminUser, adminPass)
                            when (result) {
                                is AdminLoginResult.Success -> {
                                    showAdminDialog = false
                                    adminUser = ""
                                    adminPass = ""
                                    errorMessage = null
                                    onAdminSuccess()
                                }
                                is AdminLoginResult.Error -> {
                                    errorMessage = result.message
                                }
                                is AdminLoginResult.Locked -> {
                                    dialogLockedTimeLeft = result.remainingSeconds
                                    errorMessage = "البوابة مغلقة حالياً!"
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentElectricBlueVal)
                ) {
                    Text("تسجيل دخول المشرف", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminDialog = false }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 3. Sign Up Screen
// -------------------------------------------------------------
@Composable
fun SignUpScreen(
    viewModel: WalletViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlueGradient)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "رجوع", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("إنشاء حساب جديد", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlueVal),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PersonAdd, "إضافة شخص", tint = AccentElectricBlueVal, modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardDarkBlueVal),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("الاسم الكامل (ثلاثياً على الأقل)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentElectricBlueVal,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف (مثال: 777644670)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentElectricBlueVal,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني (اختياري)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentElectricBlueVal,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text("إنشاء كلمة مرور (8 خانات على الأقل)", color = Color.Gray) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentElectricBlueVal,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("تأكيد كلمة المرور", color = Color.Gray) },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentElectricBlueVal,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorMsg?.let { msg ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = msg,
                            color = AlertErrorRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (pass != confirmPass) {
                                errorMsg = "كلمتا المرور غير متوافقتين."
                                return@Button
                            }
                            isLoading = true
                            errorMsg = null
                            scope.launch {
                                val result = viewModel.registerUser(fullName, phone, email, pass)
                                isLoading = false
                                when (result) {
                                    is SignUpResult.Success -> {
                                        onSuccess()
                                    }
                                    is SignUpResult.Error -> {
                                        errorMsg = result.message
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentElectricBlueVal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("إنشاء حساب WAM", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onBack) {
                Text("لديك حساب بالفعل؟ تسجيل الدخول", color = AccentElectricBlueVal, fontSize = 15.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Login Screen
// -------------------------------------------------------------
@Composable
fun LoginScreen(
    viewModel: WalletViewModel,
    onBack: () -> Unit,
    onSuccessHome: () -> Unit,
    onSuccessKYC: () -> Unit,
    onSignUpClick: () -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var userLockedSeconds by remember { mutableStateOf<Long>(0) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlueGradient)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "رجوع", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل الدخول", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlueVal),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, "أمان", tint = AccentElectricBlueVal, modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(30.dp))
            Text(
                "مرحباً بعودتك",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "أدخل رقم هاتفك وكلمة المرور للتحقق",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardDarkBlueVal),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentElectricBlueVal,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    var passwordVisible by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text("كلمة المرور", color = Color.Gray) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "رؤية كلمة المرور",
                                    tint = Color.Gray
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentElectricBlueVal,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    errorMsg?.let { msg ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = msg,
                            color = AlertErrorRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (userLockedSeconds > 0) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "المحفظة مقفلة أمنياً لـ $userLockedSeconds ثانية بعد 3 محاولات خاطئة متتالية.",
                            color = AlertErrorRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            isLoading = true
                            errorMsg = null
                            scope.launch {
                                val result = viewModel.loginUser(phone, pass)
                                isLoading = false
                                when (result) {
                                    is LoginResult.SuccessHome -> {
                                        onSuccessHome()
                                    }
                                    is LoginResult.SuccessNeedKYC -> {
                                        onSuccessKYC()
                                    }
                                    is LoginResult.Error -> {
                                        errorMsg = result.message
                                    }
                                    is LoginResult.Locked -> {
                                        userLockedSeconds = result.remainingSeconds
                                        errorMsg = "المحفظة مقفلة مؤقتاً!"
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlueVal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("دخول إلى المحفظة", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "نسيت كلمة المرور؟ يرجى التواصل بالدعم: 777644670",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(onClick = onSignUpClick) {
                Text("ليس لديك حساب؟ إنشاء حساب جديد", color = AccentElectricBlueVal, fontSize = 15.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// 5. KYC Identity verification
// -------------------------------------------------------------
@Composable
fun KYCScreen(
    viewModel: WalletViewModel,
    onSuccess: () -> Unit
) {
    var idSubmitted by remember { mutableStateOf(false) }
    var selfieSubmitted by remember { mutableStateOf(false) }
    var notificationMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlueGradient)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDarkBlueVal),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.ContactPage,
                    "وثائق",
                    tint = AccentElectricBlueVal,
                    modifier = Modifier.size(60.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "تحقق من هويتك",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "تطلب القوانين المالية والمصرفية لبروتوكول WAM الآمن التحقق من الهوية لتشغيل حسابك المعتمد.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Document Selection 1: ID Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (idSubmitted) Color(0xFF0D3220) else Color(0xFF1E293B))
                        .clickable { idSubmitted = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (idSubmitted) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                            contentDescription = "رفع الهوية",
                            tint = if (idSubmitted) AlertSuccessGreen else AccentElectricBlueVal
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("رفع صورة الهوية", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("جواز سفر أو بطاقة شخصية", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                    if (idSubmitted) {
                        Text("تم الاختيار", color = AlertSuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Document Selection 2: Selfie
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selfieSubmitted) Color(0xFF0D3220) else Color(0xFF1E293B))
                        .clickable { selfieSubmitted = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (selfieSubmitted) Icons.Default.CheckCircle else Icons.Default.Face,
                            contentDescription = "صورة شخصية",
                            tint = if (selfieSubmitted) AlertSuccessGreen else AccentElectricBlueVal
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("رفع صورة شخصية (Selfie)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("مباشرة تظهر ملامح الوجه بوضوح", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                    if (selfieSubmitted) {
                        Text("تم الإلتقاط", color = AlertSuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                notificationMsg?.let { msg ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(msg, color = AlertErrorRed, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(26.dp))

                Button(
                    onClick = {
                        if (!idSubmitted || !selfieSubmitted) {
                            notificationMsg = "يرجى رفع كلا الصورتين الموضحتين أعلاه."
                        } else {
                            viewModel.submitKYC("mock_id_card.webp", "mock_selfie_photo.webp")
                            onSuccess()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentElectricBlueVal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("إرسال للتحقق المالي", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "🔒 بياناتك آمنة ومشفرة بالكامل بدقة متناهية تحت الإشراف الشخصي للأستاذ ماهر الوتاري.",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 6. Home Main screen wrapper and tabs
// -------------------------------------------------------------
@Composable
fun MainTabHost(
    viewModel: WalletViewModel,
    onLogoutClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    var selectedIndex by remember { mutableStateOf(0) }
    val userState by viewModel.currentUser.collectAsState()

    // Dialog state controllers for active actions on dashboard
    var activeActionDialog by remember { mutableStateOf<String?>(null) }

    val appTitleStr by viewModel.appTitle.collectAsState()

    Scaffold(
        containerColor = DarkBackgroundVal,
        bottomBar = {
            NavigationBar(
                containerColor = CardDarkBlueVal,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = selectedIndex == 0,
                    onClick = { selectedIndex = 0 },
                    icon = { Icon(Icons.Default.Home, "الرئيسية") },
                    label = { Text("الرئيسية", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = AccentElectricBlueVal,
                        indicatorColor = PrimaryBlueVal,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = selectedIndex == 1,
                    onClick = { selectedIndex = 1 },
                    icon = { Icon(Icons.Default.Widgets, "الخدمات") },
                    label = { Text("الخدمات", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = AccentElectricBlueVal,
                        indicatorColor = PrimaryBlueVal,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = selectedIndex == 2,
                    onClick = { selectedIndex = 2 },
                    icon = { Icon(Icons.Default.TrendingUp, "الاستثمار") },
                    label = { Text("الاستثمار", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = AccentElectricBlueVal,
                        indicatorColor = PrimaryBlueVal,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    selected = selectedIndex == 3,
                    onClick = { selectedIndex = 3 },
                    icon = { Icon(Icons.Default.AccountCircle, "ملفي") },
                    label = { Text("ملفي", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = AccentElectricBlueVal,
                        indicatorColor = PrimaryBlueVal,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedIndex) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    user = userState,
                    onActionSelected = { activeActionDialog = it },
                    onAdminPortalRedirect = onAdminClick
                )

                1 -> ServicesScreen(
                    viewModel = viewModel,
                    user = userState,
                    onActionSelected = { activeActionDialog = it }
                )

                2 -> InvestmentScreen(viewModel = viewModel, user = userState)
                3 -> ProfileScreen(viewModel = viewModel, user = userState, onLogout = onLogoutClick)
            }
        }
    }

    // Active Dialogue Box Panels
    when (activeActionDialog) {
        "p2p" -> P2PTransferDialog(viewModel = viewModel, onDismiss = { activeActionDialog = null })
        "bills" -> BillsDialog(viewModel = viewModel, onDismiss = { activeActionDialog = null })
        "recharge" -> RechargeDialog(viewModel = viewModel, onDismiss = { activeActionDialog = null })
        "cash" -> CashDepositWithdrawDialog(viewModel = viewModel, onDismiss = { activeActionDialog = null })
        "history" -> WalletHistoryDialog(viewModel = viewModel, onDismiss = { activeActionDialog = null })
        "ai" -> AISmartAdvisorDialog(viewModel = viewModel, onDismiss = { activeActionDialog = null })
    }
}

// -------------------------------------------------------------
// Sub-Tab 0: Core Dashboard
// -------------------------------------------------------------
@Composable
fun DashboardScreen(
    viewModel: WalletViewModel,
    user: User?,
    onActionSelected: (String) -> Unit,
    onAdminPortalRedirect: () -> Unit
) {
    val welcomeText by viewModel.welcomePhrase.collectAsState()
    val notificationState by viewModel.userNotifications.collectAsState()

    var activeLogoCount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlueGradient)
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header conforming to owner brand rules
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Interactive secret click WAM Logo
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ActiveCardGradient)
                        .clickable {
                            activeLogoCount++
                            if (activeLogoCount >= 5) {
                                activeLogoCount = 0
                                onAdminPortalRedirect()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("WAM", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(welcomeText, color = Color.Gray, fontSize = 11.sp)
                    // Owner exact layout name
                    Text("ماهر أحمد الوتاري", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("مالك ومصمم التطبيق", color = AccentElectricBlueVal, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Notification Ring Icon
            IconButton(onClick = { /* Simulated */ }) {
                Box {
                    Icon(Icons.Default.Notifications, "تنبيهات", tint = Color.White)
                    if (notificationState.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(AlertErrorRed)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Simulated/Persisted FCM Instant Notification Banner (if any)
        if (notificationState.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryBlueVal.copy(alpha = 0.3f))
                    .border(1.dp, AccentElectricBlueVal, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Campaign, "إشعار عامّ", tint = AccentElectricBlueVal, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = notificationState.first(),
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Core WAM Confirmed Account Card ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ActiveCardGradient)
                    .padding(22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الحساب المعتمد WAM",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.Verified, "حساب موثق", tint = Color.Green, modifier = Modifier.size(20.dp))
                    }

                    // Displaying balances (No account status string mentioned anywhere)
                    Column {
                        val formattedYer = String.format("%,.0f", user?.balanceYer ?: 285400.0)
                        val formattedUsd = String.format("%,.2f", user?.balanceUsd ?: 450.0)

                        Text(
                            text = "$formattedYer ريال يمني",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$formattedUsd دولار أمريكي",
                            color = AccentElectricBlueVal,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Card Footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "رقم الحساب: ${user?.phone ?: "777644670"}",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "بروتوكول WAM آمن 🔐",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Approved network info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardDarkBlueVal)
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("الشبكة المعتمدة:", color = Color.Gray, fontSize = 12.sp)
            Text("بروتوكول WAM المالي الآمن", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("تحت إشراف المالك مباشرةً", color = AccentElectricBlueVal, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Grid of 6 Service Tiles ---
        Text(
            text = "المعاملات والخدمات السريعة",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            textAlign = TextAlign.Start
        )

        val tileModifier = Modifier
            .weight(1f)
            .height(105.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardDarkBlueVal)

        Row(modifier = Modifier.fillMaxWidth()) {
            // Tile 1: Pay Bills
            Column(
                modifier = tileModifier.clickable { onActionSelected("bills") },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ReceiptLong, "تسديد فواتير", tint = AccentElectricBlueVal, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("تسديد فواتير", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Tile 2: P2P
            Column(
                modifier = tileModifier.clickable { onActionSelected("p2p") },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.SwapHoriz, "P2P", tint = AccentElectricBlueVal, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("تحويل P2P", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Tile 3: Recharge
            Column(
                modifier = tileModifier.clickable { onActionSelected("recharge") },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.FlashOn, "شحن رصيد", tint = AccentElectricBlueVal, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("شحن رصيد", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            // Tile 4: Settle
            Column(
                modifier = tileModifier.clickable { onActionSelected("cash") },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.RequestQuote, "سحب وإيداع", tint = AccentElectricBlueVal, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("سحب وإيداع", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Tile 5: My Wallet History
            Column(
                modifier = tileModifier.clickable { onActionSelected("history") },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.AccountBalanceWallet, "محفظتي", tint = AccentElectricBlueVal, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("محفظتي", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Tile 6: AI Chat helper
            Column(
                modifier = tileModifier.clickable { onActionSelected("ai") },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Psychology, "المستشار الذكي AI", tint = AccentElectricBlueVal, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("المستشار AI", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-Tab 1: Services List page
// -------------------------------------------------------------
@Composable
fun ServicesScreen(
    viewModel: WalletViewModel,
    user: User?,
    onActionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlueGradient)
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "بوابة الخدمات المتكاملة WAM",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Text(
            "تقنيات الدفع الآجل والتسويات برقم الدعم 777644670",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Service catalog item
        ServiceRowItem(
            title = "تسديد فواتير الكهرباء والمياه والإنترنت",
            desc = "خدمة التسديد لكل الشركات المحلية في اليمن (المؤسسة العامة للاتصالات، الكهرباء العامة والخاصة، فواتير مياه عدن وصنعاء)",
            icon = Icons.Default.TrendingDown,
            onClick = { onActionSelected("bills") }
        )

        Spacer(modifier = Modifier.height(14.dp))

        ServiceRowItem(
            title = "شحن فوري لباقات الاتصالات",
            desc = "شحن رصيد وباقات يمن موبايل، سبأفون، يو (إم تي إن سابقاً)، بالإضافة لشبكات الواي فاي والإنترنت المنزلي فوراً.",
            icon = Icons.Default.SignalCellularAlt,
            onClick = { onActionSelected("recharge") }
        )

        Spacer(modifier = Modifier.height(14.dp))

        ServiceRowItem(
            title = "تحويلات P2P الذكية والسريعة",
            desc = "أرسل الأموال لأي شخص يحمل الرقم المسجل في بروتوكول WAM المالي الآمن خلال ثوانٍ معدودة برسوم منخفضة.",
            icon = Icons.Default.SendToMobile,
            onClick = { onActionSelected("p2p") }
        )

        Spacer(modifier = Modifier.height(14.dp))

        ServiceRowItem(
            title = "الإيداع والسحب عبر شبكة وكلاء WAM",
            desc = "يمكنك تغذية حسابك الالكتروني نقداً أو سحب الأموال عبر فروع الوكلاء المباشرين للمصمم ماهر أحمد الوتاري.",
            icon = Icons.Default.AddHomeWork,
            onClick = { onActionSelected("cash") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Partner section conforming to user requirements
        Card(
            colors = CardDefaults.cardColors(containerColor = CardDarkBlueVal),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("المجموعة المصرفية الشريكة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "نحن نعمل جنباً إلى جنب مع كبرى البنوك والمصارف والشبكات اليمنية المعتمدة لتقديم تسويات مالية فورية وموثوقة 100%. خاضع للرقابة الشخصية التامة من قبل ماهر أحمد الوتاري.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun ServiceRowItem(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDarkBlueVal)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryBlueVal),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = AccentElectricBlueVal)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, color = Color.Gray, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}

// -------------------------------------------------------------
// Sub-Tab 2: Beautiful Investment Screen
// -------------------------------------------------------------
@Composable
fun InvestmentScreen(viewModel: WalletViewModel, user: User?) {
    val digitalEnabled by viewModel.digitalAssetsEnabled.collectAsState()

    var currencyType by remember { mutableStateOf("BTC") } // BTC, ETH, WAMCOIN
    var buyAmountStr by remember { mutableStateOf("") }
    var sellAmountStr by remember { mutableStateOf("") }

    var localMessage by remember { mutableStateOf<String?>(null) }
    var localSuccess by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlueGradient)
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "بوابة الاستثمار الذكي",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Text(
            "قم بشراء الأصول الرقمية لنمو ميزانيتك المالية الآمنة",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (!digitalEnabled) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDarkBlueVal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Block, "مغلق", tint = AlertErrorRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "أسواق العملات الرقمية موقوفة مؤقتاً",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "يرجى مراجعة إدارة المحفظة أو مالك الخدمة الأستاذ ماهر الوتاري لإتاحة التداولات الرقمية لحسابك.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Asset Selection Tab with Professional Icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDarkBlueVal)
                    .padding(6.dp)
            ) {
                listOf("BTC", "ETH", "WAM").forEach { symbol ->
                    val isSelected = currencyType == symbol
                    val iconRes = when (symbol) {
                        "BTC" -> R.drawable.img_btc_coin_1779833107063
                        "ETH" -> R.drawable.img_eth_coin_1779833132223
                        else -> R.drawable.img_wam_coin_1779833151680
                    }
                    val fullName = when (symbol) {
                        "BTC" -> "Bitcoin"
                        "ETH" -> "Ethereum"
                        else -> "WAMCoin"
                    }
                    
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) PrimaryBlueVal else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSelected) AccentElectricBlueVal else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { currencyType = symbol }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = symbol,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = symbol,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = fullName,
                                color = if (isSelected) AccentElectricBlueVal else Color.Gray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Graph representation via Compose Canvas
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDarkBlueVal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val activeIconRes = when (currencyType) {
                            "BTC" -> R.drawable.img_btc_coin_1779833107063
                            "ETH" -> R.drawable.img_eth_coin_1779833132223
                            else -> R.drawable.img_wam_coin_1779833151680
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = activeIconRes),
                                contentDescription = currencyType,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, AccentElectricBlueVal, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(AlertSuccessGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "مباشر: $currencyType / USD",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = when (currencyType) {
                                        "BTC" -> "بيتكوين الرقمية"
                                        "ETH" -> "إيثيريوم الذكي"
                                        else -> "عملة WAM المميزة"
                                    },
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Text(
                            text = when (currencyType) {
                                "BTC" -> "88,240.50 $"
                                "ETH" -> "3,890.15 $"
                                else -> "11.20 $"
                            },
                            color = AlertSuccessGreen,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stylized Wave Chart drawn on Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                    ) {
                        val points = when (currencyType) {
                            "BTC" -> listOf(0.1f, 0.3f, 0.25f, 0.5f, 0.45f, 0.7f, 0.85f)
                            "ETH" -> listOf(0.3f, 0.2f, 0.4f, 0.35f, 0.6f, 0.5f, 0.75f)
                            else -> listOf(0.05f, 0.15f, 0.4f, 0.35f, 0.7f, 0.8f, 0.95f)
                        }

                        val strokeColor = AccentElectricBlueVal
                        val path = androidx.compose.ui.graphics.Path()

                        val stepX = size.width / (points.size - 1)
                        points.forEachIndexed { index, value ->
                            val x = index * stepX
                            val y = size.height - (value * size.height)
                            if (index == 0) {
                                path.moveTo(x, y)
                            } else {
                                path.lineTo(x, y)
                            }
                        }

                        // Bottom gradient shadow under trend lines
                        val fillPath = androidx.compose.ui.graphics.Path().apply {
                            addPath(path)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(AccentElectricBlueVal.copy(alpha = 0.2f), Color.Transparent)
                            )
                        )

                        drawPath(
                            path = path,
                            color = strokeColor,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 3.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Buy Form
            Card(
                colors = CardDefaults.cardColors(containerColor = CardDarkBlueVal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("تنفيذ صفقة تداول", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = buyAmountStr,
                        onValueChange = { buyAmountStr = it },
                        label = { Text("المبلغ للشراء بالدولار ($)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentElectricBlueVal,
                            unfocusedBorderColor = Color.Gray
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    localMessage?.let { m ->
                        Text(m, color = AlertErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    localSuccess?.let { s ->
                        Text(s, color = AlertSuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                val amt = buyAmountStr.toDoubleOrNull()
                                if (user == null || amt == null || amt <= 0) {
                                    localMessage = "يرجى كتابة مبلغ صحيح أكبر من صفر."
                                } else if (user.balanceUsd < amt) {
                                    localMessage = "رصيد الدولار بالحقيبة غير كافٍ للشراء."
                                    localSuccess = null
                                } else {
                                    localMessage = null
                                    buyAmountStr = ""
                                    // Simulated balance deduction safely
                                    scope.launch {
                                        viewModel.transferP2P("777644670", amt, "USD")
                                        localSuccess = "تم إرسال طلب الشراء الفوري لعملة $currencyType. تكتمل التسوية قريباً!"
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AlertSuccessGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("شراء الأصول", color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                localMessage = null
                                buyAmountStr = ""
                                localSuccess = "طلب بيع الأصول الرقمية تحت المراجعة من الإدارة."
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AlertErrorRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("بيع الأصول", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-Tab 3: Profile and About information screen
// -------------------------------------------------------------
@Composable
fun ProfileScreen(
    viewModel: WalletViewModel,
    user: User?,
    onLogout: () -> Unit
) {
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlueGradient)
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Profile avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(ActiveCardGradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user?.fullName?.take(2) ?: "WA",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            user?.fullName ?: "غير معروف",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "رقم الحساب: ${user?.phone ?: "777644670"}",
            color = Color.Gray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Actions rows
        ProfileOptionRow(
            title = "عن التطبيق (من نحن)",
            icon = Icons.Default.Info,
            onClick = { showAboutDialog = true }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProfileOptionRow(
            title = "التحقق من الهوية (KYC)",
            icon = Icons.Default.Badge,
            onClick = {
                // Instantly simulated status check alert
            },
            status = when (user?.kycStatus) {
                "APPROVED" -> "معتمد وموثق ✓"
                "SUBMITTED" -> "تحت المراجعة ⏱"
                else -> "غير مكتمل ⚠"
            },
            statusColor = when (user?.kycStatus) {
                "APPROVED" -> AlertSuccessGreen
                "SUBMITTED" -> AccentElectricBlueVal
                else -> AlertErrorRed
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProfileOptionRow(
            title = "البديل USSD المباشر",
            icon = Icons.Default.OfflineBolt,
            onClick = {},
            status = "*777644670#",
            statusColor = AccentElectricBlueVal
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProfileOptionRow(
            title = "تسجيل خروج",
            icon = Icons.Default.ExitToApp,
            onClick = onLogout,
            statusColor = AlertErrorRed
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "تصميم وإدارة: ماهر أحمد الوتاري",
            color = Color.Gray,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "بروتوكول WAM المالي الآمن 2026",
            color = Color.Gray,
            fontSize = 11.sp
        )
    }

    // "About" dialog conforming to owner specifications
    // No mention of "شركة الصيفي" and proper spelling of "ريال يمني" and "الآمن"
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = CardDarkBlueVal,
            title = {
                Text(
                    text = "عن تطبيق الماهر موني WAM",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "اسم التطبيق: الماهر موني",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("الإصدار: 1.0", color = Color.White, fontSize = 14.sp)
                    Text("المالك والمصمم: ماهر أحمد الوتاري", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("رقم الدعم: 777644670", color = Color.White, fontSize = 14.sp)
                    Text("البريد الإلكتروني: support@wam.com", color = Color.White, fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "بروتوكول WAM المالي الآمن يقدم واجهة معاملات موثوقة وفائقة السرعة لتسديد الفواتير وتحويلات P2P يمنياً ودولياً بالريال اليمني والدولار الأمريكي دون تأخير.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "حقوق النشر: WAM 2026 ©",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("إغلاق", color = AccentElectricBlueVal)
                }
            }
        )
    }
}

@Composable
fun ProfileOptionRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    status: String? = null,
    statusColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDarkBlueVal)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = title, tint = AccentElectricBlueVal)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
        if (status != null) {
            Text(status, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        } else {
            Icon(Icons.Default.ChevronLeft, "انتقل", tint = Color.Gray)
        }
    }
}

// -------------------------------------------------------------
// Interactive Dialog Box 1: P2P money sending ledger
// -------------------------------------------------------------
@Composable
fun P2PTransferDialog(
    viewModel: WalletViewModel,
    onDismiss: () -> Unit
) {
    var receiverPhone by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("YER") } // YER or USD

    var localError by remember { mutableStateOf<String?>(null) }
    var localSuccess by remember { mutableStateOf<String?>(null) }
    var feeNotice by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val feePercent by viewModel.p2pFeePercent.collectAsState()
    val scope = rememberCoroutineScope()

    // Dynamically recalculate estimated fees
    LaunchedEffect(amountStr, feePercent) {
        val amt = amountStr.toDoubleOrNull()
        if (amt != null && amt > 0) {
            val f = amt * (feePercent / 100)
            feeNotice = "الرسوم المقدرة ($feePercent%): ${String.format("%.2f", f)}"
        } else {
            feeNotice = ""
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDarkBlueVal,
        title = {
            Text(
                "تحويل أموال فوري (P2P)",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "أرسل الأموال لأي حساب مسجل برقم هاتفه فوراً عبر بروتوكول WAM الآمن.",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = receiverPhone,
                    onValueChange = { receiverPhone = it },
                    label = { Text("رقم هاتف المستلم (مثال: 777644670)", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentElectricBlueVal,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("المبلغ المرسَل", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentElectricBlueVal,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (feeNotice.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(feeNotice, color = AccentElectricBlueVal, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Currency switcher YER vs USD
                Row {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { currency = "YER" }
                            .padding(horizontal = 8.dp)
                    ) {
                        RadioButton(
                            selected = currency == "YER",
                            onClick = { currency = "YER" },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentElectricBlueVal)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("YER (ريال يمني)", color = Color.White, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { currency = "USD" }
                            .padding(horizontal = 8.dp)
                    ) {
                        RadioButton(
                            selected = currency == "USD",
                            onClick = { currency = "USD" },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentElectricBlueVal)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("USD (دولار)", color = Color.White, fontSize = 12.sp)
                    }
                }

                localError?.let { e ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(e, color = AlertErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                localSuccess?.let { s ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(s, color = AlertSuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull()
                    if (receiverPhone.isBlank() || amount == null || amount <= 0) {
                        localError = "يرجى تعبئة حقول الرقم والمبلغ بشكل صحيح"
                        return@Button
                    }
                    isLoading = true
                    localError = null
                    scope.launch {
                        val result = viewModel.transferP2P(receiverPhone, amount, currency)
                        isLoading = false
                        when (result) {
                            is TransferResult.Success -> {
                                localSuccess = "تم إرسال المبلغ ($amount $currency) لـ $receiverPhone بنجاح فوراً! 💸"
                                receiverPhone = ""
                                amountStr = ""
                            }
                            is TransferResult.Error -> {
                                localError = result.message
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentElectricBlueVal)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("إرسال حوالة WAM", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = Color.Gray)
            }
        }
    )
}

// -------------------------------------------------------------
// Interactive Dialog Box 2: Utility Bills payments panel
// -------------------------------------------------------------
@Composable
fun BillsDialog(
    viewModel: WalletViewModel,
    onDismiss: () -> Unit
) {
    var billType by remember { mutableStateOf("الإنترنت المنزلي ADSL") }
    var billAmountStr by remember { mutableStateOf("") }
    var billReference by remember { mutableStateOf("") }

    var localError by remember { mutableStateOf<String?>(null) }
    var localSuccess by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDarkBlueVal,
        title = {
            Text("تسديد فواتير الخدمات فوري", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("اختر نوع الخدمة لتسديدها بالريال اليمني فوراً من رصيدك.", color = Color.Gray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Selector tabs for common Yemeni bills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 12.dp)
                ) {
                    listOf("الإنترنت المنزلي ADSL", "الكهرباء", "المياه", "الهاتف الأرضي").forEach { type ->
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (billType == type) PrimaryBlueVal else Color(0xFF1E293B))
                                .clickable { billType = type }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(type, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = billReference,
                    onValueChange = { billReference = it },
                    label = { Text("رقم مرجع الفاتورة / رقم الاشتراك", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentElectricBlueVal,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = billAmountStr,
                    onValueChange = { billAmountStr = it },
                    label = { Text("المبلغ المطلوب دفعه‌ بالريال اليمني", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentElectricBlueVal,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                localError?.let { e ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(e, color = AlertErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                localSuccess?.let { s ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(s, color = AlertSuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = billAmountStr.toDoubleOrNull()
                    if (billReference.isBlank() || amt == null || amt <= 0) {
                        localError = "يرجى رفع بيانات وإدخال مبالغ تسوية صحيحة"
                        return@Button
                    }
                    isLoading = true
                    localError = null
                    scope.launch {
                        val result = viewModel.payBill(billType, amt, "WAM SETTLEMENT")
                        isLoading = false
                        when (result) {
                            is TransferResult.Success -> {
                                localSuccess = "تم تسديد فاتورة ($billType) بمبلغ $amt ريال يمني للرقم $billReference بنجاح مالي!"
                                billAmountStr = ""
                                billReference = ""
                            }
                            is TransferResult.Error -> {
                                localError = result.message
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentElectricBlueVal)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("إجراء الدفع المباشر", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.Gray)
            }
        }
    )
}

// -------------------------------------------------------------
// Interactive Dialog Box 3: Fast Mobile network recharge credit
// -------------------------------------------------------------
@Composable
fun RechargeDialog(
    viewModel: WalletViewModel,
    onDismiss: () -> Unit
) {
    var network by remember { mutableStateOf("يمن موبايل") }
    var targetPhone by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }

    var localError by remember { mutableStateOf<String?>(null) }
    var localSuccess by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDarkBlueVal,
        title = {
            Text("شحن رصيد وتفعيل باقات فورية", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("اشحن رصيد الأجهزة النقالة في اليمن بالريال اليمني.", color = Color.Gray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Network select tags matching standard Yemeni carriers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    listOf("يمن موبايل", "يو (SabaFon)", "سبأفون").forEach { net ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (network == net) PrimaryBlueVal else Color(0xFF1E293B))
                                .clickable { network = net }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(net, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = targetPhone,
                    onValueChange = { targetPhone = it },
                    label = { Text("رقم الهاتف المراد شحنه‌", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentElectricBlueVal,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("مبلغ الشحن بالريال اليمني", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentElectricBlueVal,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                localError?.let { e ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(e, color = AlertErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                localSuccess?.let { s ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(s, color = AlertSuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull()
                    if (targetPhone.isBlank() || amt == null || amt <= 0) {
                        localError = "يرجى ملئ بيانات الرقم وقيمة شحن الرصيد بشكل دقيق"
                        return@Button
                    }
                    isLoading = true
                    localError = null
                    scope.launch {
                        val result = viewModel.rechargeMobile(network, amt, targetPhone)
                        isLoading = false
                        when (result) {
                            is TransferResult.Success -> {
                                localSuccess = "تم شحن رصيد $network للرقم $targetPhone بمبلغ $amt ريال بنجاح تام!"
                                targetPhone = ""
                                amountStr = ""
                            }
                            is TransferResult.Error -> {
                                localError = result.message
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentElectricBlueVal)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("تأكيد شحن الرصيد", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.Gray)
            }
        }
    )
}

// -------------------------------------------------------------
// Interactive Dialog Box 4: Settle withdraw/deposit agent list
// -------------------------------------------------------------
@Composable
fun CashDepositWithdrawDialog(
    viewModel: WalletViewModel,
    onDismiss: () -> Unit
) {
    var isDeposit by remember { mutableStateOf(true) }
    var amountStr by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("YER") } // YER or USD
    var agentName by remember { mutableStateOf("الحوطة وكيل WAM الأبرز") }

    var localError by remember { mutableStateOf<String?>(null) }
    var localSuccess by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDarkBlueVal,
        title = {
            Text(if (isDeposit) "عملية إيداع حساب مالي" else "عملية سحب أموال نقداً", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("سجل عمليات السحب والإيداع السريعة عبر شبكة وكلاء وشركاء WAM المعتمدة.", color = Color.Gray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(14.dp))

                // Action Switch Type Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDeposit) PrimaryBlueVal else Color.Transparent)
                            .clickable { isDeposit = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("إيداع نقدي", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isDeposit) PrimaryBlueVal else Color.Transparent)
                            .clickable { isDeposit = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("سحب فوري", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Agent selector dropdown input text representation
                OutlinedTextField(
                    value = agentName,
                    onValueChange = { agentName = it },
                    label = { Text("اسم الوكيل أو الفرع المطلوب تكميل المعاملة عبره", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentElectricBlueVal,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("قيمة المعاملة بالكامل", color = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentElectricBlueVal,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Currency options
                Row {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { currency = "YER" }
                            .padding(horizontal = 4.dp)
                    ) {
                        RadioButton(
                            selected = currency == "YER",
                            onClick = { currency = "YER" },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentElectricBlueVal)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("YER (يمني)", color = Color.White, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { currency = "USD" }
                            .padding(horizontal = 4.dp)
                    ) {
                        RadioButton(
                            selected = currency == "USD",
                            onClick = { currency = "USD" },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentElectricBlueVal)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("USD (دولار)", color = Color.White, fontSize = 12.sp)
                    }
                }

                localError?.let { e ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(e, color = AlertErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                localSuccess?.let { s ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(s, color = AlertSuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull()
                    if (agentName.isBlank() || amt == null || amt <= 0) {
                        localError = "يرجى التحقق من صحة وقيمة الدفعات"
                        return@Button
                    }
                    isLoading = true
                    localError = null
                    scope.launch {
                        val result = viewModel.executeDepositOrWithdraw(isDeposit, currency, amt, agentName)
                        isLoading = false
                        when (result) {
                            is TransferResult.Success -> {
                                localSuccess = "بنجاح! تم تسجيل طلب الـ ${if (isDeposit) "إيداع" else "سحب"} بمبلغ $amt $currency عبر الفرع: $agentName"
                                amountStr = ""
                            }
                            is TransferResult.Error -> {
                                localError = result.message
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentElectricBlueVal)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("تسجيل وتأكيد", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = Color.Gray)
            }
        }
    )
}

// -------------------------------------------------------------
// Interactive Dialog Box 5: Detailed list of transaction ledger
// -------------------------------------------------------------
@Composable
fun WalletHistoryDialog(
    viewModel: WalletViewModel,
    onDismiss: () -> Unit
) {
    val txs by viewModel.userTransactions.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDarkBlueVal,
        title = {
            Text("سجل المعاملات والتسويات المالي", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Box(modifier = Modifier.height(320.dp).fillMaxWidth()) {
                if (txs.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.HourglassEmpty, "سجل فارغ", tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("لا يوجد قيود أو حركات مالية مسجلة لحسابك حالياً.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(txs) { item ->
                            HistoryRowItem(item)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق والرجوع", color = AccentElectricBlueVal)
            }
        }
    )
}

@Composable
fun HistoryRowItem(tx: TransactionLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF131326))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when (tx.type) {
                            "DEPOSIT" -> Color(0xFF0D3220)
                            "WITHDRAW" -> Color(0xFF4A1521)
                            else -> PrimaryBlueVal
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (tx.type) {
                        "DEPOSIT" -> Icons.Default.VerticalAlignBottom
                        "WITHDRAW" -> Icons.Default.VerticalAlignTop
                        "BILL" -> Icons.Default.Receipt
                        else -> Icons.Default.SwapHoriz
                    },
                    contentDescription = tx.type,
                    tint = if (tx.type == "DEPOSIT") AlertSuccessGreen else if (tx.type == "WITHDRAW") AlertErrorRed else AccentElectricBlueVal,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = when (tx.type) {
                        "DEPOSIT" -> "إيداع نقدي"
                        "WITHDRAW" -> "سحب نقدي"
                        "BILL" -> "تسديد فاتورة"
                        "RECHARGE" -> "شحن رصيد"
                        else -> "تحويل صادر P2P"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(tx.reference, color = Color.Gray, fontSize = 10.sp)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            val isPlus = tx.type == "DEPOSIT"
            Text(
                text = "${if (isPlus) "+" else "-"}${tx.amount} ${tx.currency}",
                color = if (isPlus) AlertSuccessGreen else Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
            )
            Text(
                text = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date(tx.timestamp)),
                color = Color.Gray,
                fontSize = 9.sp
            )
        }
    }
}

// -------------------------------------------------------------
// Interactive Dialog Box 6: AI smart advisor conversations log
// -------------------------------------------------------------
@Composable
fun AISmartAdvisorDialog(
    viewModel: WalletViewModel,
    onDismiss: () -> Unit
) {
    val messages by viewModel.aiChatMessages.collectAsState()
    val isSending by viewModel.isAiLoading.collectAsState()

    var userQuestion by remember { mutableStateOf("") }
    val listState = remember { androidx.compose.foundation.lazy.LazyListState() }
    val scope = rememberCoroutineScope()

    // Scroll chat to the base on messages array updates
    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDarkBlueVal,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AdminPanelSettings, "مساعد WAM الذكي", tint = AccentElectricBlueVal, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("مساعد WAM المالي الذكي AI", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "مستشارك الشخصي لتوفير النفقات وإدارة الحسابات برعاية ماهر أحمد الوتاري.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Box(
                    modifier = Modifier
                        .height(260.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D0D19))
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(messages) { msg ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (msg.isUser) 12.dp else 0.dp,
                                                bottomEnd = if (msg.isUser) 0.dp else 12.dp
                                            )
                                        )
                                        .background(if (msg.isUser) PrimaryBlueVal else Color(0xFF24243E))
                                        .padding(10.dp)
                                        .widthIn(max = 210.dp)
                                ) {
                                    Text(
                                        text = msg.content,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        if (isSending) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(6.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    CircularProgressIndicator(
                                        color = AccentElectricBlueVal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userQuestion,
                        onValueChange = { userQuestion = it },
                        placeholder = { Text("اطرح سؤالك المالي هنا...", color = Color.Gray, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentElectricBlueVal,
                            unfocusedBorderColor = Color.Gray
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (userQuestion.isNotBlank() && !isSending) {
                                viewModel.askAiAdvisor(userQuestion)
                                userQuestion = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(AccentElectricBlueVal)
                    ) {
                        Icon(Icons.Default.Send, "أرسل", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = Color.Gray)
            }
        }
    )
}

// -------------------------------------------------------------
// 7. Hidden Owner Admin Control Board View screen
// -------------------------------------------------------------
@Composable
fun AdminPanelScreen(
    viewModel: WalletViewModel,
    onBack: () -> Unit
) {
    val usersList by viewModel.allUsers.collectAsState()
    val transactionsFeed by viewModel.allTransactions.collectAsState()
    val fullLogs by viewModel.auditLogs.collectAsState()

    // Editable configurations variables
    val titleValue by viewModel.appTitle.collectAsState()
    val welcomeValue by viewModel.welcomePhrase.collectAsState()
    val feeValue by viewModel.p2pFeePercent.collectAsState()
    val frozenValue by viewModel.systemFrozen.collectAsState()
    val activeDigitalValue by viewModel.digitalAssetsEnabled.collectAsState()

    var activeAdminTab by remember { mutableStateOf(0) } // 0: Users, 1: Config, 2: System Audit, 3: Notifications dispatcher

    var editAppName by remember { mutableStateOf(titleValue) }
    var editWelcomePhr by remember { mutableStateOf(welcomeValue) }
    var editFeePct by remember { mutableStateOf(feeValue.toString()) }
    var editFrozenSystem by remember { mutableStateOf(frozenValue) }
    var editDigitalAssets by remember { mutableStateOf(activeDigitalValue) }

    var userSelectionForBalanceAdjust by remember { mutableStateOf<User?>(null) }
    var customYerBalanceStr by remember { mutableStateOf("") }
    var customUsdBalanceStr by remember { mutableStateOf("") }

    var editPushNotificationMsg by remember { mutableStateOf("") }
    var notificationFeedback by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackgroundVal)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "رجوع", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("لوحة التحكم الوتاري 👑", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                // Fast USSD reset trigger indicator
                IconButton(onClick = {
                    viewModel.executeUSSDBypass()
                }) {
                    Icon(Icons.Default.LockOpen, "إلغاء أذونات القفل", tint = AccentElectricBlueVal)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Admin Sub tabs row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardDarkBlueVal)
                    .horizontalScroll(rememberScrollState())
                    .padding(4.dp)
            ) {
                val tabs = listOf("المستخدمين", "إعدادات التطبيق", "حالة النظام", "بث الإشعارات")
                tabs.forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeAdminTab == index) PrimaryBlueVal else Color.Transparent)
                            .clickable { activeAdminTab = index }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(title, color = if (activeAdminTab == index) Color.White else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sub Tab implementations
            when (activeAdminTab) {
                0 -> { // Users lists views and actions
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(usersList) { u ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CardDarkBlueVal)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(u.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (u.isBlocked) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(AlertErrorRed)
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("محظور", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text("رقم: ${u.phone} (${u.email})", color = Color.Gray, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("رصيد يمني: ${String.format("%,.0f", u.balanceYer)} ريال", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("رصيد دولار: ${String.format("%,.2f", u.balanceUsd)} $", color = AccentElectricBlueVal, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text("الهوية مالي: ${u.kycStatus}", color = Color.LightGray, fontSize = 10.sp)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    // Button toggle block
                                    Button(
                                        onClick = {
                                            viewModel.adminToggleBlockUser(u.phone, !u.isBlocked)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (u.isBlocked) Color.Gray else AlertErrorRed),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(if (u.isBlocked) "إلغاء الحظر" else "حظر الحساب", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Button adjust balance
                                    Button(
                                        onClick = {
                                            userSelectionForBalanceAdjust = u
                                            customYerBalanceStr = u.balanceYer.toString()
                                            customUsdBalanceStr = u.balanceUsd.toString()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentElectricBlueVal),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("تعديل الرصيد", color = Color.White, fontSize = 10.sp)
                                    }

                                    if (u.kycStatus == "SUBMITTED") {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                viewModel.adminApproveUserKYC(u.phone)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AlertSuccessGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("اعتماد الهوية", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }

                1 -> { // General configurations change settings
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = editAppName,
                            onValueChange = { editAppName = it },
                            label = { Text("اسم التطبيق المخصص", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AccentElectricBlueVal,
                                unfocusedBorderColor = Color.Gray
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = editWelcomePhr,
                            onValueChange = { editWelcomePhr = it },
                            label = { Text("عبارة الترحيب بالرئيسية", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AccentElectricBlueVal,
                                unfocusedBorderColor = Color.Gray
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = editFeePct,
                            onValueChange = { editFeePct = it },
                            label = { Text("نسبة رسوم التحويل P2P %", color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AccentElectricBlueVal,
                                unfocusedBorderColor = Color.Gray
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Toggles configs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("تجميد شبكة الحوالات مؤقتاً (Freeze)", color = Color.White, fontSize = 13.sp)
                            Switch(
                                checked = editFrozenSystem,
                                onCheckedChange = { editFrozenSystem = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = AccentElectricBlueVal)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("تفعيل تداولات العملات الرقمية", color = Color.White, fontSize = 13.sp)
                            Switch(
                                checked = editDigitalAssets,
                                onCheckedChange = { editDigitalAssets = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = AccentElectricBlueVal)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.adminSaveSettings(
                                    customAppName = editAppName,
                                    welcomeMsg = editWelcomePhr,
                                    p2pFee = editFeePct.toDoubleOrNull() ?: 1.5,
                                    isFrozen = editFrozenSystem,
                                    digitalEnabled = editDigitalAssets
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentElectricBlueVal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("حفظ وبث الإعدادات للعملاء", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                2 -> { // System audit trails and security logs
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "سجل تتبع ومراقبة الأداء الآمن (Audit Log)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardDarkBlueVal)
                                .padding(8.dp)
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(fullLogs) { log ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(Color(0xFF131326))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(log.eventName, color = AccentElectricBlueVal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(log.details, color = Color.White, fontSize = 11.sp, lineHeight = 15.sp)
                                        }
                                        Text(
                                            text = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp)),
                                            color = Color.Gray,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> { // Pushes simulator message dispatcher
                    Column(modifier = Modifier.weight(1f)) {
                        Text("بث إشعار دفع (Push Notification) فوري لجميع المستخدمين", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = editPushNotificationMsg,
                            onValueChange = { editPushNotificationMsg = it },
                            placeholder = { Text("اكتب محتوى نص الإشعار للبث هنا...", color = Color.Gray, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AccentElectricBlueVal,
                                unfocusedBorderColor = Color.Gray
                            ),
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )

                        notificationFeedback?.let { msg ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(msg, color = AlertSuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (editPushNotificationMsg.isNotBlank()) {
                                    viewModel.adminDispatchNotification(editPushNotificationMsg)
                                    notificationFeedback = "تم إرسال الإشعار بنجاح لجميع العملاء الحاضرين!"
                                    editPushNotificationMsg = ""
                                } else {
                                    notificationFeedback = "النص فارغ عذراً!"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentElectricBlueVal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("إرسال الإشعار فوراً", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal dialog to adjust balance of selected user
    userSelectionForBalanceAdjust?.let { customer ->
        AlertDialog(
            onDismissRequest = { userSelectionForBalanceAdjust = null },
            containerColor = CardDarkBlueVal,
            title = {
                Text(
                    text = "تعديل رصيد: ${customer.fullName}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = customYerBalanceStr,
                        onValueChange = { customYerBalanceStr = it },
                        label = { Text("الرصيد بالريال اليمني", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customUsdBalanceStr,
                        onValueChange = { customUsdBalanceStr = it },
                        label = { Text("الرصيد بالدولار ($)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valYer = customYerBalanceStr.toDoubleOrNull()
                        val valUsd = customUsdBalanceStr.toDoubleOrNull()
                        if (valYer != null && valUsd != null) {
                            viewModel.adminAdjustUserBalance(customer.phone, valYer, valUsd)
                            userSelectionForBalanceAdjust = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentElectricBlueVal)
                ) {
                    Text("حفظ وتأكيد", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { userSelectionForBalanceAdjust = null }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }
}
