package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HelloWorldScreen()
            }
        }
    }
}

enum class GreetingTheme(
    val themeName: String,
    val greetingText: String,
    val subtitleText: String,
    val backgroundColor: Color,
    val accentColor: Color,
    val cardBgColor: Color,
    val textColor: Color,
    val subtitleColor: Color,
    val textStyle: FontFamily,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    VIBRANT_BLUE(
        themeName = "Vibrant Blue",
        greetingText = "Hello World!",
        subtitleText = "Welcome to your new digital experience.",
        backgroundColor = Color(0xFFFDFCFF),
        accentColor = Color(0xFF0061A4),
        cardBgColor = Color(0xFFD1E4FF),
        textColor = Color(0xFF001D36),
        subtitleColor = Color(0xFF00497E),
        textStyle = FontFamily.Serif,
        icon = Icons.Rounded.Public
    ),
    SUNSET_PINK(
        themeName = "Sunset Pink",
        greetingText = "Bonjour le Monde!",
        subtitleText = "Unleash your creative inner spark.",
        backgroundColor = Color(0xFFFFFBFF),
        accentColor = Color(0xFF904169),
        cardBgColor = Color(0xFFFFD9E2),
        textColor = Color(0xFF3D0023),
        subtitleColor = Color(0xFF63063C),
        textStyle = FontFamily.SansSerif,
        icon = Icons.Rounded.WbSunny
    ),
    LAVENDER_DREAM(
        themeName = "Lavender Dream",
        greetingText = "System.out.print",
        subtitleText = "Where code meets beautiful design.",
        backgroundColor = Color(0xFFFAF8FF),
        accentColor = Color(0xFF6750A4),
        cardBgColor = Color(0xFFE8DEF8),
        textColor = Color(0xFF21005D),
        subtitleColor = Color(0xFF4F378B),
        textStyle = FontFamily.Monospace,
        icon = Icons.Rounded.Terminal
    ),
    MINT_FRESH(
        themeName = "Mint Fresh",
        greetingText = "Hola Mundo!",
        subtitleText = "Breathe in a wave of fresh energy.",
        backgroundColor = Color(0xFFF4FBF7),
        accentColor = Color(0xFF006B54),
        cardBgColor = Color(0xFFA2F2D4),
        textColor = Color(0xFF002017),
        subtitleColor = Color(0xFF00513E),
        textStyle = FontFamily.SansSerif,
        icon = Icons.Rounded.Coffee
    )
}

