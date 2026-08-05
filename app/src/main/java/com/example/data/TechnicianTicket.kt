package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "technician_tickets")
data class TechnicianTicket(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val faultType: String, // "Panne FAI Réseau Optique" or "Coupure Alimentation Modem"
    val status: String, // "EN_ATTENTE", "TECHNICIEN_ASSIGNE", "INTERVENTION_EN_COURS", "RESOLU"
    val description: String,
    val modemModel: String,
    val clientAddress: String = "Client #8942 - Paris 15e",
    val estimatedResolution: String = "Intervention prévue d'ici 2h"
)
