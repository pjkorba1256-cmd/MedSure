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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.powerbank.medsure.data.session.SessionManager
import com.powerbank.medsure.ui.components.*
import com.powerbank.medsure.ui.theme.*

@Composable
fun PatientHomeScreen(
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
                title = "MedSure Recovery",
                subtitle = "Case #MS-001 • MedSure Trust Hospital",
                roleBadge = user?.role ?: "PATIENT",
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
            // Patient Welcome Banner
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
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = BrandTealLight,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Hello, ${user?.name ?: "Rajesh Sharma"}",
                            style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Post-PCI Cardiac Recovery • Day 2",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.8f))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action: Daily Check-in Card (High priority)
            MedSureCard(
                backgroundColor = StatusAmberBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusAmberBorder),
                onClick = onNavigateToCheckIn
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = StatusAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Today's Recovery Check-in",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Takes 1 minute • Direct hospital monitor",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        tint = StatusAmber,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "YOUR POST-DISCHARGE PACK",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Grid Cards
            ModuleNavigationCard(
                title = "Discharge Pack Explained",
                subtitle = "What happened, medications, and warning signs with source citations.",
                icon = Icons.Default.Description,
                badge = "4 Cards Ready",
                badgeSeverity = BadgeSeverity.SUCCESS,
                onClick = onNavigateToDischarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            ModuleNavigationCard(
                title = "Bills & Fees Breakdown",
                subtitle = "Itemized hospital charges with 'Worth asking about' checks.",
                icon = Icons.Default.ReceiptLong,
                badge = "2 Flagged",
                badgeSeverity = BadgeSeverity.AMBER,
                onClick = onNavigateToBills
            )

            Spacer(modifier = Modifier.height(12.dp))

            ModuleNavigationCard(
                title = "Insurance & Claims Tracker",
                subtitle = "Document checklist, missing page scan, and family-approved claim drafts.",
                icon = Icons.Default.Security,
                badge = "Action Needed",
                badgeSeverity = BadgeSeverity.RED,
                onClick = onNavigateToInsurance
            )
        }
    }
}

@Composable
fun ModuleNavigationCard(
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
