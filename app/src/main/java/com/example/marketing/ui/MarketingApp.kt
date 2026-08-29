package com.example.marketing.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marketing.data.AppData
import com.example.marketing.data.Customer
import com.example.marketing.data.Role
import com.example.marketing.data.Status
import com.example.marketing.data.UserAccount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Ink = Color(0xFF1C2430)
private val BgColor = Color(0xFFF7F7F4)

private fun whatsappUrl(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    val international = when {
        digits.startsWith("0") -> "255" + digits.drop(1)
        digits.startsWith("255") -> digits
        else -> "255$digits"
    }
    return "https://wa.me/$international"
}

private fun statusColor(status: Status): Color = when (status) {
    Status.NEW -> Color(0xFF64748B)
    Status.CONTACTED -> Color(0xFFB98900)
    Status.SUCCESSFUL -> Color(0xFF10B981)
}

private fun statusBg(status: Status): Color = when (status) {
    Status.NEW -> Color(0xFFF1F5F9)
    Status.CONTACTED -> Color(0xFFFEF3C7)
    Status.SUCCESSFUL -> Color(0xFFD1FAE5)
}

private fun fmtDate(millis: Long): String =
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))

@Composable
fun MarketingApp() {
    var session by remember { mutableStateOf<UserAccount?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        if (session == null) {
            LoginScreen(onLogin = { session = it })
        } else {
            HomeScreen(session = session!!, onLogout = { session = null })
        }
    }
}

