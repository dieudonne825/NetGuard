package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_tests")
data class SpeedTestRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val downloadMbps: Double,
    val uploadMbps: Double,
    val latencyMs: Int,
    val jitterMs: Int,
    val packetLossPct: Double,
    val connectionType: String = "Wi-Fi 6 (5 GHz)"
)
