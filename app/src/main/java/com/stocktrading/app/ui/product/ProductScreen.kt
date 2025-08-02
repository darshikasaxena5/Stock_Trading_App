package com.stocktrading.app.ui.product

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmark

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    symbol: String,
    onNavigateBack: () -> Unit,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    
    val scope = rememberCoroutineScope()

    LaunchedEffect(symbol) {
        viewModel.loadStockData(symbol)
    }

    successMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.toggleWatchlist() }
                ) {
                    Icon(
                        imageVector = if (uiState.isInWatchlist) {
                            Icons.Filled.Bookmark
                        } else {
                            Icons.Filled.BookmarkBorder
                        },
                        contentDescription = if (uiState.isInWatchlist) {
                            "Remove from Watchlist"
                        } else {
                            "Add to Watchlist"
                        },
                        tint = if (uiState.isInWatchlist) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                

            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        successMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isInWatchlist) {
                            Icons.Filled.Bookmark
                        } else {
                            Icons.Filled.BookmarkBorder
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.loadStockData(symbol) }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        when {
            isLoading && (uiState.stock == null || uiState.stock?.price == "Loading...") -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Loading $symbol data...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            uiState.stock != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StockPriceCard(stock = uiState.stock!!)

                    DynamicChartCard(
                        symbol = symbol,
                        chartData = uiState.chartData,
                        stock = uiState.stock!!
                    )

                    StatsCard(stock = uiState.stock!!)
                }
            }

            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "No data available for $symbol",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Button(onClick = { viewModel.loadStockData(symbol) }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }

    if (uiState.showWatchlistDialog) {
        WatchlistSelectionDialog(
            stockSymbol = symbol,
            availableWatchlists = uiState.availableWatchlists,
            currentWatchlistIds = uiState.currentWatchlistIds,
            onDismiss = { viewModel.hideWatchlistDialog() },
            onConfirmSelection = { selectedIds ->
                viewModel.confirmWatchlistSelection(selectedIds)
            },
            onCreateNew = { watchlistName ->
                viewModel.createWatchlistAndAddStock(watchlistName)
            }
        )
    }
}

