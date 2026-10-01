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
import com.powerbank.medsure.data.model.Claim
import com.powerbank.medsure.data.model.ClaimDraftResponse
import com.powerbank.medsure.data.session.SessionManager
import com.powerbank.medsure.ui.components.*
import com.powerbank.medsure.ui.theme.*
import com.powerbank.medsure.viewmodel.InsuranceUiState
import com.powerbank.medsure.viewmodel.InsuranceViewModel

@Composable
fun InsuranceScreen(
    viewModel: InsuranceViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = SessionManager.currentUser.value
    val isFamilyOrPatient = user?.role == "FAMILY" || user?.role == "PATIENT"

    Scaffold(
        topBar = {
            MedSureTopBar(
                title = "Insurance Tracker",
                subtitle = "Claim Checklist, AI Draft & Approval Gate"
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        when (val state = uiState) {
            is InsuranceUiState.Loading -> {
                LoadingIndicatorView("Verifying insurance documents & claim status...")
            }
            is InsuranceUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = StatusRed)
                }
            }
            is InsuranceUiState.Success -> {
                InsuranceContent(
                    claim = state.claim,
                    draft = state.draft,
                    approvalMessage = state.approvalMessage,
                    isFamilyOrPatient = isFamilyOrPatient,
                    onGenerateDraft = { viewModel.generateClaimDraft() },
                    onApproveDraft = { viewModel.approveAndSendDraft() },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
fun InsuranceContent(
    claim: Claim,
    draft: ClaimDraftResponse?,
    approvalMessage: String?,
    isFamilyOrPatient: Boolean,
    onGenerateDraft: () -> Unit,
    onApproveDraft: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Success / Action feedback banner
        if (approvalMessage != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = StatusSuccessBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = approvalMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StatusSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Claim Status Card
        MedSureCard(backgroundColor = BrandNavyDark) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HEALTH REIMBURSEMENT CLAIM",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = claim.claimNumber,
                        style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "MedSure Trust Hospital • Case MS-001",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                    )
                }

                val statusSeverity = when (claim.status) {
                    "DENIED" -> BadgeSeverity.RED
                    "REIMBURSEMENT", "APPROVED" -> BadgeSeverity.SUCCESS
                    else -> BadgeSeverity.PENDING
                }
                StatusBadge(text = claim.status, severity = statusSeverity)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Required Documents Checklist Section
        Text(
            text = "REQUIRED DOCUMENTS CHECKLIST",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        claim.documents.forEach { doc ->
            val hasWarning = doc.pageCountWarning != null
            val isMissing = !doc.present

            MedSureCard(
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isMissing) StatusRedBorder else if (hasWarning) StatusAmberBorder else SurfaceBorder
                ),
                backgroundColor = if (isMissing) StatusRedBg.copy(alpha = 0.3f) else if (hasWarning) StatusAmberBg.copy(alpha = 0.3f) else SurfaceCard
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (isMissing) StatusRed.copy(alpha = 0.1f) else StatusSuccess.copy(alpha = 0.12f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isMissing) Icons.Default.Close else Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isMissing) StatusRed else StatusSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = doc.documentType,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            if (doc.pageCountWarning != null) {
                                Text(
                                    text = doc.pageCountWarning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isMissing) StatusRed else StatusAmber,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = "${doc.pageCount} page(s) verified",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    if (isMissing) {
                        StatusBadge(text = "MISSING", severity = BadgeSeverity.RED)
                    } else if (hasWarning) {
                        StatusBadge(text = "PAGE GAP", severity = BadgeSeverity.AMBER)
                    } else {
                        StatusBadge(text = "VERIFIED", severity = BadgeSeverity.SUCCESS)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // AI CLAIM DRAFT & FAMILY APPROVAL GATE
        if (claim.status == "DENIED" || draft != null) {
            Text(
                text = "AI-ASSISTED CLAIM RE-SUBMISSION",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (draft == null) {
                MedSureCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).background(BrandTeal.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BrandTeal, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Draft Claim Resubmission Pack",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "AI compiles treatment records and diagnostic explanation for insurer re-evaluation.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onGenerateDraft,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate AI Claim Draft")
                    }
                }
            } else {
                // Draft Generated - Family Approval Required
                MedSureCard(
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandNavy.copy(alpha = 0.3f))
                ) {
                    Surface(
                        color = StatusAmberBg,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusAmberBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI-generated draft — review before approval.",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = StatusAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Claim Resubmission Letter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Claim Amount: ₹${"%,.2f".format(draft.claimAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = BrandNavy,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = BackgroundLight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = draft.draftText,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ATTACHED SUPPORTING DOCUMENTS (${draft.supportingDocuments.size}):",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    draft.supportingDocuments.forEach { docName ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = docName, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // APPROVE & SEND BUTTON (Strict Family Approval Gate)
                    Button(
                        onClick = onApproveDraft,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("APPROVE & SEND TO INSURER", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