data class FloatingParticle(
    val id: Long,
    val startXDp: Float, // horizontal starting position in Dp
    val startYDp: Float, // vertical starting position in Dp
    val text: String,
    val size: Float,     // sp size of text
    val creationTime: Long,
    val wiggleSpeed: Float,
    val wiggleScale: Float,
    val driftY: Float    // distance to float upwards
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HelloWorldScreen() {
    var nameInput by remember { mutableStateOf("World") }
    var selectedTheme by remember { mutableStateOf(GreetingTheme.VIBRANT_BLUE) }
    
    // Tap wave animation states
    var pulseProgress by remember { mutableStateOf(0f) }
    var pulseCenter by remember { mutableStateOf(Offset.Zero) }
    val pulseAnim = remember { Animatable(0f) }
    
    // Interactive Stats states
    var streakCount by remember { mutableStateOf(12) }
    var pointsCount by remember { mutableStateOf(2400) }
    
    // Floating emoji collection
    val floatingParticles = remember { mutableStateListOf<FloatingParticle>() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current

    // Infinite heartbeat animation to trigger periodic recomposition for particles
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val ticker = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ticker"
    )

    // Filter out old floating emojis (alive for 2.0s)
    LaunchedEffect(ticker.value) {
        val now = System.currentTimeMillis()
        val toRemove = floatingParticles.filter { now - it.creationTime > 2000 }
        floatingParticles.removeAll(toRemove)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(selectedTheme.backgroundColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val xDp = with(density) { offset.x.toDp().value }
                        val yDp = with(density) { offset.y.toDp().value }
                        pulseCenter = offset
                        floatingParticles.add(
                            FloatingParticle(
                                id = Random.nextLong(),
                                startXDp = xDp,
                                startYDp = yDp,
                                text = listOf("✨", "💫", "⭐", "🌍", "🪐").random(),
                                size = Random.nextFloat() * 12f + 16f,
                                creationTime = System.currentTimeMillis(),
                                wiggleSpeed = Random.nextFloat() * 3f + 1f,
                                wiggleScale = Random.nextFloat() * 20f + 10f,
                                driftY = 350f
                            )
                        )
                        // Trigger pulse wave
                        pulseProgress = 0.01f
                    }
                )
            }
    ) {
        // Stepwise trigger animation when pulseProgress starts
        LaunchedEffect(pulseProgress) {
            if (pulseProgress > 0f) {
                pulseAnim.snapTo(0f)
                pulseAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1000, easing = LinearOutSlowInEasing)
                )
                pulseProgress = 0f
            }
        }

        // Beautiful background visualizer
        BackgroundVisualizer(
            theme = selectedTheme,
            pulseProgress = if (pulseAnim.value > 0f && pulseAnim.value < 1f) pulseAnim.value else 0f,
            pulseCenter = pulseCenter
        )

        // Floating Particles layer
        val now = System.currentTimeMillis()
        floatingParticles.forEach { particle ->
            val elapsed = now - particle.creationTime
            val fraction = (elapsed / 2000f).coerceIn(0f, 1f)
            
            // Calculate float upwards and sway from side to side
            val verticalY = particle.startYDp - (fraction * particle.driftY)
            val wiggle = kotlin.math.sin(fraction * Math.PI * 2 * particle.wiggleSpeed).toFloat() * particle.wiggleScale
            val xPosition = particle.startXDp + wiggle
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.TopStart)
            ) {
                Box(
                    modifier = Modifier
                        .offset(
                            x = xPosition.dp,
                            y = verticalY.dp
                        )
                        .scale(1f - fraction * 0.3f)
                ) {
                    Text(
                        text = particle.text,
                        fontSize = particle.size.sp,
                        color = Color.Unspecified,
                        modifier = Modifier.blur((1.dp * (fraction * 2f)).coerceAtLeast(0.1.dp))
                    )
                }
            }
        }

        // Foreground content with full edge-to-edge layout
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                
                // 1. M3 Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left active-ripple hamburger icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    // Spawns a small sparkle from the app bar
                                    floatingParticles.add(
                                        FloatingParticle(
                                            id = Random.nextLong(),
                                            startXDp = 40f,
                                            startYDp = 40f,
                                            text = "✨",
                                            size = 20f,
                                            creationTime = System.currentTimeMillis(),
                                            wiggleSpeed = 2f,
                                            wiggleScale = 15f,
                                            driftY = 200f
                                        )
                                    )
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Menu",
                                tint = selectedTheme.textColor
                            )
                        }
                        
                        Text(
                            text = "Hello World",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = selectedTheme.textColor
                        )
                    }

                    // Right User Initial Circle Avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(selectedTheme.accentColor)
                            .clickable {
                                // Greet user specifically
                                nameInput = "Explorer"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "S",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Scrollable main body to fit everything elegantly on all phone sizes
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // 2. Central Hero Greeting Card (Material 3 Container)
                    Greeting(
                        name = nameInput.ifBlank { "World" },
                        selectedTheme = selectedTheme,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.1f, fill = false)
                            .testTag("greeting_card")
                    )

                    // 3. Quick Stats Grid (2 columns)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Card 1: Daily Streak (Warm pink)
                        Card(
                            onClick = {
                                streakCount += 1
                                repeat(5) { i ->
                                    floatingParticles.add(
                                        FloatingParticle(
                                            id = Random.nextLong(),
                                            startXDp = 80f + Random.nextFloat() * 40f,
                                            startYDp = 320f + Random.nextFloat() * 40f,
                                            text = listOf("🔥", "⚡", "✨").random(),
                                            size = Random.nextFloat() * 10f + 14f,
                                            creationTime = System.currentTimeMillis() + i * 50,
                                            wiggleSpeed = 3f,
                                            wiggleScale = 20f,
                                            driftY = 250f
                                        )
                                    )
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFD9E2)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF904169)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = "Streak Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "DAILY STREAK",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp,
                                        color = Color(0xFF3D0023)
                                    )
                                    Text(
                                        text = "$streakCount Days",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF3D0023)
                                    )
                                }
                            }
                        }

                        // Card 2: Points (Lavender purple)
                        Card(
                            onClick = {
                                pointsCount += 100
                                repeat(5) { i ->
                                    floatingParticles.add(
                                        FloatingParticle(
                                            id = Random.nextLong(),
                                            startXDp = 240f + Random.nextFloat() * 40f,
                                            startYDp = 320f + Random.nextFloat() * 40f,
                                            text = listOf("⭐", "💎", "✨").random(),
                                            size = Random.nextFloat() * 10f + 14f,
                                            creationTime = System.currentTimeMillis() + i * 50,
                                            wiggleSpeed = 2.5f,
                                            wiggleScale = 18f,
                                            driftY = 250f
                                        )
                                    )
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8DEF8)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF6750A4)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Favorite,
                                        contentDescription = "Points Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "POINTS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp,
                                        color = Color(0xFF21005D)
                                    )
                                    Text(
                                        text = "${(pointsCount / 1000f)}k",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF21005D)
                                    )
                                }
                            }
                        }
                    }

                    // 4. Interactive Control Panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .shadow(2.dp, RoundedCornerShape(24.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Customizable Name Input
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { if (it.length <= 20) nameInput = it },
                            label = { Text("Customize Greeting Name") },
                            placeholder = { Text("Enter a name...") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = selectedTheme.accentColor,
                                unfocusedBorderColor = selectedTheme.textColor.copy(alpha = 0.2f),
                                focusedLabelColor = selectedTheme.accentColor,
                                unfocusedLabelColor = selectedTheme.textColor.copy(alpha = 0.5f),
                                focusedTextColor = selectedTheme.textColor,
                                unfocusedTextColor = selectedTheme.textColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input"),
                            trailingIcon = {
                                if (nameInput.isNotEmpty()) {
                                    IconButton(onClick = { nameInput = "" }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Clear",
                                            tint = selectedTheme.textColor.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Presets Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            listOf("World", "Explorer", "Friend", "Developer").forEach { preset ->
                                val isSelected = nameInput == preset
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) selectedTheme.accentColor.copy(alpha = 0.15f)
                                            else Color(0xFFF1F2F6)
                                        )
                                        .clickable {
                                            nameInput = preset
                                            focusManager.clearFocus()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = preset,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) selectedTheme.accentColor else selectedTheme.textColor.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Theme selector label
                        Text(
                            text = "SELECT THEME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = selectedTheme.textColor.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Theme Selection Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(GreetingTheme.values()) { theme ->
                                val isSelected = selectedTheme == theme
                                Card(
                                    onClick = { selectedTheme = theme },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) theme.accentColor.copy(alpha = 0.12f)
                                        else Color(0xFFF5F6FA)
                                    ),
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = if (isSelected) theme.accentColor else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .width(135.dp)
                                        .height(48.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = theme.icon,
                                            contentDescription = theme.themeName,
                                            tint = if (isSelected) theme.accentColor else theme.textColor.copy(alpha = 0.5f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = theme.themeName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) theme.accentColor else theme.textColor.copy(alpha = 0.7f),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Big Action Button: Appreciate the World (Triggers heart explosion!)
                        Button(
                            onClick = {
                                val heartEmojis = listOf("❤️", "💖", "💙", "💚", "🌟", "🔥", "🦄", "🌈")
                                repeat(12) { i ->
                                    floatingParticles.add(
                                        FloatingParticle(
                                            id = Random.nextLong(),
                                            startXDp = 80f + Random.nextFloat() * 200f,
                                            startYDp = 450f + Random.nextFloat() * 60f,
                                            text = heartEmojis.random(),
                                            size = Random.nextFloat() * 12f + 18f,
                                            creationTime = System.currentTimeMillis() + i * 40,
                                            wiggleSpeed = Random.nextFloat() * 3f + 1.5f,
                                            wiggleScale = Random.nextFloat() * 25f + 12f,
                                            driftY = 400f
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = selectedTheme.accentColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("appreciate_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = "Heart"
                                )
                                Text(
                                    text = "Appreciate the World",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // 5. Bottom Navigation & Floating Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                ) {
                    // Actual Navigation Bar matching M3 from HTML
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .align(Alignment.BottomCenter)
                            .background(Color(0xFFF3F4F9))
                            .drawBehind {
                                // Subtle top border
                                drawLine(
                                    color = Color(0xFFE1E2E6),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Home tab (Active)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clickable {
                                    // Spawns sparkles on click
                                    repeat(3) {
                                        floatingParticles.add(
                                            FloatingParticle(
                                                id = Random.nextLong(),
                                                startXDp = 60f + Random.nextFloat() * 20f,
                                                startYDp = 600f,
                                                text = "💫",
                                                size = 14f,
                                                creationTime = System.currentTimeMillis(),
                                                wiggleSpeed = 2f,
                                                wiggleScale = 10f,
                                                driftY = 150f
                                            )
                                        )
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(selectedTheme.cardBgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Home,
                                    contentDescription = "Home",
                                    tint = selectedTheme.textColor
                                )
                            }
                            Text(
                                text = "Home",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1C1E)
                            )
                        }

                        // Favs tab (Inactive)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clickable {
                                    // Make points jump up
                                    pointsCount += 50
                                    floatingParticles.add(
                                        FloatingParticle(
                                            id = Random.nextLong(),
                                            startXDp = 180f,
                                            startYDp = 600f,
                                            text = "💖",
                                            size = 18f,
                                            creationTime = System.currentTimeMillis(),
                                            wiggleSpeed = 3f,
                                            wiggleScale = 12f,
                                            driftY = 200f
                                        )
                                    )
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FavoriteBorder,
                                    contentDescription = "Favs",
                                    tint = Color(0xFF44474E)
                                )
                            }
                            Text(
                                text = "Favs",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF44474E)
                            )
                        }

                        // Profile tab (Inactive)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clickable {
                                    nameInput = "Jane Doe"
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PersonOutline,
                                    contentDescription = "Profile",
                                    tint = Color(0xFF44474E)
                                )
                            }
                            Text(
                                text = "Profile",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF44474E)
                            )
                        }

                        // Placeholder spacer to account for the overlapping FAB
                        Spacer(modifier = Modifier.width(60.dp))
                    }

                    // Floating Action Button overlapping precisely on top right (absolute top-7 right-6)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-24).dp, y = (-8).dp)
                    ) {
                        Button(
                            onClick = {
                                // Spawns an almighty rainbow confetti splash!
                                val colors = listOf("🎉", "✨", "🔥", "🌈", "⭐", "🍀", "🍩", "🌸")
                                repeat(16) { i ->
                                    floatingParticles.add(
                                        FloatingParticle(
                                            id = Random.nextLong(),
                                            startXDp = 300f + Random.nextFloat() * 40f,
                                            startYDp = 580f + Random.nextFloat() * 30f,
                                            text = colors.random(),
                                            size = Random.nextFloat() * 12f + 16f,
                                            creationTime = System.currentTimeMillis() + i * 30,
                                            wiggleSpeed = Random.nextFloat() * 4f + 2f,
                                            wiggleScale = Random.nextFloat() * 35f - 15f,
                                            driftY = 450f
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = selectedTheme.accentColor
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .testTag("fab_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
    selectedTheme: GreetingTheme = GreetingTheme.VIBRANT_BLUE
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = selectedTheme.cardBgColor
        ),
        border = BorderStroke(
            width = 1.dp,
            color = selectedTheme.accentColor.copy(alpha = 0.2f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            
            // Decorative background circles drawn directly to mimic the custom CSS circles
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                
                // Absolute circle 1: Top Right
                drawCircle(
                    color = selectedTheme.accentColor.copy(alpha = 0.08f),
                    radius = 90.dp.toPx(),
                    center = Offset(width - 20.dp.toPx(), 20.dp.toPx())
                )
                
                // Absolute circle 2: Bottom Left
                drawCircle(
                    color = selectedTheme.accentColor.copy(alpha = 0.08f),
                    radius = 70.dp.toPx(),
                    center = Offset(20.dp.toPx(), height - 20.dp.toPx())
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large Accent colored Circle holding white icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(selectedTheme.accentColor)
                        .shadow(4.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = selectedTheme.icon,
                        contentDescription = "Theme Icon",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Greeting Bold Lead
                Text(
                    text = "${selectedTheme.greetingText.substringBefore(" ")} World!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = selectedTheme.textStyle,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = selectedTheme.textColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Custom Personalized User Name
                Text(
                    text = "Hello, $name!",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = selectedTheme.textStyle,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    ),
                    color = selectedTheme.textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Supportive subtitle description
                Text(
                    text = selectedTheme.subtitleText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = selectedTheme.textStyle,
                        letterSpacing = 0.5.sp
                    ),
                    color = selectedTheme.subtitleColor.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun BackgroundVisualizer(
    theme: GreetingTheme,
    pulseProgress: Float,
    pulseCenter: Offset
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg_anim")
    
    // Slow orbit rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Wave pulse oscillation
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)

        // Draw Interactive Pulse Expansion Ring (anywhere on tap)
        if (pulseProgress > 0f && pulseProgress < 1f) {
            val maxRadius = width.coerceAtLeast(height) * 1.3f
            val currentRadius = maxRadius * pulseProgress
            val opacity = (1f - pulseProgress).coerceIn(0f, 1f)
            
            drawCircle(
                color = theme.accentColor.copy(alpha = opacity * 0.25f),
                radius = currentRadius,
                center = pulseCenter,
                style = Stroke(width = (3.dp * (1f + pulseProgress * 1.5f)).toPx())
            )
        }
    }
}

// Modifiers are cleanly imported and natively utilized
