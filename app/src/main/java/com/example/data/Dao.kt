package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    // Users
    @Query("SELECT * FROM users WHERE phone = :phone")
    fun getUser(phone: String): Flow<User?>

    @Query("SELECT * FROM users WHERE phone = :phone")
    suspend fun getUserSync(phone: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    // Transactions
    @Query("SELECT * FROM transactions WHERE senderPhone = :phone OR receiverPhone = :phone ORDER BY timestamp DESC")
    fun getTransactionsForUser(phone: String): Flow<List<TransactionLog>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionLog)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    // App Settings
    @Query("SELECT * FROM app_settings")
    fun getAppSettingsFlow(): Flow<List<AppSetting>>

    @Query("SELECT * FROM app_settings WHERE `key` = :key")
    suspend fun getAppSetting(key: String): AppSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSetting(setting: AppSetting)
}
