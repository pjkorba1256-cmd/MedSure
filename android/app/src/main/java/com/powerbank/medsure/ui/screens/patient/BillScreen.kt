package com.powerbank.medsure.ui.screens.patient

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
import com.powerbank.medsure.data.model.BillItem
import com.powerbank.medsure.data.model.Query
import com.powerbank.medsure.ui.components.*
import com.powerbank.medsure.ui.theme.*
import com.powerbank.medsure.viewmodel.BillUiState
import com.powerbank.medsure.viewmodel.BillViewModel

@Composable
fun BillScreen(
    viewModel: BillViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val eventMsg by viewModel.querySentEvent.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedItemForQuery by remember { mutableStateOf<BillItem?>(null) }

    LaunchedEffect(eventMsg) {
        eventMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearQueryEvent()
        }
    }

    Scaffold(
        topBar = {
            MedSureTopBar(
                title = "Hospital Bill Explainer",
                subtitle = "Itemized breakdown & 'Worth asking about' checks"
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundLight
    ) { padding ->
        when (val state = uiState) {
            is BillUiState.Loading -> {
                LoadingIndicatorView("Analyzing hospital invoice lines...")
            }
            is BillUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = StatusRed)
                }
            }
            is BillUiState.Success -> {
                BillContent(
                    items = state.items,
                    queries = state.queries,
                    totalAmount = state.totalAmount,
                    flaggedCount = state.flaggedCount,
                    onAskHospital = { item -> selectedItemForQuery = item },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    selectedItemForQuery?.let { item ->
        AskHospitalDialog(
            item = item,
            onDismiss = { selectedItemForQuery = null },
            onSubmit = { message ->
                viewModel.askTheHospital(item.id, message)
                selectedItemForQuery = null
            }
        )
    }
}

@Composable
fun BillContent(
    items: List<BillItem>,
    queries: List<Query>,
    totalAmount: Double,
    flaggedCount: Int,
    onAskHospital: (BillItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Summary Header Card
        MedSureCard(backgroundColor = BrandNavyDark) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL HOSPITAL CHARGES",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${"%,.2f".format(totalAmount)}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = StatusAmber.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusAmber)
                ) {
                    Text(
                        text = "$flaggedCount Worth Asking About",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusAmberBorder,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "ITEMIZED CHARGES (${items.size} ITEMS)",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        items.forEach { item ->
            val linkedQuery = queries.firstOrNull { it.billItemId == item.id }

            MedSureCard(
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (item.flagged) StatusAmberBorder else SurfaceBorder
                ),
                backgroundColor = if (item.flagged) StatusAmberBg.copy(alpha = 0.35f) else SurfaceCard
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Category: ${item.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Text(
                        text = "₹${"%,.2f".format(item.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                if (!item.explanation.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = item.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (item.flagged) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(
                            text = item.flagReason ?: "Worth asking about",
                            severity = BadgeSeverity.AMBER
                        )

                        Button(
                            onClick = { onAskHospital(item) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandNavy),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.QuestionAnswer, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ask Hospital", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Show query & reply if exists
                if (linkedQuery != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = BackgroundLight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = BrandNavy, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Your Query: ${linkedQuery.message}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                            if (!linkedQuery.reply.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = StatusSuccessBg,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Hospital Desk Reply: ${linkedQuery.reply}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = StatusSuccess
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Status: Waiting for Hospital Desk reply",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusAmber
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AskHospitalDialog(
    item: BillItem,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var queryText by remember {
        mutableStateOf("Can you please clarify what is included under '${item.description}'?")
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
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = BrandNavy, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Ask the Hospital Desk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Regarding: ${item.description}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    label = { Text("Your Question") },
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
                        onClick = { onSubmit(queryText) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
                    ) {
                        Text("Send Query")
                    }
                }
            }
        }
    }
}
