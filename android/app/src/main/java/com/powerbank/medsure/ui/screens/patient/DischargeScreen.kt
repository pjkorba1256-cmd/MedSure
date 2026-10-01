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
import com.powerbank.medsure.data.model.DischargePack
import com.powerbank.medsure.ui.components.*
import com.powerbank.medsure.ui.theme.*
import com.powerbank.medsure.viewmodel.DischargeUiState
import com.powerbank.medsure.viewmodel.DischargeViewModel

@Composable
fun DischargeScreen(
    viewModel: DischargeViewModel,
    onBackClick: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            MedSureTopBar(
                title = "Discharge Pack",
                subtitle = "Plain-Language Guide with Source Attribution"
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        when (val state = uiState) {
            is DischargeUiState.Loading -> {
                LoadingIndicatorView("Extracting and verifying discharge records...")
            }
            is DischargeUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = StatusRed)
                }
            }
            is DischargeUiState.Success -> {
                DischargeContent(pack = state.pack, modifier = Modifier.padding(padding))
            }
        }
    }
}

@Composable
fun DischargeContent(
    pack: DischargePack,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Clinical Overview Callout
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = BrandTealLight.copy(alpha = 0.35f),
            border = androidx.compose.foundation.BorderStroke(1.dp, BrandTeal.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = BrandTeal, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Source-Verified: All cards are extracted directly from hospital discharge summary without medical hallucinations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BrandNavyDark,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CARD 1: WHAT HAPPENED
        MedSureCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).background(BrandNavy.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BrandNavy, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "WHAT HAPPENED",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandNavy
                    )
                }
                SourceCitationChip(source = pack.whatHappened.source)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = pack.whatHappened.text,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CARD 2: WHAT TO TAKE
        MedSureCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).background(BrandTeal.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Medication, contentDescription = null, tint = BrandTeal, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "WHAT TO TAKE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandTeal
                    )
                }
                if (pack.medications.isNotEmpty()) {
                    SourceCitationChip(source = pack.medications[0].source)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            pack.medications.forEachIndexed { index, med ->
                Surface(
                    color = BackgroundLight,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${med.name} (${med.dosage})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            StatusBadge(text = med.timing, severity = BadgeSeverity.NORMAL)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = med.purpose,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CARD 3: WHEN TO RETURN
        MedSureCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).background(StatusPending.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null, tint = StatusPending, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "WHEN TO RETURN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = StatusPending
                    )
                }
                SourceCitationChip(source = pack.followUp.source)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(text = pack.followUp.dateOrDays, severity = BadgeSeverity.PENDING)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = pack.followUp.department,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pack.followUp.instructions,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CARD 4: WHAT TO WATCH FOR
        MedSureCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).background(StatusRed.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "WHAT TO WATCH FOR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = StatusRed
                    )
                }
                if (pack.whatToWatch.isNotEmpty()) {
                    SourceCitationChip(source = pack.whatToWatch[0].source)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            pack.whatToWatch.forEach { watch ->
                val isRed = watch.severity == "RED"
                Surface(
                    color = if (isRed) StatusRedBg else StatusAmberBg,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isRed) StatusRedBorder else StatusAmberBorder),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = watch.warning,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isRed) StatusRed else StatusAmber
                            )
                            StatusBadge(
                                text = if (isRed) "EMERGENCY" else "CAUTION",
                                severity = if (isRed) BadgeSeverity.RED else BadgeSeverity.AMBER
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Action: ${watch.action}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
