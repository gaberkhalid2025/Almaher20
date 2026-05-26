package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val phone: String, // e.g., 777644670
    val fullName: String,
    val email: String,
    val passwordHash: String, // Hashed password
    val balanceYer: Double = 285400.0, // Initial Yer balance
    val balanceUsd: Double = 450.0,    // Initial Usd balance
    val kycStatus: String = "PENDING", // PENDING, SUBMITTED, APPROVED
    val isBlocked: Boolean = false,
    val selfiePath: String? = null,
    val idCardPath: String? = null
)

@Entity(tableName = "transactions")
data class TransactionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderPhone: String,
    val receiverPhone: String,
    val amount: Double,
    val currency: String, // YER, USD
    val type: String, // P2P, BILL, RECHARGE, WITHDRAW, DEPOSIT
    val timestamp: Long = System.currentTimeMillis(),
    val reference: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventName: String, // LOGIN_SUCCESS, LOGIN_FAIL, ADMIN_LOGIN_SUCCESS, ADMIN_LOGIN_FAIL, TRANSFER, KYC_SUBMIT
    val timestamp: Long = System.currentTimeMillis(),
    val details: String,
    val deviceInfo: String = "Android Device"
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey val key: String,
    val value: String
)
