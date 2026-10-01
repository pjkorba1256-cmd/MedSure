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
import com.powerbank.medsure.data.model.Alert
import com.powerbank.medsure.ui.components.*
import com.powerbank.medsure.ui.theme.*
import com.powerbank.medsure.viewmodel.HospitalUiState
import com.powerbank.medsure.viewmodel.HospitalViewModel

@Composable
fun AlertsScreen(
    viewModel: HospitalViewModel,
    onBackClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            MedSureTopBar(
                title = "Clinical Recovery Alerts",
                subtitle = "Real-time alerts triggered by deterministic check-in rules"
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        when (val state = uiState) {
            is HospitalUiState.Loading -> {
                LoadingIndicatorView("Retrieving clinical alert feed...")
            }
            is HospitalUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = StatusRed)
                }
            }
            is HospitalUiState.Success -> {
                AlertsContent(
                    alerts = state.alerts,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
fun AlertsContent(
    alerts: List<Alert>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Clinical Safety Protocol Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = BrandNavyDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = BrandTeal, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Deterministic Guardrails Active",
                        style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Medical alerts are generated strictly via rules.yaml thresholds without AI conjecture.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (alerts.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.CheckCircle,
                title = "No Active Alerts",
                subtitle = "All discharged patients are within normal recovery thresholds."
            )
        } else {
            alerts.forEach { alert ->
                val isRed = alert.severity == "RED"

                MedSureCard(
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isRed) StatusRedBorder else StatusAmberBorder
                    ),
                    backgroundColor = if (isRed) StatusRedBg.copy(alpha = 0.35f) else StatusAmberBg.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        if (isRed) StatusRed.copy(alpha = 0.15f) else StatusAmber.copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isRed) Icons.Default.Error else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isRed) StatusRed else StatusAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = alert.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRed) StatusRed else StatusAmber
                                )
                                Text(
                                    text = "Rule ID: ${alert.ruleId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }

                        StatusBadge(
                            text = if (isRed) "RED ALERT" else "AMBER ALERT",
                            severity = if (isRed) BadgeSeverity.RED else BadgeSeverity.AMBER
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = SurfaceCard,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row {
                                Text("Patient: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(alert.patientName ?: "Rajesh Sharma", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("Case: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(alert.caseId, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Summary: ${alert.summary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { /* Acknowledge / contact */ },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRed) StatusRed else BrandNavy
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isRed) "Emergency Contact Patient" else "Review Case")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
