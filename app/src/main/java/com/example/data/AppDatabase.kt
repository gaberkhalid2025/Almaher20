package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, TransactionLog::class, AuditLog::class, AppSetting::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wam_wallet_db"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDb(database.walletDao())
                }
            }
        }

        suspend fun populateDb(walletDao: WalletDao) {
            // Seed default settings
            walletDao.insertAppSetting(AppSetting("app_name", "الماهر موني"))
            walletDao.insertAppSetting(AppSetting("primary_color", "#0A2463"))
            walletDao.insertAppSetting(AppSetting("secondary_color", "#3A7CA5"))
            walletDao.insertAppSetting(AppSetting("accent_color", "#00B4D8"))
            walletDao.insertAppSetting(AppSetting("p2p_fee_percent", "1.5"))
            walletDao.insertAppSetting(AppSetting("system_frozen", "false"))
            walletDao.insertAppSetting(AppSetting("enable_digital_assets", "true"))
            walletDao.insertAppSetting(AppSetting("welcome_phrase", "مرحباً بك في جيل المال الذكي"))

            // Seed an audit log
            walletDao.insertAuditLog(
                AuditLog(
                    eventName = "SYSTEM_INIT",
                    details = "تم تشغيل نظام بروتوكول WAM المالي الآمن لأول مرة بنجاح تحت إشراف ماهر أحمد الوتاري"
                )
            )

            // Seed Owner Maher Alwatari's user account so they can log in or see their profile instantly.
            // Password hash for: WAM2026 -> simple SHA256 of "maher--736462"
            // Let's create a user with phone 777644670 (the owner's support phone requested)
            // Name: ماهر أحمد الوتاري
            // Password: same as back gateway: maher--736462 (or simple hash "e9eaac81c828df9dc11a0d3b6af411ea9ed22de162d3a3350c3bc5e985bdf077")
            // Balances: 285400 ريال يمني and 450 دولار أمريكي
            walletDao.insertUser(
                User(
                    phone = "777644670",
                    fullName = "ماهر أحمد الوتاري",
                    email = "support@wam.com",
                    passwordHash = "e9eaac81c828df9dc11a0d3b6af411ea9ed22de162d3a3350c3bc5e985bdf077", // Hash for 'maher--736462'
                    balanceYer = 285400.0,
                    balanceUsd = 450.0,
                    kycStatus = "APPROVED"
                )
            )
        }
    }
}
