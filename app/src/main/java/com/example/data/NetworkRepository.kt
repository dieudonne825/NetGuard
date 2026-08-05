package com.example.data

import kotlinx.coroutines.flow.Flow

class NetworkRepository(private val networkDao: NetworkDao) {
    val allOutages: Flow<List<OutageLog>> = networkDao.getAllOutages()
    val allSpeedTests: Flow<List<SpeedTestRecord>> = networkDao.getAllSpeedTests()
    val allTickets: Flow<List<TechnicianTicket>> = networkDao.getAllTickets()

    suspend fun logOutage(outageLog: OutageLog): Long {
        return networkDao.insertOutage(outageLog)
    }

    suspend fun markOutageReported(id: Long) {
        networkDao.markReported(id)
    }

    suspend fun addSpeedTest(record: SpeedTestRecord) {
        networkDao.insertSpeedTest(record)
    }

    suspend fun createTechnicianTicket(ticket: TechnicianTicket) {
        networkDao.insertTicket(ticket)
    }

    suspend fun updateTicketStatus(id: Long, status: String) {
        networkDao.updateTicketStatus(id, status)
    }
}