@Composable
private fun StockPriceCard(stock: com.stocktrading.app.data.models.Stock) {
    val changeValue = stock.changePercent
        .replace("%", "")
        .replace("+", "")
        .toFloatOrNull() ?: 0f
    val isPositive = changeValue >= 0
    val changeColor = if (isPositive) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = if (stock.name.isNotEmpty() && stock.name != stock.symbol) {
                    stock.name
                } else {
                    stock.symbol
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when {
                    stock.price == "Loading..." -> "Loading price..."
                    stock.price.isNotEmpty() -> "$${stock.price}"
                    else -> "Price unavailable"
                },
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (stock.change.isNotEmpty() && stock.changePercent.isNotEmpty() &&
                stock.change != "0.00" && stock.changePercent != "0.00%") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${if (isPositive && !stock.change.startsWith("+")) "+" else ""}${stock.change}",
                        style = MaterialTheme.typography.titleMedium,
                        color = changeColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stock.changePercent,
                        style = MaterialTheme.typography.titleMedium,
                        color = changeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicChartCard(
    symbol: String,
    chartData: List<com.stocktrading.app.data.models.ChartPoint>,
    stock: com.stocktrading.app.data.models.Stock
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Price Chart",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val width = size.width
                val height = size.height
                val padding = 40f

                val dataPoints = if (chartData.isNotEmpty()) {
                    chartData.map { it.price }
                } else {
                    generateDynamicChartData(symbol, stock)
                }

                val maxPrice = dataPoints.maxOrNull() ?: 100f
                val minPrice = dataPoints.minOrNull() ?: 50f
                val priceRange = maxPrice - minPrice

                val normalizedRange = if (priceRange > 0) priceRange else 1f

                val points = dataPoints.mapIndexed { index, price ->
                    val divisor = if (dataPoints.size > 1) (dataPoints.size - 1).toFloat() else 1f
                    val x = padding + (index.toFloat() * (width - 2 * padding) / divisor)
                    val y = height - padding - ((price - minPrice) / normalizedRange * (height - 2 * padding))
                    Offset(x, y)
                }

                val gridColor = Color.Gray.copy(alpha = 0.3f)
                for (i in 1..4) {
                    val y = padding + i * (height - 2 * padding) / 5
                    drawLine(
                        color = gridColor,
                        start = Offset(padding, y),
                        end = Offset(width - padding, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (points.size > 1) {
                    val path = Path()
                    points.forEachIndexed { index, point ->
                        if (index == 0) {
                            path.moveTo(point.x, point.y)
                        } else {
                            path.lineTo(point.x, point.y)
                        }
                    }

                    val changeValue = stock.changePercent
                        .replace("%", "")
                        .replace("+", "")
                        .toFloatOrNull() ?: 0f

                    val chartTrendIsPositive = if (dataPoints.size > 1) {
                        val firstPrice = dataPoints.first()
                        val lastPrice = dataPoints.last()
                        lastPrice >= firstPrice
                    } else {
                        changeValue >= 0
                    }

                    val trendColor = if (chartTrendIsPositive) Color(0xFF4CAF50) else Color(0xFFF44336)

                    drawPath(
                        path = path,
                        color = trendColor,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    points.forEach { point ->
                        drawCircle(
                            color = trendColor,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = point
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (chartData.isNotEmpty()) {
                    "Last ${chartData.size} trading days"
                } else {
                    "Simulated 30-day trend for $symbol"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun generateDynamicChartData(symbol: String, stock: com.stocktrading.app.data.models.Stock): List<Float> {
    val currentPrice = stock.price.toFloatOrNull() ?: 100f
    val changeValue = stock.changePercent
        .replace("%", "")
        .replace("+", "")
        .toFloatOrNull() ?: 0f

    val seed = symbol.hashCode().toLong()
    val random = kotlin.random.Random(seed)

    val dataPoints = mutableListOf<Float>()

    val isGainer = stock.changePercent.contains("+") || (changeValue > 0 && !stock.changePercent.contains("-"))

    val basePrice = if (currentPrice > 1f) currentPrice else 100f

    val totalChangeAmount = basePrice * (kotlin.math.abs(changeValue) / 100f)
    
    val startingPrice = if (isGainer) {
        basePrice - totalChangeAmount
    } else {
        basePrice + totalChangeAmount
    }

    for (i in 0 until 30) {
        val dayProgress = i.toFloat() / 29f

        val trendComponent = if (isGainer) {
            totalChangeAmount * dayProgress
        } else {
            -totalChangeAmount * dayProgress
        }

        val volatility = basePrice * 0.008f
        val randomComponent = kotlin.math.sin(i.toFloat() * 0.2f + (seed % 100).toFloat() * 0.1f) * volatility

        val price = startingPrice + trendComponent + randomComponent

        dataPoints.add(kotlin.math.max(price, 0.01f))
    }

    if (dataPoints.size >= 2) {
        val firstPrice = dataPoints.first()
        val lastPrice = dataPoints.last()
        
        if (isGainer && lastPrice <= firstPrice) {
            for (j in dataPoints.indices) {
                val progress = j.toFloat() / (dataPoints.size - 1)
                dataPoints[j] = firstPrice + (basePrice - firstPrice) * progress
            }
        } else if (!isGainer && lastPrice >= firstPrice) {
            for (j in dataPoints.indices) {
                val progress = j.toFloat() / (dataPoints.size - 1)
                dataPoints[j] = firstPrice - (firstPrice - basePrice) * progress
            }
        }
    }

    return dataPoints
}

@Composable
private fun StatsCard(stock: com.stocktrading.app.data.models.Stock) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Key Statistics",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatRow("Volume", stock.volume.ifEmpty { "N/A" })
                StatRow("Symbol", stock.symbol)
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}