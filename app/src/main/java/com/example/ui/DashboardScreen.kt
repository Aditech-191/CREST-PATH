package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ApplicationStatus
import com.example.data.OpportunityCardItem
import com.example.data.OpportunityCategory
import com.example.data.OpportunitySort
import com.example.data.UgandaRegion
import com.example.data.VerifiedCatalog
import com.example.viewmodel.HubUiData
import com.example.viewmodel.TrackerFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    data: HubUiData,
    selectedOpportunityId: String?,
    onSelectOpportunity: (String?) -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (OpportunityCategory?) -> Unit,
    onSelectRegion: (UgandaRegion) -> Unit,
    onSelectCity: (String?) -> Unit,
    onClearLocationFilter: () -> Unit,
    onClearAllFilters: () -> Unit,
    onSelectTrackerFilter: (TrackerFilter) -> Unit,
    onSelectSort: (OpportunitySort) -> Unit,
    onToggleSave: (OpportunityCardItem) -> Unit,
    onUpdateStatus: (OpportunityCardItem, ApplicationStatus, String) -> Unit,
    onDeleteImported: (String) -> Unit,
    onSwitchDemoProfile: (String) -> Unit,
    onOpenParser: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedItem = remember(data.allItems, selectedOpportunityId) {
        data.allItems.firstOrNull { it.opportunity.id == selectedOpportunityId }
    }
    var showDemoSwitcherSheet by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isSplitPane = maxWidth >= 840.dp

        if (isSplitPane) {
            Row(modifier = Modifier.fillMaxSize()) {
                DashboardFeedColumn(
                    data = data,
                    selectedOpportunityId = selectedOpportunityId,
                    onSelectOpportunity = onSelectOpportunity,
                    onSearchChange = onSearchChange,
                    onSelectCategory = onSelectCategory,
                    onSelectRegion = onSelectRegion,
                    onSelectCity = onSelectCity,
                    onClearLocationFilter = onClearLocationFilter,
                    onClearAllFilters = onClearAllFilters,
                    onSelectTrackerFilter = onSelectTrackerFilter,
                    onSelectSort = onSelectSort,
                    onToggleSave = onToggleSave,
                    onQuickMarkApplied = { item ->
                        onUpdateStatus(item, ApplicationStatus.APPLIED, item.notes)
                    },
                    onOpenDemoSwitcher = { showDemoSwitcherSheet = true },
                    onOpenParser = onOpenParser,
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight()
                )

                Surface(
                    modifier = Modifier
                        .weight(0.85f)
                        .fillMaxHeight()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        ),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    val activeDetail = selectedItem ?: data.filteredItems.firstOrNull()
                    if (activeDetail != null) {
                        OpportunityDetailContent(
                            item = activeDetail,
                            onClose = { onSelectOpportunity(null) },
                            onToggleSave = { onToggleSave(activeDetail) },
                            onUpdateStatus = { st, notes -> onUpdateStatus(activeDetail, st, notes) },
                            onDeleteImported = if (!activeDetail.opportunity.verified) {
                                { onDeleteImported(activeDetail.opportunity.id) }
                            } else null
                        )
                    } else {
                        EmptyDetailPlaceholder()
                    }
                }
            }
        } else {
            DashboardFeedColumn(
                data = data,
                selectedOpportunityId = selectedOpportunityId,
                onSelectOpportunity = onSelectOpportunity,
                onSearchChange = onSearchChange,
                onSelectCategory = onSelectCategory,
                onSelectRegion = onSelectRegion,
                onSelectCity = onSelectCity,
                onClearLocationFilter = onClearLocationFilter,
                onClearAllFilters = onClearAllFilters,
                onSelectTrackerFilter = onSelectTrackerFilter,
                onSelectSort = onSelectSort,
                onToggleSave = onToggleSave,
                onQuickMarkApplied = { item ->
                    onUpdateStatus(item, ApplicationStatus.APPLIED, item.notes)
                },
                onOpenDemoSwitcher = { showDemoSwitcherSheet = true },
                onOpenParser = onOpenParser,
                modifier = Modifier.fillMaxSize()
            )

            if (selectedItem != null) {
                BackHandler { onSelectOpportunity(null) }
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { onSelectOpportunity(null) },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    OpportunityDetailContent(
                        item = selectedItem,
                        onClose = { onSelectOpportunity(null) },
                        onToggleSave = { onToggleSave(selectedItem) },
                        onUpdateStatus = { st, notes -> onUpdateStatus(selectedItem, st, notes) },
                        onDeleteImported = if (!selectedItem.opportunity.verified) {
                            { onDeleteImported(selectedItem.opportunity.id) }
                        } else null
                    )
                }
            }
        }
    }

    if (showDemoSwitcherSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDemoSwitcherSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            DemoProfileSwitcherSheetContent(
                activeKey = data.activeProfile.profileKey,
                personalName = data.personalProfile?.fullName?.ifBlank { "My Profile" } ?: "My Profile",
                personalSubtitle = "${data.personalProfile?.university ?: "Makerere University"} · ${data.personalProfile?.program ?: "BSc Computer Science"}",
                onSelectProfile = { key ->
                    showDemoSwitcherSheet = false
                    onSwitchDemoProfile(key)
                }
            )
        }
    }
}

