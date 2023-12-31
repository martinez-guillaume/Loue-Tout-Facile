package com.example.louetoutfacile.network

import androidx.room.*
import java.util.Date
import androidx.room.TypeConverter
import java.util.*


@Database(entities = [User::class, Equipment::class, Category::class, Status::class, Retour::class, Reservation::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun categoryDao(): CategoryDao
    abstract fun retourDao(): RetourDao
    abstract fun reservationDao(): ReservationDao
    abstract fun statusDao(): StatusDao

}


// SQLite, utilisé par Room, ne prend pas en charge directement le type Date.
class Converters {
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date? {
        return millisSinceEpoch?.let { Date(it) }
    }
}


// Entité User
@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "firstname") val firstname: String,
    @ColumnInfo(name = "login") val login: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "isAdmin") val isAdmin: Boolean = false
)

// Entité Equipment
@Entity(tableName = "equipment")
data class Equipment(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "category") val category: Int,
    @ColumnInfo(name = "status") var status: Int,
    @ColumnInfo(name = "price") val price: Double,
    @ColumnInfo(name = "image_url") val imageUrl: String
)

// Entité Category
@Entity(tableName = "category")
data class Category(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String
)

@Entity(tableName = "status")
data class Status(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String
)

// Entité Retour
@Entity(tableName = "retour")
data class Retour(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "id_equipment") val id_equipment: Long,
    @ColumnInfo(name = "id_user") val id_user: Long,
    @ColumnInfo(name = "return_date") val return_date: Date
)

// Entité Reservation
@Entity(tableName = "reservation")
data class Reservation(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "id_user") val id_user: Long,
    @ColumnInfo(name = "id_equipment") val id_equipment: Long,
    @ColumnInfo(name = "start_date") val start_date: Date,
    @ColumnInfo(name = "end_date") val end_date: Date
)




// DAO pour User
@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE login = :login")
    fun findByLogin(login: String): User?

    @Query("SELECT * FROM user WHERE login = :login AND isAdmin = 1")
    fun findAdminByLogin(login: String): User?

    @Query("SELECT * FROM user WHERE id = :userId")
    fun findById(userId: Long): User

    @Insert
    fun insert(user: User)

    @Update
    fun update(user: User)

    @Delete
    fun delete(user: User)

}

// DAO pour Equipment
@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment")
    fun getAll(): List<Equipment>

    @Query("SELECT * FROM equipment WHERE id = :equipmentId")
    fun findById(equipmentId: Long): Equipment

    @Query("SELECT * FROM equipment WHERE title LIKE :title")
    suspend fun searchByTitle(title: String): List<Equipment>

    @Insert
    fun insert(equipment: Equipment)

    @Update
    fun update(equipment: Equipment)

    @Delete
    fun delete(equipment: Equipment)
}

// DAO pour Category
@Dao
interface CategoryDao {
    @Query("SELECT * FROM category")
    fun getAll(): List<Category>

    @Query("SELECT * FROM category WHERE id = :categoryId")
    fun findById(categoryId: Int): Category

    @Insert
    fun insert(category: Category)

    @Update
    fun update(category: Category)

    @Delete
    fun delete(category: Category)
}

@Dao
interface StatusDao {
    @Query("SELECT * FROM status")
    fun getAll(): List<Status>

    @Query("SELECT * FROM status WHERE id = :statusId")
    fun findById(statusId: Int): Status

    @Query("SELECT name FROM status WHERE id = :statusId")
    fun getStatusNameById(statusId: Int): String

    @Insert
    fun insert(status: Status)

    @Update
    fun update(status: Status)

    @Delete
    fun delete(status: Status)
}

// DAO pour Retour
@Dao
interface RetourDao {
    @Query("SELECT * FROM retour")
    fun getAll(): List<Retour>

    @Query("SELECT * FROM retour WHERE id = :retourId")
    fun findById(retourId: Long): Retour

    @Insert
    fun insert(retour: Retour)

    @Update
    fun update(retour: Retour)

    @Delete
    fun delete(retour: Retour)
}

// DAO pour Reservation
@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservation")
    fun getAll(): List<Reservation>

    @Query("SELECT * FROM reservation WHERE id = :reservationId")
    fun findById(reservationId: Long): Reservation

    @Query("SELECT * FROM reservation WHERE id_equipment = :equipmentId")
    fun getReservationsForEquipment(equipmentId: Long): List<Reservation>

    @Query("SELECT COUNT(*) FROM reservation WHERE id_equipment = :equipmentId")
    suspend fun countReservationsForEquipment(equipmentId: Long): Int

    @Query("DELETE FROM reservation WHERE id_equipment = :equipmentId")
    fun deleteReservationsByEquipmentId(equipmentId: Long)

    @Insert
    fun insert(reservation: Reservation)

    @Update
    fun update(reservation: Reservation)

    @Delete
    fun delete(reservation: Reservation)
}


