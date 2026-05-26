package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class WalletRepository(private val dao: WalletDao) {

    // Utilities - Password Hashing
    fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password // Fallback to plain if hash error occurs safely
        }
    }

    // Users
    fun getUser(phone: String): Flow<User?> = dao.getUser(phone)
    
    suspend fun getUserSync(phone: String): User? = withContext(Dispatchers.IO) {
        dao.getUserSync(phone)
    }

    fun getAllUsers(): Flow<List<User>> = dao.getAllUsers()

    suspend fun insertUser(user: User) = withContext(Dispatchers.IO) {
        dao.insertUser(user)
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        dao.updateUser(user)
    }

    // Transactions
    fun getTransactionsForUser(phone: String): Flow<List<TransactionLog>> = dao.getTransactionsForUser(phone)
    
    fun getAllTransactions(): Flow<List<TransactionLog>> = dao.getAllTransactions()

    suspend fun insertTransaction(transaction: TransactionLog) = withContext(Dispatchers.IO) {
        dao.insertTransaction(transaction)
    }

    // Audit Logs
    fun getAllAuditLogs(): Flow<List<AuditLog>> = dao.getAllAuditLogs()

    suspend fun insertAuditLog(eventName: String, details: String) = withContext(Dispatchers.IO) {
        dao.insertAuditLog(AuditLog(eventName = eventName, details = details))
    }

    // App Settings
    fun getAppSettingsFlow(): Flow<List<AppSetting>> = dao.getAppSettingsFlow()

    suspend fun getAppSetting(key: String): String? = withContext(Dispatchers.IO) {
        dao.getAppSetting(key)?.value
    }

    suspend fun insertAppSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        dao.insertAppSetting(AppSetting(key, value))
    }

    // P2P Transfer Process
    suspend fun performP2PTransfer(
        senderPhone: String,
        receiverPhone: String,
        amount: Double,
        currency: String // YER, USD
    ): TransferResult = withContext(Dispatchers.IO) {
        val systemFrozen = dao.getAppSetting("system_frozen")?.value == "true"
        if (systemFrozen) {
            return@withContext TransferResult.Error("النظام معلق مؤقتاً من قبل الإدارة. لا يمكن إجراء معاملات حالياً.")
        }

        if (senderPhone == receiverPhone) {
            return@withContext TransferResult.Error("لا يمكنك التحويل لنفس رقم هاتف حسابك.")
        }

        val sender = dao.getUserSync(senderPhone) ?: return@withContext TransferResult.Error("حساب المرسِل غير موجود")
        val receiver = dao.getUserSync(receiverPhone) ?: return@withContext TransferResult.Error("حساب المستلِم غير مسجل في الخدمة")

        if (receiver.isBlocked) {
            return@withContext TransferResult.Error("حساب المستلم معلق أو محظور.")
        }

        val feePercentStr = dao.getAppSetting("p2p_fee_percent")?.value ?: "1.5"
        val feePercent = feePercentStr.toDoubleOrNull() ?: 1.5
        val fee = if (currency == "YER") (amount * (feePercent / 100)) else (amount * (feePercent / 100))
        val totalDebit = amount + fee

        if (currency == "YER") {
            if (sender.balanceYer < totalDebit) {
                return@withContext TransferResult.Error("رصيدك غير كافٍ. يتطلب: ${String.format("%.2f", totalDebit)} ريال يمني (الرسوم: ${String.format("%.2f", fee)})")
            }
            // Update sender
            dao.updateUser(sender.copy(balanceYer = sender.balanceYer - totalDebit))
            // Update receiver
            dao.updateUser(receiver.copy(balanceYer = receiver.balanceYer + amount))
        } else {
            if (sender.balanceUsd < totalDebit) {
                return@withContext TransferResult.Error("رصيدك غير كافٍ. يتطلب: ${String.format("%.2f", totalDebit)} دولار (الرسوم: ${String.format("%.2f", fee)})")
            }
            // Update sender
            dao.updateUser(sender.copy(balanceUsd = sender.balanceUsd - totalDebit))
            // Update receiver
            dao.updateUser(receiver.copy(balanceUsd = receiver.balanceUsd + amount))
        }

        // Write Transaction Logs
        dao.insertTransaction(
            TransactionLog(
                senderPhone = senderPhone,
                receiverPhone = receiverPhone,
                amount = amount,
                currency = currency,
                type = "P2P",
                reference = "تحويل P2P آمن - رسوم: $fee"
            )
        )

        // Write Audit Logs
        dao.insertAuditLog(
            AuditLog(
                eventName = "TRANSFER",
                details = "تم تحويل $amount $currency من $senderPhone إلى $receiverPhone (رسوم: $fee)"
            )
        )

        TransferResult.Success
    }

    // Bills payment, recharge, deposit, withdraw helpers
    suspend fun performBillPayment(phone: String, type: String, amount: Double, provider: String): TransferResult = withContext(Dispatchers.IO) {
        val user = dao.getUserSync(phone) ?: return@withContext TransferResult.Error("الحساب غير موجود")
        if (user.balanceYer < amount) {
            return@withContext TransferResult.Error("رصيد بالريال اليمني غير كافٍ لتسديد هذه الفاتورة")
        }
        // Update
        dao.updateUser(user.copy(balanceYer = user.balanceYer - amount))
        // Log transaction
        dao.insertTransaction(
            TransactionLog(
                senderPhone = phone,
                receiverPhone = "SYSTEM_BILL",
                amount = amount,
                currency = "YER",
                type = "BILL",
                reference = "تسديد فاتورة $type عبر $provider"
            )
        )
        // Log audit
        dao.insertAuditLog(
            AuditLog(
                eventName = "BILL_PAY",
                details = "تسديد $amount ريال يمني فاتورة $type من رقم $phone"
            )
        )
        TransferResult.Success
    }

    suspend fun performMobileRecharge(phone: String, networkName: String, amount: Double, targetNumber: String): TransferResult = withContext(Dispatchers.IO) {
        val user = dao.getUserSync(phone) ?: return@withContext TransferResult.Error("الحساب غير موجود")
        if (user.balanceYer < amount) {
            return@withContext TransferResult.Error("رصيدك بالريال اليمني غير كافٍ لشحن الرصيد")
        }
        dao.updateUser(user.copy(balanceYer = user.balanceYer - amount))
        dao.insertTransaction(
            TransactionLog(
                senderPhone = phone,
                receiverPhone = "SYSTEM_RECHARGE",
                amount = amount,
                currency = "YER",
                type = "RECHARGE",
                reference = "شحن رصيد $networkName للرقم $targetNumber"
            )
        )
        dao.insertAuditLog(
            AuditLog(
                eventName = "RECHARGE",
                details = "شحن رصيد $amount ريال يمني لـ $networkName للرقم $targetNumber"
            )
        )
        TransferResult.Success
    }

    suspend fun performDepositOrWithdraw(phone: String, isDeposit: Boolean, currency: String, amount: Double, agent: String): TransferResult = withContext(Dispatchers.IO) {
        val user = dao.getUserSync(phone) ?: return@withContext TransferResult.Error("الحساب غير موجود")
        if (isDeposit) {
            if (currency == "YER") {
                dao.updateUser(user.copy(balanceYer = user.balanceYer + amount))
            } else {
                dao.updateUser(user.copy(balanceUsd = user.balanceUsd + amount))
            }
            dao.insertTransaction(
                TransactionLog(
                    senderPhone = "AGENT_$agent",
                    receiverPhone = phone,
                    amount = amount,
                    currency = currency,
                    type = "DEPOSIT",
                    reference = "إيداع نقدي عبر وكيل WAM: $agent"
                )
            )
            dao.insertAuditLog(
                AuditLog(
                    eventName = "DEPOSIT",
                    details = "إيداع $amount $currency للحساب $phone"
                )
            )
        } else {
            // Withdraw
            if (currency == "YER") {
                if (user.balanceYer < amount) return@withContext TransferResult.Error("رصيد غير كافٍ للسحب")
                dao.updateUser(user.copy(balanceYer = user.balanceYer - amount))
            } else {
                if (user.balanceUsd < amount) return@withContext TransferResult.Error("رصيد غير كافٍ للسحب")
                dao.updateUser(user.copy(balanceUsd = user.balanceUsd - amount))
            }
            dao.insertTransaction(
                TransactionLog(
                    senderPhone = phone,
                    receiverPhone = "AGENT_$agent",
                    amount = amount,
                    currency = currency,
                    type = "WITHDRAW",
                    reference = "سحب نقدي عبر وكيل WAM: $agent"
                )
            )
            dao.insertAuditLog(
                AuditLog(
                    eventName = "WITHDRAW",
                    details = "سحب $amount $currency من الحساب $phone"
                )
            )
        }
        TransferResult.Success
    }
}

sealed class TransferResult {
    object Success : TransferResult()
    data class Error(val message: String) : TransferResult()
}
