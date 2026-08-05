package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outage_logs")
data class OutageLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "ISP_OUTAGE" (Panne FAI) or "POWER_OUTAGE" (Coupure Courant) or "RECOVERY"
    val title: String,
    val description: String,
    val durationSeconds: Long = 0,
    val ispName: String = "Orange / Free / SFR / Bouygues",
    val reportedToTechnician: Boolean = false
)
