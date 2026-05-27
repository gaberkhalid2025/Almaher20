package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {

    // System Config
    @Query("SELECT * FROM system_config WHERE id = 1")
    fun getSystemConfigFlow(): Flow<SystemConfig?>

    @Query("SELECT * FROM system_config WHERE id = 1")
    suspend fun getSystemConfig(): SystemConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: SystemConfig)

    // Services
    @Query("SELECT * FROM app_service ORDER BY id DESC")
    fun getAllServicesFlow(): Flow<List<AppService>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: AppService)

    @Update
    suspend fun updateService(service: AppService)

    @Delete
    suspend fun deleteService(service: AppService)

    @Query("DELETE FROM app_service")
    suspend fun deleteAllServices()

    // Client records
    @Query("SELECT * FROM client_record ORDER BY id DESC")
    fun getAllClientsFlow(): Flow<List<ClientRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientRecord): Long

    @Update
    suspend fun updateClient(client: ClientRecord)

    @Delete
    suspend fun deleteClient(client: ClientRecord)

    @Query("DELETE FROM client_record")
    suspend fun deleteAllClients()

    // Transaction logs
    @Query("SELECT * FROM transaction_log ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionLog)

    @Query("DELETE FROM transaction_log")
    suspend fun deleteAllTransactions()
}
