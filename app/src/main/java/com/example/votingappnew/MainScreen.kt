package com.example.votingappnew

import android.widget.Toast
import com.example.votingappnew.models.Candidate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MainScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return

    var candidates by remember { mutableStateOf(listOf<Candidate>()) }
    var hasVoted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Check if user voted
    LaunchedEffect(Unit) {
        db.collection("votes")
            .whereEqualTo("user_ID", userId)
            .get()
            .addOnSuccessListener { documents ->
                hasVoted = !documents.isEmpty
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
                errorMessage = "خطأ: ${it.message}"
            }
    }

    // Load candidates
    LaunchedEffect(Unit) {
        db.collection("candidates")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    errorMessage = "خطأ: ${error.message}"
                    return@addSnapshotListener
                }
                snapshot?.let {
                    val candidateList = it.documents.mapNotNull { doc ->
                        Candidate(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            age = doc.getLong("age") ?: 0,
                            totalVotes = doc.getLong("total-votes") ?: 0,
                            imageUrl = doc.getString("imageUrl")
                        )
                    }
                    candidates = candidateList
                }
            }
    }

    // Show error
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            errorMessage = null
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // UI layout
    Scaffold(
        bottomBar = {
            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                    Toast.makeText(context, "تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("تسجيل الخروج")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(text = "المرشحين", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(candidates) { candidate ->
                    CandidateItem(
                        candidate = candidate,
                        hasVoted = hasVoted,
                        onVote = {
                            val vote = hashMapOf(
                                "user_ID" to userId,
                                "vote_Choice" to candidate.id,
                                "timestamp" to FieldValue.serverTimestamp()
                            )
                            db.collection("votes")
                                .add(vote)
                                .addOnSuccessListener {
                                    db.collection("candidates")
                                        .document(candidate.id)
                                        .update("total-votes", FieldValue.increment(1))
                                        .addOnSuccessListener {
                                            hasVoted = true
                                            Toast.makeText(context, "تم التصويت!", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener {
                                    errorMessage = "خطأ: ${it.message}"
                                }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CandidateItem(
    candidate: Candidate,
    hasVoted: Boolean,
    onVote: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlideImage(
                model = candidate.imageUrl,
                contentDescription = "صورة ${candidate.name}",
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Crop
            ) {
                it.placeholder(android.R.drawable.ic_menu_report_image)
                    .error(android.R.drawable.ic_menu_report_image)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = candidate.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "العمر: ${candidate.age}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "الأصوات: ${candidate.totalVotes}", style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = onVote,
                enabled = !hasVoted,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasVoted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    contentColor = if (hasVoted) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (hasVoted) "تم التصويت" else "صوت")
            }
        }
    }
}
