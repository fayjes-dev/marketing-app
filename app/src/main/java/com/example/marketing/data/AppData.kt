package com.example.marketing.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

object AppData {

    val users = mutableStateListOf(
        UserAccount("u_admin", "Admin", "admin", "admin123", Role.ADMIN),
        UserAccount("u_aisha", "Aisha", "aisha", "pass123", Role.STAFF),
        UserAccount("u_ravi", "Ravi", "ravi", "pass123", Role.STAFF),
        UserAccount("u_fayjes", "Fayjes", "fayjes", "fayjes1234", Role.STAFF)
    )

    val customers = mutableStateListOf<Customer>()
    val isSyncing = mutableStateOf(true)

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val customersRef by lazy { db.collection("customers") }

    fun startSync() {
        customersRef.addSnapshotListener { snapshot, _ ->
            if (snapshot == null) return@addSnapshotListener
            val fresh = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val phone = doc.getString("phone") ?: return@mapNotNull null
                val statusStr = doc.getString("status") ?: Status.NEW.name
                val dateAdded = doc.getLong("dateAdded") ?: System.currentTimeMillis()
                val addedBy = doc.getString("addedBy") ?: ""
                val assignedTo = doc.getString("assignedTo") ?: ""
                val notesRaw = doc.get("notes") as? List<*> ?: emptyList<Any>()
                val notes = notesRaw.mapNotNull { item ->
                    val map = item as? Map<*, *> ?: return@mapNotNull null
                    val text = map["text"] as? String ?: return@mapNotNull null
                    val timestamp = (map["timestamp"] as? Long) ?: (map["timestamp"] as? Double)?.toLong() ?: 0L
                    val author = map["author"] as? String ?: ""
                    Note(text = text, timestamp = timestamp, author = author)
                }.sortedByDescending { it.timestamp }
                val historyRaw = doc.get("history") as? List<*> ?: emptyList<Any>()
                val history = historyRaw.mapNotNull { item ->
                    val map = item as? Map<*, *> ?: return@mapNotNull null
                    val text = map["text"] as? String ?: return@mapNotNull null
                    val timestamp = (map["timestamp"] as? Long) ?: (map["timestamp"] as? Double)?.toLong() ?: 0L
                    HistoryEvent(text = text, timestamp = timestamp)
                }.sortedByDescending { it.timestamp }
                Customer(
                    id = doc.id,
                    name = name,
                    phone = phone,
                    status = runCatching { Status.valueOf(statusStr) }.getOrDefault(Status.NEW),
                    dateAdded = dateAdded,
                    addedBy = addedBy,
                    assignedTo = assignedTo,
                    notes = notes,
                    history = history
                )
            }
            customers.clear()
            customers.addAll(fresh)
            isSyncing.value = false
        }
    }

    fun login(usernameOrEmail: String, password: String): UserAccount? {
        val id = usernameOrEmail.trim().lowercase()
        return users.find { it.username.lowercase() == id && it.password == password }
    }

    fun addCustomer(name: String, phone: String, addedBy: String, assignedTo: String) {
        val id = UUID.randomUUID().toString()
        val data = hashMapOf(
            "name" to name,
            "phone" to phone,
            "status" to Status.NEW.name,
            "dateAdded" to System.currentTimeMillis(),
            "addedBy" to addedBy,
            "assignedTo" to assignedTo,
            "notes" to emptyList<Any>(),
            "history" to listOf(
                mapOf("text" to "Added by ${userName(addedBy)}", "timestamp" to System.currentTimeMillis())
            )
        )
        customersRef.document(id).set(data)
    }

    private fun addHistoryEvent(customerId: String, text: String) {
        val event = mapOf("text" to text, "timestamp" to System.currentTimeMillis())
        customersRef.document(customerId).update("history", FieldValue.arrayUnion(event))
    }

    fun addNote(customerId: String, text: String, author: String) {
        val note = mapOf(
            "text" to text,
            "timestamp" to System.currentTimeMillis(),
            "author" to author
        )
        customersRef.document(customerId).update("notes", FieldValue.arrayUnion(note))
    }

    fun updateStatus(customerId: String, status: Status) {
        val oldLabel = customers.find { it.id == customerId }?.status?.label ?: "?"
        customersRef.document(customerId).update("status", status.name)
        addHistoryEvent(customerId, "Status changed from $oldLabel to ${status.label}")
    }

    fun reassign(customerId: String, newAssignee: String) {
        val oldId = customers.find { it.id == customerId }?.assignedTo
        val oldName = if (oldId != null) userName(oldId) else "?"
        customersRef.document(customerId).update("assignedTo", newAssignee)
        addHistoryEvent(customerId, "Reassigned from $oldName to ${userName(newAssignee)}")
    }

    fun deleteCustomer(customerId: String) {
        customersRef.document(customerId).delete()
    }

    fun userName(id: String): String = users.find { it.id == id }?.name ?: "—"

    fun staffUsers(): List<UserAccount> = users.filter { it.role == Role.STAFF }
}
