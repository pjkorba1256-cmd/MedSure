package com.powerbank.medsure.ui.screens.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.powerbank.medsure.data.model.CheckInResponse
import com.powerbank.medsure.ui.components.*
import com.powerbank.medsure.ui.theme.*
import com.powerbank.medsure.viewmodel.CheckInUiState
import com.powerbank.medsure.viewmodel.CheckInViewModel

@Composable
fun CheckInScreen(
    viewModel: CheckInViewModel,
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()

    var pain by remember { mutableStateOf(2) }
    var fever by remember { mutableStateOf(false) }
    var breathingDifficulty by remember { mutableStateOf(false) }
    var dizziness by remember { mutableStateOf(false) }
    var vomiting by remember { mutableStateOf(false) }
    var freeText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            MedSureTopBar(
                title = "Daily Recovery Check-In",
                subtitle = "Monitored directly by hospital clinical desk"
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        when (val state = uiState) {
            is CheckInUiState.Submitting -> {
                LoadingIndicatorView("Transmitting check-in and analyzing rules...")
            }
            is CheckInUiState.Submitted -> {
                CheckInSubmittedView(
                    response = state.response,
                    onNewCheckIn = { viewModel.resetState() },
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Clinical Safety Notice
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandNavyDark.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandNavy.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = BrandNavy, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Answers are reviewed by hospital clinical algorithms. Emergency symptoms trigger immediate desk alerts.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandNavyDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Question 1: Pain Level (0 - 10)
                    MedSureCard {
                        Text(
                            text = "1. Current Pain Level (0 - 10)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "0 = No pain, 10 = Worst imaginable pain",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Score: $pain/10", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (pain >= 7) StatusRed else BrandNavy)
                            StatusBadge(
                                text = if (pain >= 9) "SEVERE" else if (pain >= 7) "ELEVATED" else "NORMAL",
                                severity = if (pain >= 9) BadgeSeverity.RED else if (pain >= 7) BadgeSeverity.AMBER else BadgeSeverity.SUCCESS
                            )
                        }

                        Slider(
                            value = pain.toFloat(),
                            onValueChange = { pain = it.toInt() },
                            valueRange = 0f..10f,
                            steps = 9,
                            colors = SliderDefaults.colors(thumbColor = BrandNavy, activeTrackColor = BrandNavy)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Question 2: Difficulty Breathing (Key Trigger!)
                    BooleanQuestionCard(
                        title = "2. Are you experiencing difficulty breathing?",
                        subtitle = "Shortness of breath at rest or rapid breathing (Emergency indicator)",
                        value = breathingDifficulty,
                        onValueChange = { breathingDifficulty = it },
                        highlightIfYes = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Question 3: Fever
                    BooleanQuestionCard(
                        title = "3. Do you have a fever (temperature > 100°F)?",
                        subtitle = "Chills, cold sweats, or high body temperature",
                        value = fever,
                        onValueChange = { fever = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Question 4: Dizziness
                    BooleanQuestionCard(
                        title = "4. Do you feel lightheaded or dizzy?",
                        subtitle = "Feeling unsteady while standing or walking",
                        value = dizziness,
                        onValueChange = { dizziness = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Question 5: Vomiting
                    BooleanQuestionCard(
                        title = "5. Have you had nausea or vomiting?",
                        subtitle = "Inability to keep medications or meals down",
                        value = vomiting,
                        onValueChange = { vomiting = it }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Question 6: Free Text
                    MedSureCard {
                        Text(
                            text = "6. Anything else you'd like to report?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Report any other sensations, swelling, or questions for the hospital desk.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = freeText,
                            onValueChange = { freeText = it },
                            placeholder = { Text("e.g. Mild tenderness at radial catheter site...") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            viewModel.submitCheckIn(
                                pain = pain,
                                fever = fever,
                                breathingDifficulty = breathingDifficulty,
                                dizziness = dizziness,
                                vomiting = vomiting,
                                freeText = freeText
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SUBMIT TODAY'S CHECK-IN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

@Composable
fun BooleanQuestionCard(
    title: String,
    subtitle: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    highlightIfYes: Boolean = false
) {
    MedSureCard(
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (value && highlightIfYes) StatusRedBorder else SurfaceBorder
        ),
        backgroundColor = if (value && highlightIfYes) StatusRedBg.copy(alpha = 0.35f) else SurfaceCard
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // YES button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onValueChange(true) },
                shape = RoundedCornerShape(10.dp),
                color = if (value) (if (highlightIfYes) StatusRed else BrandNavy) else BackgroundLight,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (value) Color.Transparent else SurfaceBorder)
            ) {
                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "YES",
                        fontWeight = FontWeight.Bold,
                        color = if (value) Color.White else TextPrimary
                    )
                }
            }

            // NO button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onValueChange(false) },
                shape = RoundedCornerShape(10.dp),
                color = if (!value) BrandTeal else BackgroundLight,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (!value) Color.Transparent else SurfaceBorder)
            ) {
                Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "NO",
                        fontWeight = FontWeight.Bold,
                        color = if (!value) Color.White else TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun CheckInSubmittedView(
    response: CheckInResponse,
    onNewCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasRedAlert = response.alertsTriggered.any { it.contains("Breathing", ignoreCase = true) || it.contains("Severe", ignoreCase = true) || it.contains("Chest", ignoreCase = true) }
    val hasAlerts = response.alertsTriggered.isNotEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    if (hasRedAlert) StatusRed.copy(alpha = 0.15f) else StatusSuccess.copy(alpha = 0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (hasRedAlert) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (hasRedAlert) StatusRed else StatusSuccess,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (hasRedAlert) "Immediate Clinical Alert Dispatched" else "Check-in Logged Successfully",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (hasRedAlert) StatusRed else TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (hasRedAlert)
                "Hospital desk received an urgent RED ALERT regarding difficulty breathing."
            else
                "Your recovery stats have been updated and synced with MedSure Trust Hospital desk.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (hasAlerts) {
            MedSureCard(
                backgroundColor = if (hasRedAlert) StatusRedBg else StatusAmberBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (hasRedAlert) StatusRedBorder else StatusAmberBorder)
            ) {
                Text(
                    text = "DETERMINISTIC RULES ENGINE OUTPUT:",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (hasRedAlert) StatusRed else StatusAmber,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                response.alertsTriggered.forEach { trigger ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Icon(
                            imageVector = if (hasRedAlert) Icons.Default.Error else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (hasRedAlert) StatusRed else StatusAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = trigger, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onNewCheckIn,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandNavy)
        ) {
            Text("Done / View Recovery Plan")
        }
    }
}
