package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ApplicationStatus
import com.example.data.MatchResult
import com.example.data.OpportunityCardItem
import com.example.data.OpportunityCategory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OpportunityCard(
    item: OpportunityCardItem,
    selected: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    onMarkApplied: () -> Unit,
    modifier: Modifier = Modifier
) {
    val opp = item.opportunity
    val isApplied = item.status == ApplicationStatus.APPLIED ||
        item.status == ApplicationStatus.INTERVIEW ||
        item.status == ApplicationStatus.ACCEPTED

    val cardBorder = if (selected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag("opportunity_card_${opp.id}"),
        shape = RoundedCornerShape(20.dp),
        border = cardBorder,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top badges: category + verified + match pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    CategoryBadge(category = opp.category)
                    VerificationBadge(verified = opp.verified)
                }

                MatchScoreBadge(score = item.match.scorePercent)
            }

            // Title & org
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = opp.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${opp.organization} · ${opp.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Deadline & stipend row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DeadlinePill(
                    deadlineText = opp.deadlineText,
                    daysRemaining = item.daysRemaining
                )
                if (opp.stipendOrValue.isNotBlank() && opp.stipendOrValue != "Not specified") {
                    Text(
                        text = opp.stipendOrValue,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Visual skill alignment progress bar + percentage
            SkillAlignmentIndicator(
                opportunityId = opp.id,
                match = item.match
            )

            // Matched vs missing skill chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item.match.matchedSkills.take(3).forEach { skill ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "✓ $skill",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                item.match.missingSkills.take(2).forEach { skill ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = skill,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Quick actions: Save + Mark Applied / Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("save_button_${opp.id}")
                    ) {
                        Icon(
                            imageVector = if (item.saved) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (item.saved) "Remove bookmark" else "Save opportunity",
                            tint = if (item.saved) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (item.status != ApplicationStatus.NONE) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isApplied) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            }
                        ) {
                            Text(
                                text = item.status.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isApplied) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                if (isApplied) {
                    OutlinedButton(
                        onClick = onClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("view_applied_${opp.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Applied")
                    }
                } else {
                    Button(
                        onClick = onMarkApplied,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("mark_applied_${opp.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Mark Applied")
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(category: OpportunityCategory) {
    val (bg, fg) = when (category) {
        OpportunityCategory.INTERNSHIP ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        OpportunityCategory.SCHOLARSHIP ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        OpportunityCategory.FELLOWSHIP ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Text(
            text = category.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun VerificationBadge(verified: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (verified) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        } else {
            MaterialTheme.colorScheme.tertiaryContainer
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (verified) Icons.Rounded.Verified else Icons.Outlined.ReportProblem,
                contentDescription = if (verified) "Verified opportunity" else "Unverified forward",
                tint = if (verified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = if (verified) "Verified" else "Imported",
                style = MaterialTheme.typography.labelSmall,
                color = if (verified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
fun MatchScoreBadge(score: Int) {
    val container = when {
        score >= 78 -> MaterialTheme.colorScheme.primary
        score >= 58 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }
    val content = when {
        score >= 78 -> MaterialTheme.colorScheme.onPrimary
        score >= 58 -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = RoundedCornerShape(10.dp), color = container) {
        Text(
            text = "$score% match",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = content,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SkillAlignmentIndicator(
    opportunityId: String,
    match: MatchResult,
    modifier: Modifier = Modifier
) {
    val totalRequired = match.matchedSkills.size + match.missingSkills.size
    val alignmentPercent = match.skillAlignmentPercent.coerceIn(0, 100)
    val targetFraction = (alignmentPercent / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 350f),
        label = "skill_alignment_progress"
    )

    val barColor = when {
        alignmentPercent >= 70 -> MaterialTheme.colorScheme.primary
        alignmentPercent >= 40 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

    val ratioText = if (totalRequired > 0) {
        "${match.matchedSkills.size} of $totalRequired skills matched"
    } else {
        "Profile & field alignment"
    }
    val accessibilityText = "Skill alignment $alignmentPercent percent, $ratioText"

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityText
            }
            .testTag("skill_alignment_bar_$opportunityId")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ratioText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$alignmentPercent%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = barColor,
                    modifier = Modifier.testTag("skill_alignment_percent_$opportunityId")
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(trackColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.coerceAtLeast(0.04f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(barColor)
                )
            }
        }
    }
}

@Composable
fun DeadlinePill(deadlineText: String, daysRemaining: Long?) {
    val urgent = daysRemaining != null && daysRemaining in 0..14
    val expired = daysRemaining != null && daysRemaining < 0
    val bg = when {
        expired -> MaterialTheme.colorScheme.errorContainer
        urgent -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val fg = when {
        expired -> MaterialTheme.colorScheme.onErrorContainer
        urgent -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when {
        daysRemaining == null -> deadlineText
        daysRemaining < 0 -> "Closed · $deadlineText"
        daysRemaining == 0L -> "Closes today"
        daysRemaining <= 14L -> "${daysRemaining}d left · $deadlineText"
        else -> deadlineText
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = fg
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OpportunityDetailContent(
    item: OpportunityCardItem,
    onClose: () -> Unit,
    onToggleSave: () -> Unit,
    onUpdateStatus: (ApplicationStatus, String) -> Unit,
    onDeleteImported: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val opp = item.opportunity
    var notesDraft by remember(item.opportunity.id, item.notes) { mutableStateOf(item.notes) }
    var selectedStatus by remember(item.opportunity.id, item.status) { mutableStateOf(item.status) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CategoryBadge(opp.category)
                    VerificationBadge(opp.verified)
                    MatchScoreBadge(item.match.scorePercent)
                }
                Text(
                    text = opp.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = opp.organization,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("close_detail_button")
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Close details")
            }
        }

        // Warnings if imported / unverified
        if (!opp.verified && opp.verificationWarnings.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ReportProblem,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Safety & Verification Checks",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    opp.verificationWarnings.forEach { warning ->
                        Text(
                            text = "• $warning",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }

        // Metadata grid
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailInfoRow(Icons.Outlined.LocationOn, "Location", opp.location)
                DetailInfoRow(Icons.Outlined.CalendarMonth, "Deadline", opp.deadlineText)
                DetailInfoRow(Icons.Outlined.Payments, "Stipend / Value", opp.stipendOrValue)
                DetailInfoRow(Icons.Rounded.Verified, "Source", opp.verifiedSource)
            }
        }

        if (opp.summary.isNotBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Overview", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = opp.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Skill match breakdown
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Skill Fit · ${item.match.summaryLabel} (${item.match.scorePercent}% overall)",
                style = MaterialTheme.typography.titleMedium
            )
            SkillAlignmentIndicator(
                opportunityId = "${opp.id}_detail",
                match = item.match
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item.match.matchedSkills.forEach { skill ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "✓ $skill",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
                item.match.missingSkills.forEach { skill ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = "Build: $skill",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // Eligibility & requirements
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Eligibility", style = MaterialTheme.typography.titleMedium)
            Text(
                text = opp.eligibility,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (opp.requirements.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Application Requirements", style = MaterialTheme.typography.titleMedium)
                opp.requirements.forEach { req ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = req,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Application tracker pipeline
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Application Tracker", style = MaterialTheme.typography.titleMedium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    ApplicationStatus.SAVED,
                    ApplicationStatus.IN_PROGRESS,
                    ApplicationStatus.APPLIED,
                    ApplicationStatus.INTERVIEW,
                    ApplicationStatus.ACCEPTED
                ).forEach { statusOption ->
                    FilterChip(
                        selected = selectedStatus == statusOption,
                        onClick = {
                            selectedStatus = statusOption
                            onUpdateStatus(statusOption, notesDraft)
                        },
                        label = { Text(statusOption.label) },
                        modifier = Modifier.testTag("status_chip_${statusOption.name}")
                    )
                }
            }

            OutlinedTextField(
                value = notesDraft,
                onValueChange = { notesDraft = it.take(1000) },
                label = { Text("Personal application notes / checklist") },
                placeholder = { Text("e.g., Request intro letter from HoD, polish GitHub README...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("application_notes_input"),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onToggleSave,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("detail_save_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = if (item.saved) Icons.Rounded.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (item.saved) "Saved" else "Save")
                }

                Button(
                    onClick = { onUpdateStatus(selectedStatus.takeIf { it != ApplicationStatus.NONE } ?: ApplicationStatus.SAVED, notesDraft) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("detail_save_notes_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save Tracker")
                }
            }
        }

        // Official URL launcher (only if a real URL exists)
        if (opp.officialUrl.startsWith("http://") || opp.officialUrl.startsWith("https://")) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(opp.officialUrl))
                    runCatching { context.startActivity(intent) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("open_official_link_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Open Official Application Link")
            }
        }

        if (!opp.verified && onDeleteImported != null) {
            OutlinedButton(
                onClick = onDeleteImported,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("delete_imported_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Delete Imported Card")
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DetailInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
