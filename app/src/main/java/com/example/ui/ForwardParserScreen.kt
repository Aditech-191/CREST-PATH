package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.OpportunityCategory
import com.example.data.ParseUiState
import com.example.data.ParsedOpportunityDraft
import com.example.data.VerifiedCatalog

@Composable
fun ForwardParserScreen(
    rawInput: String,
    parseState: ParseUiState,
    isSaving: Boolean,
    onInputChange: (String) -> Unit,
    onLoadSample: (String) -> Unit,
    onParseClick: () -> Unit,
    onClearClick: () -> Unit,
    onUpdateDraft: (ParsedOpportunityDraft) -> Unit,
    onSaveDraft: (ParsedOpportunityDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 840.dp

        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ParserInputPane(
                        rawInput = rawInput,
                        isParsing = parseState is ParseUiState.Parsing,
                        onInputChange = onInputChange,
                        onLoadSample = onLoadSample,
                        onParseClick = onParseClick,
                        onClearClick = onClearClick
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ParserOutputPane(
                        parseState = parseState,
                        isSaving = isSaving,
                        onUpdateDraft = onUpdateDraft,
                        onSaveDraft = onSaveDraft
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ParserInputPane(
                    rawInput = rawInput,
                    isParsing = parseState is ParseUiState.Parsing,
                    onInputChange = onInputChange,
                    onLoadSample = onLoadSample,
                    onParseClick = onParseClick,
                    onClearClick = onClearClick
                )

                ParserOutputPane(
                    parseState = parseState,
                    isSaving = isSaving,
                    onUpdateDraft = onUpdateDraft,
                    onSaveDraft = onSaveDraft
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ParserInputPane(
    rawInput: String,
    isParsing: Boolean,
    onInputChange: (String) -> Unit,
    onLoadSample: (String) -> Unit,
    onParseClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.ContentPaste,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Column {
                    Text(
                        text = "WhatsApp & LinkedIn Post Parser",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Turn messy campus forwards into clean, verifiable opportunity cards.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    onClick = { onLoadSample(VerifiedCatalog.sampleWhatsAppForward) },
                    label = { Text("Try WhatsApp Forward") },
                    modifier = Modifier.testTag("sample_whatsapp_chip")
                )
                AssistChip(
                    onClick = { onLoadSample(VerifiedCatalog.sampleLinkedInPost) },
                    label = { Text("Try LinkedIn Post") },
                    modifier = Modifier.testTag("sample_linkedin_chip")
                )
            }

            OutlinedTextField(
                value = rawInput,
                onValueChange = onInputChange,
                label = { Text("Paste forward or job post text") },
                placeholder = { Text("Paste any messy WhatsApp forward, Telegram post, or LinkedIn announcement here...") },
                minLines = 6,
                maxLines = 12,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("parser_raw_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (rawInput.isNotBlank()) {
                    OutlinedButton(
                        onClick = onClearClick,
                        enabled = !isParsing,
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("parser_clear_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Rounded.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear")
                    }
                }

                Button(
                    onClick = onParseClick,
                    enabled = !isParsing && rawInput.trim().length >= 20,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("parser_extract_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isParsing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Extracting with AI...")
                    } else {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Structure Opportunity Card")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ParserOutputPane(
    parseState: ParseUiState,
    isSaving: Boolean,
    onUpdateDraft: (ParsedOpportunityDraft) -> Unit,
    onSaveDraft: (ParsedOpportunityDraft) -> Unit
) {
    when (parseState) {
        ParseUiState.Idle -> {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "How Safe Extraction Works",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "• Extracts title, organization, deadline, eligibility, required skills, and checklist items.\n• Never invents URLs or closing dates missing from the forward.\n• Automatically flags scam signals (personal Gmail, WhatsApp-only numbers, or registration fees).\n• Saved cards are clearly labeled as Imported (Unverified) so your Verified-Only Career Advisor stays trustworthy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        ParseUiState.Parsing -> {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Structuring title, deadline, eligibility & safety checks...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        is ParseUiState.Error -> {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Extraction Error",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = parseState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        is ParseUiState.Ready -> {
            val draft = parseState.draft
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("parsed_opportunity_preview")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Structured Card Preview",
                            style = MaterialTheme.typography.titleLarge
                        )
                        VerificationBadge(verified = false)
                    }

                    if (draft.verificationWarnings.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                        text = "AI Safety Warnings",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                                draft.verificationWarnings.forEach { w ->
                                    Text(
                                        text = "• $w",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = draft.title,
                        onValueChange = { onUpdateDraft(draft.copy(title = it.take(160))) },
                        label = { Text("Opportunity Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = draft.organization,
                        onValueChange = { onUpdateDraft(draft.copy(organization = it.take(120))) },
                        label = { Text("Organization") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OpportunityCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = draft.category == cat,
                                onClick = { onUpdateDraft(draft.copy(category = cat)) },
                                label = { Text(cat.label) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = draft.deadlineText,
                            onValueChange = { onUpdateDraft(draft.copy(deadlineText = it.take(80))) },
                            label = { Text("Deadline") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = draft.location,
                            onValueChange = { onUpdateDraft(draft.copy(location = it.take(120))) },
                            label = { Text("Location") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = draft.stipendOrValue,
                        onValueChange = { onUpdateDraft(draft.copy(stipendOrValue = it.take(120))) },
                        label = { Text("Stipend / Value") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = draft.eligibility,
                        onValueChange = { onUpdateDraft(draft.copy(eligibility = it.take(600))) },
                        label = { Text("Eligibility") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (draft.requirements.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Extracted Requirements", style = MaterialTheme.typography.titleSmall)
                            draft.requirements.forEach { req ->
                                Text(
                                    text = "• $req",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (draft.requiredSkills.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Extracted Skills", style = MaterialTheme.typography.titleSmall)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                draft.requiredSkills.forEach { skill ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = skill,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (draft.officialUrl.isNotBlank()) {
                        Text(
                            text = "Link found in post: ${draft.officialUrl}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "No official URL present in post (none fabricated).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { onSaveDraft(draft) },
                        enabled = !isSaving && draft.title.isNotBlank() && draft.organization.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_parsed_card_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save Card to My Opportunities")
                        }
                    }
                }
            }
        }
    }
}
