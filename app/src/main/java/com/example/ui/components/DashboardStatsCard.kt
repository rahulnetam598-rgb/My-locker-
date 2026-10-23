package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CategoryEducationTeal
import com.example.ui.theme.CategoryIdentityBlue
import com.example.ui.theme.CategoryOtherIndigo
import com.example.ui.theme.LockerBlueDark
import com.example.ui.theme.LockerBluePrimary

@Composable
fun DashboardStatsCard(
    totalCount: Int,
    identityCount: Int,
    educationCount: Int,
    otherCount: Int,
    totalStorageBytes: Long,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            LockerBluePrimary,
            LockerBlueDark
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_stats_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LockerBluePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(gradientBrush)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFFA8C7FA),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DIGITAL DOCUMENT VAULT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFFA8C7FA)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (totalCount == 1) "1 Stored Document" else "$totalCount Stored Documents",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }

                    // On-Device badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Local Storage",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Category Count Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryCountPill(
                        categoryName = "Identity",
                        count = identityCount,
                        icon = Icons.Default.Badge,
                        isSelected = selectedCategory == "IDENTITY",
                        accentColor = CategoryIdentityBlue,
                        onClick = {
                            if (selectedCategory == "IDENTITY") onCategorySelected(null)
                            else onCategorySelected("IDENTITY")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    CategoryCountPill(
                        categoryName = "Education",
                        count = educationCount,
                        icon = Icons.Default.School,
                        isSelected = selectedCategory == "EDUCATION",
                        accentColor = CategoryEducationTeal,
                        onClick = {
                            if (selectedCategory == "EDUCATION") onCategorySelected(null)
                            else onCategorySelected("EDUCATION")
                        },
                        modifier = Modifier.weight(1f)
                    )

                    CategoryCountPill(
                        categoryName = "Other",
                        count = otherCount,
                        icon = Icons.Default.Folder,
                        isSelected = selectedCategory == "OTHER",
                        accentColor = CategoryOtherIndigo,
                        onClick = {
                            if (selectedCategory == "OTHER") onCategorySelected(null)
                            else onCategorySelected("OTHER")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCountPill(
    categoryName: String,
    count: Int,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgModifier = if (isSelected) {
        Modifier
            .background(Color.White)
            .border(2.dp, Color(0xFFA8C7FA), RoundedCornerShape(14.dp))
    } else {
        Modifier.background(Color.White.copy(alpha = 0.16f))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .then(bgModifier)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) accentColor else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isSelected) Color(0xFF0F172A) else Color.White
            )

            Text(
                text = categoryName,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = if (isSelected) Color(0xFF475569) else Color.White.copy(alpha = 0.85f)
            )
        }
    }
}
