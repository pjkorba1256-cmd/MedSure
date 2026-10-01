package com.powerbank.medsure.ui.screens.hospital

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.powerbank.medsure.data.model.Query
import com.powerbank.medsure.ui.components.*
import com.powerbank.medsure.ui.theme.*
import com.powerbank.medsure.viewmodel.HospitalUiState
import com.powerbank.medsure.viewmodel.HospitalViewModel

@Composable
fun QueriesScreen(
    viewModel: HospitalViewModel,
    onBackClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val actionMsg by viewModel.actionMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var replyingQuery by remember { mutableStateOf<Query?>(null) }

    LaunchedEffect(actionMsg) {
        actionMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionMessage()
        }
    }

    Scaffold(
        topBar = {
            MedSureTopBar(
                title = "Family Bill Queries",
                subtitle = "Direct caregiver inquiries regarding invoice items"
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundLight
    ) { padding ->
        when (val state = uiState) {
            is HospitalUiState.Loading -> {
                LoadingIndicatorView("Fetching queries from families...")
            }
            is HospitalUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = StatusRed)
                }
            }
            is HospitalUiState.Success -> {
                QueriesContent(
                    queries = state.queries,
                    onReplyClick = { q -> replyingQuery = q },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    replyingQuery?.let { query ->
        HospitalReplyDialog(
            query = query,
            onDismiss = { replyingQuery = null },
            onSubmit = { replyText ->
                viewModel.replyToQuery(query.id, replyText)
                replyingQuery = null
            }
        )
    }
}

@Composable
fun QueriesContent(
    queries: List<Query>,
    onReplyClick: (Query) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "CAREGIVER INQUIRIES (${queries.size})",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (queries.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.ChatBubbleOutline,
                title = "No Pending Inquiries",
                subtitle = "All caregiver bill queries have been answered."
            )
        } else {
            queries.forEach { query ->
                val isResolved = query.status == "RESOLVED"

                MedSureCard(
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isResolved) SurfaceBorder else StatusAmberBorder
                    ),
                    backgroundColor = if (isResolved) SurfaceCard else StatusAmberBg.copy(alpha = 0.25f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "From: ${query.createdByName ?: "Anita Sharma (Family)"}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Case: ${query.caseId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }

                        StatusBadge(
                            text = if (isResolved) "RESOLVED" else "OPEN QUERY",
                            severity = if (isResolved) BadgeSeverity.SUCCESS else BadgeSeverity.AMBER
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = BackgroundLight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (!query.billItemDescription.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Item: ${query.billItemDescription}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandNavy
                                    )
                                    if (query.billItemAmount != null) {
                                        Text(
                                            text = "₹${"%,.2f".format(query.billItemAmount)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandNavy
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = "Question: \"${query.message}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }

                    if (!query.reply.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = StatusSuccessBg,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Your Hospital Reply:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusSuccess
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = query.reply,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { onReplyClick(query) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reply to Family")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun HospitalReplyDialog(
    query: Query,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var replyText by remember {
        mutableStateOf("This administrative charge covers insurance third-party claim liaising, digital case paperwork, and diagnostic consolidation.")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).background(BrandNavy.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Reply, contentDescription = null, tint = BrandNavy, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Hospital Desk Reply", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("To: ${query.createdByName ?: "Caregiver"}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Question: \"${query.message}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Hospital Explanation") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSubmit(replyText) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
                    ) {
                        Text("Send Reply to Family")
                    }
                }
            }
        }
    }
}
