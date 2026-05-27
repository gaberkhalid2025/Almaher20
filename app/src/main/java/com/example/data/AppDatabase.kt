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
    entities = [SystemConfig::class, AppService::class, ClientRecord::class, TransactionLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "al_maher_wallet_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Insert initial default config on thread pool
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getDatabase(context)
                            if (database.walletDao().getSystemConfig() == null) {
                                database.walletDao().insertOrUpdateConfig(SystemConfig())
                                
                                // Insert some initial sample accounting services
                                database.walletDao().insertService(
                                    AppService(name = "خدمة العملاء والتحصيل", price = 15.0, type = "خدمة عملاء")
                                )
                                database.walletDao().insertService(
                                    AppService(name = "التوصيل للمحافظات", price = 25.0, type = "خدمة توصيل")
                                )
                                database.walletDao().insertService(
                                    AppService(name = "الدعم الفني والصيانة", price = 30.0, type = "خدمة صيانة")
                                )
                                database.walletDao().insertService(
                                    AppService(name = "استشارات مالية وضريبية", price = 50.0, type = "خدمة استشارات")
                                )

                                // Insert sample records for ledger table demonstration
                                val c1 = database.walletDao().insertClient(
                                    ClientRecord(name = "عبدالله اليافعي", phone = "777123456", debtAmount = 5000.0, paidAmount = 2000.0, creditAmount = 0.0)
                                )
                                val c2 = database.walletDao().insertClient(
                                    ClientRecord(name = "محمد الصنعاني", phone = "771987654", debtAmount = 0.0, paidAmount = 15000.0, creditAmount = 3000.0)
                                )
                                val c3 = database.walletDao().insertClient(
                                    ClientRecord(name = "أحمد سيف", phone = "733554433", debtAmount = 8000.0, paidAmount = 8000.0, creditAmount = 1500.0)
                                )

                                database.walletDao().insertTransaction(TransactionLog(clientId = c1, clientName = "عبدالله اليافعي", amount = 5000.0, type = "دين", notes = "بضاعة أجلة"))
                                database.walletDao().insertTransaction(TransactionLog(clientId = c1, clientName = "عبدالله اليافعي", amount = 2000.0, type = "مدفوع", notes = "دفعة نقدية أولى"))
                                database.walletDao().insertTransaction(TransactionLog(clientId = c2, clientName = "محمد الصنعاني", amount = 15000.0, type = "مدفوع", notes = "رصيد معجل"))
                                database.walletDao().insertTransaction(TransactionLog(clientId = c2, clientName = "محمد الصنعاني", amount = 3000.0, type = "رصيد دائن", notes = "أمانة"))
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
