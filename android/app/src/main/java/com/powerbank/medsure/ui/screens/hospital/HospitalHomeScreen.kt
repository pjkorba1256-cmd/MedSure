package com.powerbank.medsure.ui.screens.hospital

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.powerbank.medsure.ui.components.*
import com.powerbank.medsure.ui.theme.*
import com.powerbank.medsure.viewmodel.HospitalUiState
import com.powerbank.medsure.viewmodel.HospitalViewModel

@Composable
fun HospitalHomeScreen(
    viewModel: HospitalViewModel,
    onNavigateToDischarges: () -> Unit,
    onNavigateToQueries: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            MedSureTopBar(
                title = "Hospital Operations Desk",
                subtitle = "MedSure Trust Hospital • Inpatient Care Coordination",
                roleBadge = "HOSPITAL_DESK",
                onLogoutClick = onLogout
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        when (val state = uiState) {
            is HospitalUiState.Loading -> {
                LoadingIndicatorView("Connecting to clinical desk feed...")
            }
            is HospitalUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = StatusRed)
                }
            }
            is HospitalUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Quick Status Cards Banner
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Metric 1: RED Alerts
                        MetricCard(
                            title = "RED ALERTS",
                            value = "${state.redAlertCount}",
                            icon = Icons.Default.Warning,
                            color = StatusRed,
                            bgColor = StatusRedBg,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToAlerts
                        )

                        // Metric 2: Open Queries
                        MetricCard(
                            title = "BILL QUERIES",
                            value = "${state.openQueryCount}",
                            icon = Icons.Default.QuestionAnswer,
                            color = StatusAmber,
                            bgColor = StatusAmberBg,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToQueries
                        )

                        // Metric 3: Active Cases
                        MetricCard(
                            title = "CASES",
                            value = "1",
                            icon = Icons.Default.LocalHospital,
                            color = BrandNavy,
                            bgColor = BrandTealLight.copy(alpha = 0.3f),
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToDischarges
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "OPERATIONAL DESK WORKSTATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Clinical Alerts Workstation (High Priority)
                    DeskNavigationCard(
                        title = "Clinical Recovery Alerts",
                        subtitle = "Real-time alerts triggered by deterministic check-in rules engine.",
                        icon = Icons.Default.Emergency,
                        badge = if (state.redAlertCount > 0) "${state.redAlertCount} RED ALERT" else "All Clear",
                        badgeSeverity = if (state.redAlertCount > 0) BadgeSeverity.RED else BadgeSeverity.SUCCESS,
                        onClick = onNavigateToAlerts
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Bill Queries Workstation
                    DeskNavigationCard(
                        title = "Family Bill Queries",
                        subtitle = "Inquiries from caregivers regarding specific invoice items.",
                        icon = Icons.Default.ReceiptLong,
                        badge = "${state.queries.size} Inquiries",
                        badgeSeverity = BadgeSeverity.AMBER,
                        onClick = onNavigateToQueries
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Discharge Management Workstation
                    DeskNavigationCard(
                        title = "Discharge Processing & OCR",
                        subtitle = "Upload discharge summaries, extract text with PyMuPDF, and trigger AI structured packs.",
                        icon = Icons.Default.UploadFile,
                        badge = "Case MS-001 Active",
                        badgeSeverity = BadgeSeverity.NORMAL,
                        onClick = onNavigateToDischarges
                    )
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

@Composable
fun DeskNavigationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badge: String,
    badgeSeverity: BadgeSeverity,
    onClick: () -> Unit
) {
    MedSureCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(BrandNavy.copy(alpha = 0.08f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = BrandNavy, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            StatusBadge(text = badge, severity = badgeSeverity)
        }
    }
}
