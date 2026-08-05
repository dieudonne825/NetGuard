package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in messages) {
                val sender = sms.displayOriginatingAddress ?: "Inconnu"
                val body = sms.messageBody ?: continue

                Log.d("SmsReceiver", "SMS reçu de $sender : $body")

                // Check if this SMS is related to an incident ticket
                if (isIncidentSms(body)) {
                    processIncidentSms(context, sender, body)
                }
            }
        }
    }

    private fun isIncidentSms(body: String): Boolean {
        val uppercaseBody = body.uppercase(Locale.ROOT)
        return uppercaseBody.contains("NETGUARD") ||
                uppercaseBody.contains("RPT-") ||
                uppercaseBody.contains("TICK-") ||
                uppercaseBody.contains("INCIDENT") ||
                uppercaseBody.contains("PANNE") ||
                uppercaseBody.contains("STATUT") ||
                uppercaseBody.contains("TECHNICIEN") ||
                uppercaseBody.contains("INTERVENTION")
    }

    private fun processIncidentSms(context: Context, sender: String, body: String) {
        val uppercaseBody = body.uppercase(Locale.ROOT)

        // Parse ticket reference (e.g. RPT-FR-12345 or TICK-8921 or #8942)
        val ticketRegex = Regex("""(RPT-[A-Z0-9-]+|TICK-[A-Z0-9-]+|#[0-9]{4,6})""")
        val match = ticketRegex.find(uppercaseBody)
        val ticketRef = match?.value ?: "Ticket Réseau"

        // Deduce new ticket status from text
        val newStatus = when {
            uppercaseBody.contains("RÉSOLU") || uppercaseBody.contains("RESOLU") || uppercaseBody.contains("RÉSOLUE") || uppercaseBody.contains("RESOLUE") || uppercaseBody.contains("RÉTABLI") || uppercaseBody.contains("RETABLI") -> "RESOLU"
            uppercaseBody.contains("EN COURS") || uppercaseBody.contains("EN_COURS") || uppercaseBody.contains("SUR PLACE") || uppercaseBody.contains("INTERVENTION") -> "INTERVENTION_EN_COURS"
            uppercaseBody.contains("ASSIGNE") || uppercaseBody.contains("ASSIGNÉ") || uppercaseBody.contains("PRIS EN CHARGE") || uppercaseBody.contains("PRIS_EN_CHARGE") || uppercaseBody.contains("CONFIRMÉ") -> "TECHNICIEN_ASSIGNE"
            uppercaseBody.contains("CLÔTURÉ") || uppercaseBody.contains("CLOTURE") || uppercaseBody.contains("FERMÉ") -> "CLOTURE"
            else -> "EN_ATTENTE"
        }

        val readableStatus = when (newStatus) {
            "RESOLU" -> "Résolu ✅"
            "INTERVENTION_EN_COURS" -> "Intervention en Cours 🛠️"
            "TECHNICIEN_ASSIGNE" -> "Pris en Charge / Technicien Assigné 👨‍🔧"
            "CLOTURE" -> "Clôturé 🔒"
            else -> "Signalé 📥"
        }

        // Notify user via System Notification
        OutageNotificationManager.showSmsTicketUpdateNotification(
            context = context,
            sender = sender,
            message = body,
            ticketRef = ticketRef,
            readableStatus = readableStatus
        )

        // Asynchronously update ticket status in database if ticket exists or notify listeners
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val tickets = db.networkDao().getAllTicketsSync()
                val targetTicket = tickets.find { 
                    it.ticketNumber.equals(ticketRef, ignoreCase = true) || ticketRef.contains(it.ticketNumber, ignoreCase = true)
                }
                
                if (targetTicket != null) {
                    val updatedTicket = targetTicket.copy(status = newStatus)
                    db.networkDao().updateTicket(updatedTicket)
                    Log.d("SmsReceiver", "Ticket ${targetTicket.ticketNumber} mis à jour avec le statut: $newStatus")
                } else {
                    // Create new ticket if unknown reference
                    val newTicket = com.example.data.TechnicianTicket(
                        ticketNumber = if (ticketRef.startsWith("Ticket")) "RPT-SMS-${(1000..9999).random()}" else ticketRef,
                        faultType = "Mise à jour SMS Opérateur",
                        status = newStatus,
                        description = "Reçu via SMS de $sender : $body",
                        modemModel = "Support Télécom SMS"
                    )
                    db.networkDao().insertTicket(newTicket)
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Erreur mise à jour DB SMS", e)
            }
        }

        // Notify ViewModel listener if registered
        onSmsReceivedListener?.invoke(ticketRef, newStatus, body)
    }

    companion object {
        var onSmsReceivedListener: ((ticketRef: String, newStatus: String, message: String) -> Unit)? = null
    }
}
