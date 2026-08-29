package com.example.marketing.data

enum class Role { ADMIN, STAFF }

enum class Status(val label: String) {
    NEW("New"),
    CONTACTED("Contacted"),
    SUCCESSFUL("Successful")
}

data class UserAccount(
    val id: String,
    val name: String,
    val username: String,
    val password: String,
    val role: Role
)

data class Note(
    val text: String,
    val timestamp: Long,
    val author: String
)

data class HistoryEvent(
    val text: String,
    val timestamp: Long
)

data class Customer(
    val id: String,
    var name: String,
    var phone: String,
    var status: Status,
    val dateAdded: Long,
    val addedBy: String,
    var assignedTo: String,
    var notes: List<Note> = emptyList(),
    var history: List<HistoryEvent> = emptyList()
)
