package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CustomerDao
import com.example.data.dao.PriceDao
import com.example.data.dao.SaleDao
import com.example.data.entity.CustomerEntity
import com.example.data.entity.PriceConfigEntity
import com.example.data.entity.PriceLogEntity
import com.example.data.entity.SaleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CustomerEntity::class,
        SaleEntity::class,
        PriceConfigEntity::class,
        PriceLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun saleDao(): SaleDao
    abstract fun priceDao(): PriceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dairysync_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    // Seed initial data
                    val priceDao = database.priceDao()
                    val customerDao = database.customerDao()

                    // Default Milk Prices
                    priceDao.insertPriceConfig(PriceConfigEntity("Cow Milk", 50.0))
                    priceDao.insertPriceConfig(PriceConfigEntity("Buffalo Milk", 70.0))
                    priceDao.insertPriceConfig(PriceConfigEntity("A2 Milk", 90.0))

                    // Initial Price change logs
                    priceDao.insertPriceLog(PriceLogEntity(milkType = "Cow Milk", oldPrice = 0.0, newPrice = 50.0, timestamp = System.currentTimeMillis() - 86400000 * 5))
                    priceDao.insertPriceLog(PriceLogEntity(milkType = "Buffalo Milk", oldPrice = 0.0, newPrice = 70.0, timestamp = System.currentTimeMillis() - 86400000 * 5))
                    priceDao.insertPriceLog(PriceLogEntity(milkType = "A2 Milk", oldPrice = 0.0, newPrice = 90.0, timestamp = System.currentTimeMillis() - 86400000 * 5))

                    // Default Customers
                    customerDao.insertCustomer(CustomerEntity(name = "Buyer 1", phone = "9876543210", qrPreference = "UPI"))
                    customerDao.insertCustomer(CustomerEntity(name = "Buyer 2", phone = "8765432109", qrPreference = "UPI"))
                    customerDao.insertCustomer(CustomerEntity(name = "Arun", phone = "7654321098", qrPreference = "UPI"))
                    customerDao.insertCustomer(CustomerEntity(name = "John", phone = "6543210987", qrPreference = "CASH"))
                    customerDao.insertCustomer(CustomerEntity(name = "Milk Shop", phone = "5432109876", qrPreference = "UPI"))
                }
            }
        }
    }
}
