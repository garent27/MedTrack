package com.garent.s35123656.medtrack

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garent.s35123656.medtrack.data.viewModel.SymptomsViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomTrendsScreen(
    patientId: String,
    viewModel: SymptomsViewModel,
    onBack: () -> Unit
) {
    val trendData by viewModel.getSymptomTrends(patientId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Symptom Trends", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Severity Over Time",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "This chart shows the average severity of your logged symptoms over the last 7 days of entries.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (trendData.size < 2) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Not enough data to show trends yet.\nKeep logging your symptoms!",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                SymptomTrendChart(data = trendData)
            }
        }
    }
}

/**
 * Custom-drawn trend chart demonstrating technical depth using Canvas.
 */
@Composable
fun SymptomTrendChart(data: List<Pair<String, Float>>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = Color.LightGray.copy(alpha = 0.5f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val spacing = width / (data.size - 1).coerceAtLeast(1)
                
                // Draw horizontal grid lines (Severity 1-10)
                for (i in 0..5) {
                    val y = height - (i * height / 5)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Create path for the trend line
                val path = Path()
                data.forEachIndexed { index, pair ->
                    val x = index * spacing
                    // Y calculation: lower severity is higher on screen coordinate, so we invert
                    val y = height - (pair.second / 10f * height)
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                    
                    // Draw point
                    drawCircle(
                        color = primaryColor,
                        radius = 5.dp.toPx(),
                        center = Offset(x, y)
                    )
                }

                // Draw the line
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
        }
    }
    
    // X-Axis Labels
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        data.forEach { pair ->
            Text(text = pair.first, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
