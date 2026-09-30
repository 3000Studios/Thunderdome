package com.example.ui.components

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.PlayerProfileEntity
import com.example.game.monetization.BillingManager
import com.example.game.monetization.ProductDetailItem
import com.example.ui.theme.*

@Composable
fun StoreModal(
    profile: PlayerProfileEntity,
    availableProducts: Map<String, ProductDetailItem>,
    onBuyProduct: (activity: Activity, productId: String) -> Unit,
    onRestorePurchases: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.5.dp, DarkSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "3000 STUDIOS // HANGAR DEPOT",
                                style = MaterialTheme.typography.labelSmall,
                                color = AeroCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "SUPPLY & WARBIRD STORE",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // 1. FEATURED: 3000 STUDIOS FOUNDER PACK
                        val founderProd = availableProducts[BillingManager.PRODUCT_FOUNDER_PACK]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1304)),
                            border = BorderStroke(2.dp, Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFF59E0B), Color(0xFFFFE082))))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = Color(0xFFFFD700),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "👑 ULTIMATE FOUNDER",
                                            color = Color.Black,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Text(
                                        text = founderProd?.formattedPrice ?: "$9.99",
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "3000 STUDIOS FOUNDER PACK",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "• Exclusive Gold Apex Zero Warbird\n• Founder Gold Paint & Solar Ion Trail\n• +50,000 Credits & +100 Plasma Cores\n• Permanent Ad Removal & Founder Badge",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE2E8F0),
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { activity?.let { onBuyProduct(it, BillingManager.PRODUCT_FOUNDER_PACK) } },
                                    modifier = Modifier.fillMaxWidth().height(46.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (profile.hasFounderPack) AeroEmerald else Color(0xFFFFD700),
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !profile.hasFounderPack
                                ) {
                                    Text(
                                        text = if (profile.hasFounderPack) "FOUNDER UNLOCKED ✓" else "GET FOUNDER PACK (${founderProd?.formattedPrice ?: "$9.99"})",
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        // 2. STARTER PACK
                        val starterProd = availableProducts[BillingManager.PRODUCT_STARTER_PACK]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            border = BorderStroke(1.dp, AeroViolet.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("STARTER PILOT PACK", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                    Text("BlazeHound interceptor + 15k Credits + 30 Cores", color = TextSecondary, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { activity?.let { onBuyProduct(it, BillingManager.PRODUCT_STARTER_PACK) } },
                                    colors = ButtonDefaults.buttonColors(containerColor = AeroViolet),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !profile.hasStarterPack
                                ) {
                                    Text(
                                        text = if (profile.hasStarterPack) "OWNED ✓" else (starterProd?.formattedPrice ?: "$4.99"),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // 3. REMOVE ADS
                        val removeAdsProd = availableProducts[BillingManager.PRODUCT_REMOVE_ADS]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            border = BorderStroke(1.dp, AeroCyan.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("REMOVE ALL ADS", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                    Text("Disables interstitials & gives instant sortie continues", color = TextSecondary, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { activity?.let { onBuyProduct(it, BillingManager.PRODUCT_REMOVE_ADS) } },
                                    colors = ButtonDefaults.buttonColors(containerColor = AeroCyan, contentColor = DarkVoid),
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = !profile.isAdsRemoved && !profile.hasFounderPack
                                ) {
                                    Text(
                                        text = if (profile.isAdsRemoved || profile.hasFounderPack) "ACTIVE ✓" else (removeAdsProd?.formattedPrice ?: "$2.99"),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // 4. PLASMA CORE DROPS
                        Text("PLASMA CORE SUPPLY CRATES", style = MaterialTheme.typography.labelLarge, color = Color.White)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val smallCores = availableProducts[BillingManager.PRODUCT_CORES_SMALL]
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                                border = BorderStroke(1.dp, DarkSurfaceBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("25 CORES", fontWeight = FontWeight.Bold, color = AeroViolet, fontSize = 14.sp)
                                    Text("Overclocking crate", color = TextSecondary, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { activity?.let { onBuyProduct(it, BillingManager.PRODUCT_CORES_SMALL) } },
                                        colors = ButtonDefaults.buttonColors(containerColor = AeroViolet),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(smallCores?.formattedPrice ?: "$0.99", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            val largeCores = availableProducts[BillingManager.PRODUCT_CORES_LARGE]
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                                border = BorderStroke(1.dp, DarkSurfaceBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("150 CORES", fontWeight = FontWeight.Bold, color = AeroEmerald, fontSize = 14.sp)
                                    Text("Fleet crate (Best Value)", color = TextSecondary, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { activity?.let { onBuyProduct(it, BillingManager.PRODUCT_CORES_LARGE) } },
                                        colors = ButtonDefaults.buttonColors(containerColor = AeroEmerald, contentColor = DarkVoid),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(largeCores?.formattedPrice ?: "$4.99", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Restore Purchases & Footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onRestorePurchases) {
                            Text("RESTORE PURCHASES", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "SECURED BY GOOGLE PLAY",
                            color = TextMuted,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}
