package com.example.data

import kotlinx.coroutines.flow.Flow

class WalletRepository(private val dao: WalletDao) {

    // Config Flows
    val systemConfigFlow: Flow<SystemConfig?> = dao.getSystemConfigFlow()
    suspend fun getSystemConfig(): SystemConfig? = dao.getSystemConfig()
    suspend fun saveSystemConfig(config: SystemConfig) = dao.insertOrUpdateConfig(config)

    // Services
    val allServicesFlow: Flow<List<AppService>> = dao.getAllServicesFlow()
    suspend fun addService(service: AppService) = dao.insertService(service)
    suspend fun updateService(service: AppService) = dao.updateService(service)
    suspend fun deleteService(service: AppService) = dao.deleteService(service)
    suspend fun clearAllServices() = dao.deleteAllServices()

    // Client records
    val allClientsFlow: Flow<List<ClientRecord>> = dao.getAllClientsFlow()
    suspend fun addClient(client: ClientRecord): Long = dao.insertClient(client)
    suspend fun updateClient(client: ClientRecord) = dao.updateClient(client)
    suspend fun deleteClient(client: ClientRecord) = dao.deleteClient(client)
    suspend fun clearAllClients() = dao.deleteAllClients()

    // Transaction logs
    val allTransactionsFlow: Flow<List<TransactionLog>> = dao.getAllTransactionsFlow()
    suspend fun addTransaction(transaction: TransactionLog) = dao.insertTransaction(transaction)
    suspend fun clearAllTransactions() = dao.deleteAllTransactions()

    // Clear everything
    suspend fun clearAllDatabase() {
        dao.deleteAllClients()
        dao.deleteAllServices()
        dao.deleteAllTransactions()
        // Reset config to defaults
        dao.insertOrUpdateConfig(SystemConfig())
    }
}