@Composable
fun LoginScreen(onLogin: (UserAccount) -> Unit) {
    var idField by remember { mutableStateOf("") }
    var pwField by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun attemptLogin() {
        val match = AppData.login(idField, pwField)
        if (match == null) {
            error = "Incorrect username/email or password."
        } else {
            error = null
            onLogin(match)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Ink, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Groups, contentDescription = null, tint = Color.White)
        }
        Spacer(Modifier.height(16.dp))
        Text("Marketing", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text(
            "Sign in to manage your customers",
            fontSize = 14.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
        )

        Text("Username or email", fontSize = 12.sp, color = Color(0xFF64748B))
        OutlinedTextField(
            value = idField,
            onValueChange = { idField = it },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
            placeholder = { Text("e.g. aisha") },
            singleLine = true
        )

        Text("Password", fontSize = 12.sp, color = Color(0xFF64748B))
        OutlinedTextField(
            value = pwField,
            onValueChange = { pwField = it },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
            placeholder = { Text("••••••••") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        error?.let {
            Text(it, color = Color(0xFFDC2626), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        Button(
            onClick = { attemptLogin() },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Ink),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Log in", color = Color.White)
        }
    }
}

@Composable
fun HomeScreen(session: UserAccount, onLogout: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }
    var showAdd by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<Customer?>(null) }

    val visible = AppData.customers
        .filter { session.role == Role.ADMIN || it.assignedTo == session.id }
        .filter { filter == "All" || it.status.label == filter }
        .filter {
            query.isBlank() ||
                it.name.contains(query, ignoreCase = true) ||
                it.phone.contains(query)
        }
        .sortedByDescending { it.dateAdded }

    val successCount = AppData.customers.count {
        it.status == Status.SUCCESSFUL && (session.role == Role.ADMIN || it.assignedTo == session.id)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(modifier = Modifier.padding(20.dp, 16.dp, 20.dp, 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (session.role == Role.ADMIN) "All customers" else "Your customers",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text("Hi, ${session.name}", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Filled.Logout, contentDescription = "Log out", tint = Color(0xFF64748B))
                    }
                }

                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFD1FAE5), RoundedCornerShape(12.dp))
                        .padding(12.dp, 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$successCount successful conversion${if (successCount == 1) "" else "s"}",
                        fontSize = 12.sp,
                        color = Color(0xFF047857)
                    )
                }
            }

            // Search
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                placeholder = { Text("Search name or phone", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            // Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All", "New", "Contacted", "Successful").forEach { f ->
                    val selected = filter == f
                    Box(
                        modifier = Modifier
                            .background(
                                if (selected) Ink else Color.White,
                                RoundedCornerShape(50)
                            )
                            .clickable { filter = f }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(f, fontSize = 12.sp, color = if (selected) Color.White else Color(0xFF64748B))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // List
            if (visible.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (AppData.isSyncing.value) "Inasawazisha..." else "No customers yet.\nTap + to add your first one.",
                        fontSize = 14.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp, 0.dp, 20.dp, 90.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(visible, key = { it.id }) { customer ->
                        CustomerRow(
                            customer = customer,
                            showAssignee = session.role == Role.ADMIN,
                            onOpen = { detail = customer }
                        )
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAdd = true },
            containerColor = Ink,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add customer")
        }
    }

    if (showAdd) {
        AddCustomerDialog(
            session = session,
            onDismiss = { showAdd = false },
            onSave = { name, phone, assignedTo ->
                AppData.addCustomer(name, phone, session.id, assignedTo)
                showAdd = false
            }
        )
    }

    detail?.let { c ->
        CustomerDetailDialog(
            customer = c,
            session = session,
            onDismiss = { detail = null }
        )
    }
}

@Composable
fun CustomerRow(customer: Customer, showAssignee: Boolean, onOpen: () -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(14.dp))
            .clickable { onOpen() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(Color(0xFFF1F5F9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(customer.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Ink)
                Spacer(Modifier.width(6.dp))
                Box(modifier = Modifier.size(6.dp).background(statusColor(customer.status), CircleShape))
            }
            Text(
                "${customer.phone} · ${fmtDate(customer.dateAdded)}",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
            if (showAssignee) {
                Text(
                    "Assigned: ${AppData.userName(customer.assignedTo)}",
                    fontSize = 10.sp,
                    color = Color(0xFFB0B8C1)
                )
            }
        }

        IconButton(onClick = {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}")))
        }) {
            Icon(Icons.Filled.Call, contentDescription = "Call", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = {
            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${customer.phone}")))
        }) {
            Icon(Icons.Filled.Chat, contentDescription = "SMS", tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl(customer.phone))))
        }) {
            Icon(Icons.Filled.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun AddCustomerDialog(
    session: UserAccount,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var assignedTo by remember { mutableStateOf(session.id) }
    val staff = AppData.staffUsers()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add customer", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                Text("Name", fontSize = 12.sp, color = Color(0xFF64748B))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 10.dp),
                    singleLine = true
                )
                Text("Phone number", fontSize = 12.sp, color = Color(0xFF64748B))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 10.dp),
                    singleLine = true
                )
                if (session.role == Role.ADMIN) {
                    Text("Assign to", fontSize = 12.sp, color = Color(0xFF64748B))
                    var expanded by remember { mutableStateOf(false) }
                    val assignedName = AppData.userName(assignedTo)
                    Box {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(assignedName)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            staff.forEach { u ->
                                DropdownMenuItem(text = { Text(u.name) }, onClick = {
                                    assignedTo = u.id
                                    expanded = false
                                })
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, phone, assignedTo) },
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CustomerDetailDialog(customer: Customer, session: UserAccount, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var status by remember { mutableStateOf(customer.status) }
    var assignedTo by remember { mutableStateOf(customer.assignedTo) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val staff = AppData.staffUsers()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(customer.name, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                Text(customer.phone, fontSize = 13.sp, color = Color(0xFF64748B))
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(statusBg(status), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(status.label, fontSize = 12.sp, color = statusColor(status))
                }

                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}")))
                    }, modifier = Modifier.weight(1f)) { Text("Call") }
                    OutlinedButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${customer.phone}")))
                    }, modifier = Modifier.weight(1f)) { Text("SMS") }
                    OutlinedButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl(customer.phone))))
                    }, modifier = Modifier.weight(1f)) { Text("WhatsApp") }
                }

                Spacer(Modifier.height(14.dp))
                Text("Update status", fontSize = 12.sp, color = Color(0xFF64748B))
                Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Status.values().forEach { s ->
                        val selected = status == s
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (selected) Ink else Color.White, RoundedCornerShape(8.dp))
                                .clickable {
                                    status = s
                                    AppData.updateStatus(customer.id, s)
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(s.label, fontSize = 11.sp, color = if (selected) Color.White else Color(0xFF64748B))
                        }
                    }
                }

                if (session.role == Role.ADMIN) {
                    Spacer(Modifier.height(14.dp))
                    Text("Assigned to", fontSize = 12.sp, color = Color(0xFF64748B))
                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.padding(top = 4.dp)) {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(AppData.userName(assignedTo))
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            staff.forEach { u ->
                                DropdownMenuItem(text = { Text(u.name) }, onClick = {
                                    assignedTo = u.id
                                    AppData.reassign(customer.id, u.id)
                                    expanded = false
                                })
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    "Added by ${AppData.userName(customer.addedBy)} on ${fmtDate(customer.dateAdded)}",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(Modifier.height(14.dp))
                TextButton(onClick = { showDeleteConfirm = true }) {
                    Text("Delete customer", color = Color(0xFFDC2626))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete customer?") },
            text = { Text("This will permanently remove ${customer.name}. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    AppData.deleteCustomer(customer.id)
                    showDeleteConfirm = false
                    onDismiss()
                }) { Text("Delete", color = Color(0xFFDC2626)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
