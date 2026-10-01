package com.powerbank.medsure.ui.screens.family

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.powerbank.medsure.data.session.SessionManager
import com.powerbank.medsure.ui.components.*
import com.powerbank.medsure.ui.screens.patient.ModuleNavigationCard
import com.powerbank.medsure.ui.theme.*

@Composable
fun FamilyHomeScreen(
    onNavigateToDischarge: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToInsurance: () -> Unit,
    onNavigateToCheckIn: () -> Unit,
    onLogout: () -> Unit
) {
    val user = SessionManager.currentUser.value

    Scaffold(
        topBar = {
            MedSureTopBar(
                title = "Family Care Portal",
                subtitle = "Patient: Rajesh Sharma • Case #MS-001",
                roleBadge = "FAMILY_CAREGIVER",
                onLogoutClick = onLogout
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Caregiver Welcome Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BrandNavyDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(BrandTeal.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FamilyRestroom,
                            contentDescription = null,
                            tint = BrandTealLight,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Hello, ${user?.name ?: "Anita Sharma"}",
                            style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Caring for Rajesh Sharma (Spouse)",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.8f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Urgent Family Approval Gate Card (High Priority!)
            MedSureCard(
                backgroundColor = StatusAmberBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusAmberBorder),
                onClick = onNavigateToInsurance
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LockClock,
                            contentDescription = null,
                            tint = StatusAmber,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Family Approval Required",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "AI Claim draft ready • Review and authorize dispatch to insurer.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    StatusBadge(text = "GATE PENDING", severity = BadgeSeverity.AMBER)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SHARED CASE MANAGEMENT",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            ModuleNavigationCard(
                title = "Discharge Pack",
                subtitle = "Plain-language summary of what happened, medication schedules, and return instructions.",
                icon = Icons.Default.Description,
                badge = "Verified Source",
                badgeSeverity = BadgeSeverity.SUCCESS,
                onClick = onNavigateToDischarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            ModuleNavigationCard(
                title = "Bills & Fees (Ask Hospital)",
                subtitle = "Review flagged items and send direct queries to the hospital billing desk.",
                icon = Icons.Default.ReceiptLong,
                badge = "2 Flagged",
                badgeSeverity = BadgeSeverity.AMBER,
                onClick = onNavigateToBills
            )

            Spacer(modifier = Modifier.height(12.dp))

            ModuleNavigationCard(
                title = "Insurance Tracker & Checklist",
                subtitle = "Monitor claim status, detect missing pages, and approve claim drafts.",
                icon = Icons.Default.Security,
                badge = "Denied - Fix",
                badgeSeverity = BadgeSeverity.RED,
                onClick = onNavigateToInsurance
            )

            Spacer(modifier = Modifier.height(12.dp))

            ModuleNavigationCard(
                title = "Daily Recovery Check-In",
                subtitle = "Log patient symptoms and review real-time alerts.",
                icon = Icons.Default.MedicalServices,
                badge = "Daily Monitor",
                badgeSeverity = BadgeSeverity.NORMAL,
                onClick = onNavigateToCheckIn
            )
        }
    }
}
