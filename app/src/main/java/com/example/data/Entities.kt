package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system_config")
data class SystemConfig(
    @PrimaryKey val id: Int = 1,
    val appName: String = "الماهر للحسابات",
    val primaryColorHex: String = "#0D47A1", // Deep blue
    val secondaryColorHex: String = "#FFC107", // Bright gold
    val isDarkMode: Boolean = false,
    val isReportsEnabled: Boolean = true,
    val isNotificationsEnabled: Boolean = true,
    val isBackupEnabled: Boolean = true,
    val isRealTimeSyncEnabled: Boolean = true,
    val minStockLimit: Int = 10,
    val smsGatewayNumber: String = "+967777644670",
    val smsApiKey: String = "",
    val customColumnsSemicolonSeparated: String = "", // e.g. "رقم الفاتورة;تاريخ الاستحقاق"
    val supervisorPermissionsJson: String = "إضافة;تعديل;حذف", // Permissions granted to supervisors
    val appPasswordHash: String = "123456" // Default login password
)

@Entity(tableName = "app_service")
data class AppService(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Double,
    val isEnabled: Boolean = true,
    val type: String = "خدمة عامة" // Like: خدمة عملاء - توصيل - صيانة - استشارات
)

@Entity(tableName = "client_record")
data class ClientRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val debtAmount: Double = 0.0, // الديون (Red)
    val paidAmount: Double = 0.0, // المدفوع (Green)
    val creditAmount: Double = 0.0, // الرصيد الدائن (Orange)
    val lastUpdateEpoch: Long = System.currentTimeMillis(),
    val customFieldsJson: String = "" // For dynamic table features, format: "key1:val1;key2:val2"
) {
    // Calculates net balance: (Credit + Paid) - Debt
    val netBalance: Double
        get() = (creditAmount + paidAmount) - debtAmount
}

@Entity(tableName = "transaction_log")
data class TransactionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val clientName: String,
    val amount: Double,
    val type: String, // "دين" (debt), "مدفوع" (paid), "رصيد دائن" (credit)
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
