package com.example.marketing.data

import androidx.compose.runtime.mutableStateListOf
import java.util.UUID

/**
 * Simple in-memory data store shared by the whole app.
 * This keeps the prototype self-contained (no backend / database setup required).
 * Swap this out for a real backend (Firebase, REST API, Room + sync, etc.)
 * when moving beyond the prototype stage.
 */
object AppData {

    val users = mutableStateListOf(
        UserAccount("u_admin", "Admin", "admin", "admin123", Role.ADMIN),
        UserAccount("u_aisha", "Aisha", "aisha", "pass123", Role.STAFF),
        UserAccount("u_ravi", "Ravi", "ravi", "pass123", Role.STAFF),
        UserAccount("u_fayjes", "Fayjes", "fayjes", "fayjes1234", Role.STAFF)
    )

    val customers = mutableStateListOf<Customer>()

    fun login(usernameOrEmail: String, password: String): UserAccount? {
        val id = usernameOrEmail.trim().lowercase()
        return users.find { it.username.lowercase() == id && it.password == password }
    }

    fun addCustomer(name: String, phone: String, addedBy: String, assignedTo: String) {
        customers.add(
            0,
            Customer(
                id = UUID.randomUUID().toString(),
                name = name,
                phone = phone,
                status = Status.NEW,
                dateAdded = System.currentTimeMillis(),
                addedBy = addedBy,
                assignedTo = assignedTo
            )
        )
    }

    fun updateStatus(customerId: String, status: Status) {
        customers.find { it.id == customerId }?.status = status
    }

    fun reassign(customerId: String, newAssignee: String) {
        customers.find { it.id == customerId }?.assignedTo = newAssignee
    }

    fun userName(id: String): String = users.find { it.id == id }?.name ?: "—"

    fun staffUsers(): List<UserAccount> = users.filter { it.role == Role.STAFF }
}