@Composable
private fun DashboardFeedColumn(
    data: HubUiData,
    selectedOpportunityId: String?,
    onSelectOpportunity: (String?) -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (OpportunityCategory?) -> Unit,
    onSelectRegion: (UgandaRegion) -> Unit,
    onSelectCity: (String?) -> Unit,
    onClearLocationFilter: () -> Unit,
    onClearAllFilters: () -> Unit,
    onSelectTrackerFilter: (TrackerFilter) -> Unit,
    onSelectSort: (OpportunitySort) -> Unit,
    onToggleSave: (OpportunityCardItem) -> Unit,
    onQuickMarkApplied: (OpportunityCardItem) -> Unit,
    onOpenDemoSwitcher: () -> Unit,
    onOpenParser: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Editorial Hero Banner + Active Profile Switcher
        item(key = "hero_banner") {
            HeroBannerCard(
                data = data,
                onOpenDemoSwitcher = onOpenDemoSwitcher,
                onOpenParser = onOpenParser
            )
        }

        // Demo mode quick indicator bar
        item(key = "demo_banner") {
            AnimatedVisibility(visible = data.activeProfile.demoMode) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DEMO MODE · ${data.activeProfile.fullName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Match scores & AI Advisor reflect this student's skills.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        TextButton(
                            onClick = onOpenDemoSwitcher,
                            modifier = Modifier.testTag("switch_demo_banner_button")
                        ) {
                            Text("Switch")
                        }
                    }
                }
            }
        }

        // KPI Metric Strip
        item(key = "kpi_strip") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricMiniCard(
                    label = "Top Matches",
                    value = data.strongMatchCount.toString(),
                    icon = Icons.Outlined.Stars,
                    highlighted = false,
                    onClick = {
                        onSelectTrackerFilter(TrackerFilter.ALL)
                        onSelectSort(OpportunitySort.BEST_MATCH)
                    },
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    label = "Saved",
                    value = data.savedCount.toString(),
                    icon = Icons.Outlined.BookmarkBorder,
                    highlighted = data.filter.trackerFilter == TrackerFilter.SAVED,
                    onClick = { onSelectTrackerFilter(TrackerFilter.SAVED) },
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    label = "Applied",
                    value = data.appliedCount.toString(),
                    icon = Icons.Outlined.CheckCircle,
                    highlighted = data.filter.trackerFilter == TrackerFilter.APPLIED,
                    onClick = { onSelectTrackerFilter(TrackerFilter.APPLIED) },
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    label = "Closing Soon",
                    value = data.urgentCount.toString(),
                    icon = Icons.Outlined.AccessTime,
                    highlighted = data.filter.sort == OpportunitySort.DEADLINE_SOON,
                    onClick = {
                        onSelectTrackerFilter(TrackerFilter.ALL)
                        onSelectSort(OpportunitySort.DEADLINE_SOON)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search + Filter controls
        item(key = "search_filters") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = data.filter.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search MTN, Mastercard, Kotlin, AutoCAD, Kampala...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        if (data.filter.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_search_input")
                )

                // Tracker filter chips + Sort toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TrackerFilter.entries.forEach { tf ->
                        FilterChip(
                            selected = data.filter.trackerFilter == tf,
                            onClick = { onSelectTrackerFilter(tf) },
                            label = { Text(tf.label) },
                            modifier = Modifier.testTag("tracker_filter_${tf.name}")
                        )
                    }

                    AssistChip(
                        onClick = {
                            val entries = OpportunitySort.entries
                            val nextIndex = (entries.indexOf(data.filter.sort) + 1) % entries.size
                            onSelectSort(entries[nextIndex])
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        label = { Text("Sort: ${data.filter.sort.label}") },
                        modifier = Modifier.testTag("sort_toggle_chip")
                    )
                }

                // Category filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    OpportunityCategory.entries.forEach { category ->
                        FilterChip(
                            selected = data.filter.selectedCategory == category,
                            onClick = { onSelectCategory(category) },
                            label = { Text(category.label) },
                            modifier = Modifier.testTag("category_chip_${category.name}")
                        )
                    }
                }

                // Uganda Region & City filter component
                UgandaLocationFilterCard(
                    selectedRegion = data.filter.selectedRegion,
                    selectedCity = data.filter.selectedCity,
                    regionCounts = data.regionCounts,
                    cityCounts = data.cityCounts,
                    onSelectRegion = onSelectRegion,
                    onSelectCity = onSelectCity,
                    onClearLocation = onClearLocationFilter
                )
            }
        }

        // Section count header
        item(key = "results_header") {
            val locationSuffix = when {
                !data.filter.selectedCity.isNullOrBlank() -> " · ${data.filter.selectedCity}"
                data.filter.selectedRegion != UgandaRegion.ALL -> " · ${data.filter.selectedRegion.label}"
                else -> ""
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${data.filteredItems.size} Opportunities$locationSuffix",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Sorted by ${data.filter.sort.label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (data.filteredItems.isEmpty()) {
            item(key = "empty_list") {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No matching opportunities here",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Try clearing a region or category filter, switching student profile, or pasting a WhatsApp forward in the Parse tab.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (data.filter.hasActiveFilters) {
                            Spacer(Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = onClearAllFilters,
                                modifier = Modifier.testTag("clear_all_filters_button")
                            ) {
                                Text("Clear All Filters")
                            }
                        }
                    }
                }
            }
        } else {
            items(
                items = data.filteredItems,
                key = { it.opportunity.id }
            ) { item ->
                OpportunityCard(
                    item = item,
                    selected = item.opportunity.id == selectedOpportunityId,
                    onClick = { onSelectOpportunity(item.opportunity.id) },
                    onToggleSave = { onToggleSave(item) },
                    onMarkApplied = { onQuickMarkApplied(item) }
                )
            }
        }

        item(key = "bottom_spacer") {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroBannerCard(
    data: HubUiData,
    onOpenDemoSwitcher: () -> Unit,
    onOpenParser: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_hero_campus),
            contentDescription = "Kampala university hills banner",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.78f),
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.activeProfile.fullName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.surface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${data.activeProfile.university} · Yr ${data.activeProfile.yearOfStudy} ${data.activeProfile.program}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                AssistChip(
                    onClick = onOpenDemoSwitcher,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Demo Switch") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        leadingIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("hero_demo_switcher_chip")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${data.activeProfile.skills.size} active skills tracked",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                    Text(
                        text = data.activeProfile.skills.take(4).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    onClick = onOpenParser,
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.testTag("hero_parse_cta")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Paste Forward",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricMiniCard(
    label: String,
    value: String,
    icon: ImageVector,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.testTag("kpi_$label"),
        shape = RoundedCornerShape(16.dp),
        color = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highlighted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = if (highlighted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (highlighted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyDetailPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.PeopleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "Select an opportunity",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Inspect eligibility, skill match breakdown, and track your application status here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DemoProfileSwitcherSheetContent(
    activeKey: String,
    personalName: String,
    personalSubtitle: String,
    onSelectProfile: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Quick Demo Student Switcher",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Instantly test how CrestPath's match engine and AI Advisor adapt across different Ugandan university students.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ProfileOptionTile(
            title = "$personalName (My Profile)",
            subtitle = personalSubtitle,
            badge = "REAL",
            selected = activeKey.isBlank(),
            onClick = { onSelectProfile("") },
            tag = "demo_option_personal"
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        VerifiedCatalog.demoProfiles.forEach { demo ->
            ProfileOptionTile(
                title = demo.fullName,
                subtitle = "${demo.university} · ${demo.demoTagline}",
                badge = "DEMO",
                selected = activeKey == demo.profileKey,
                onClick = { onSelectProfile(demo.profileKey) },
                tag = "demo_option_${demo.profileKey}"
            )
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ProfileOptionTile(
    title: String,
    subtitle: String,
    badge: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = if (selected) "ACTIVE" else badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
