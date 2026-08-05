package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkDao {
    // Outage logs
    @Query("SELECT * FROM outage_logs ORDER BY timestamp DESC")
    fun getAllOutages(): Flow<List<OutageLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutage(outageLog: OutageLog): Long

    @Query("UPDATE outage_logs SET reportedToTechnician = 1 WHERE id = :id")
    suspend fun markReported(id: Long)

    // Speed tests
    @Query("SELECT * FROM speed_tests ORDER BY timestamp DESC")
    fun getAllSpeedTests(): Flow<List<SpeedTestRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeedTest(speedTestRecord: SpeedTestRecord)

    // Technician Tickets
    @Query("SELECT * FROM technician_tickets ORDER BY timestamp DESC")
    fun getAllTickets(): Flow<List<TechnicianTicket>>

    @Query("SELECT * FROM technician_tickets ORDER BY timestamp DESC")
    suspend fun getAllTicketsSync(): List<TechnicianTicket>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TechnicianTicket)

    @androidx.room.Update
    suspend fun updateTicket(ticket: TechnicianTicket)

    @Query("UPDATE technician_tickets SET status = :status WHERE id = :id")
    suspend fun updateTicketStatus(id: Long, status: String)
}
