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
import com.powerbank.medsure.ui.components.*
import com.powerbank.medsure.ui.theme.*
import com.powerbank.medsure.viewmodel.HospitalViewModel

@Composable
fun DischargesScreen(
    viewModel: HospitalViewModel,
    onViewPatientPack: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    var processSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MedSureTopBar(
                title = "Discharge Processing",
                subtitle = "Upload discharge files & trigger OCR + AI extraction"
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
            // Processing status feedback
            if (processSuccess) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StatusSuccessBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Discharge Pack Processed Successfully!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StatusSuccess,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "OCR extracted 2 pages • 4 plain-language cards generated with source citations.",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusSuccess
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Case Card: Rajesh Sharma (MS-001)
            MedSureCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Rajesh Sharma",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Case #MS-001 • Cardiology (ICU Bed 04)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    StatusBadge(text = "DISCHARGED", severity = BadgeSeverity.SUCCESS)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = BackgroundLight,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = StatusRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "discharge_summary_ms001.pdf (2 Pages)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Extracted via PyMuPDF • Confidence: 96% • Verified",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusSuccess
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Upload / Re-process Button
                    Button(
                        onClick = {
                            isProcessing = true
                            processSuccess = false
                            // Trigger backend processing
                            viewModel.loadDeskData()
                            isProcessing = false
                            processSuccess = true
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Process OCR & AI")
                    }

                    // View Patient Cards Button
                    OutlinedButton(
                        onClick = onViewPatientPack,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Preview Pack")
                    }
                }
            }
        }
    }
}
