package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.ActiveStudentProfile
import com.example.data.StudentProfileDoc
import com.example.data.VerifiedCatalog
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSettingsScreen(
    userEmail: String?,
    personalProfile: StudentProfileDoc?,
    activeProfile: ActiveStudentProfile,
    themeMode: ThemeMode,
    isSaving: Boolean,
    onSelectTheme: (ThemeMode) -> Unit,
    onSwitchDemoProfile: (String) -> Unit,
    onSavePersonalProfile: (
        fullName: String,
        university: String,
        program: String,
        yearOfStudy: Int,
        gpa: String,
        skills: List<String>,
        interests: List<String>
    ) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember(personalProfile?.fullName) {
        mutableStateOf(personalProfile?.fullName?.ifBlank { "Ugandan Scholar" } ?: "Ugandan Scholar")
    }
    var university by remember(personalProfile?.university) {
        mutableStateOf(personalProfile?.university?.ifBlank { "Makerere University" } ?: "Makerere University")
    }
    var program by remember(personalProfile?.program) {
        mutableStateOf(personalProfile?.program?.ifBlank { "BSc Computer Science" } ?: "BSc Computer Science")
    }
    var yearOfStudy by remember(personalProfile?.yearOfStudy) {
        mutableIntStateOf((personalProfile?.yearOfStudy ?: 3L).toInt().coerceIn(1, 5))
    }
    var gpa by remember(personalProfile?.gpa) {
        mutableStateOf(personalProfile?.gpa ?: "4.20")
    }
    var skills by remember(personalProfile?.skills) {
        mutableStateOf(personalProfile?.skills ?: listOf("Kotlin", "Python", "SQL", "Git"))
    }
    var interests by remember(personalProfile?.interests) {
        mutableStateOf(personalProfile?.interests ?: listOf("Software Engineering", "Scholarships"))
    }
    var newSkillInput by remember { mutableStateOf("") }
    var newInterestInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Demo Profile Switcher Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Quick Demo Student Switcher",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Switch between your personal profile and realistic Makerere, Kyambogo, or MUST student personas to preview skill matches and AI Advisor feedback.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !activeProfile.demoMode,
                            onClick = { onSwitchDemoProfile("") },
                            label = { Text("My Profile") },
                            modifier = Modifier.testTag("settings_demo_personal")
                        )
                        VerifiedCatalog.demoProfiles.forEach { demo ->
                            FilterChip(
                                selected = activeProfile.profileKey == demo.profileKey,
                                onClick = { onSwitchDemoProfile(demo.profileKey) },
                                label = { Text(demo.fullName) },
                                modifier = Modifier.testTag("settings_demo_${demo.profileKey}")
                            )
                        }
                    }

                    if (activeProfile.demoMode) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Active Demo: ${activeProfile.fullName} — ${activeProfile.university}, Year ${activeProfile.yearOfStudy} ${activeProfile.program}. Skills: ${activeProfile.skills.joinToString(", ")}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // 2. Personal Profile Editor
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "My Ugandan Student Profile",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (!userEmail.isNullOrBlank()) {
                        Text(
                            text = "Signed in as $userEmail",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it.take(100) },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input")
                    )

                    OutlinedTextField(
                        value = university,
                        onValueChange = { university = it.take(120) },
                        label = { Text("University") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_university_input")
                    )

                    // Quick Ugandan university chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        VerifiedCatalog.ugandanUniversities.take(5).forEach { uni ->
                            FilterChip(
                                selected = university.equals(uni, ignoreCase = true),
                                onClick = { university = uni },
                                label = { Text(uni.substringBefore(" (")) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = program,
                            onValueChange = { program = it.take(120) },
                            label = { Text("Degree / Course") },
                            singleLine = true,
                            modifier = Modifier.weight(1.5f)
                        )
                        OutlinedTextField(
                            value = gpa,
                            onValueChange = { gpa = it.take(20) },
                            label = { Text("CGPA") },
                            singleLine = true,
                            modifier = Modifier.weight(0.7f)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Year of Study", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..5).forEach { yr ->
                                FilterChip(
                                    selected = yearOfStudy == yr,
                                    onClick = { yearOfStudy = yr },
                                    label = { Text("Year $yr") }
                                )
                            }
                        }
                    }

                    // Skills tag editor
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Skills (${skills.size}/25)", style = MaterialTheme.typography.titleSmall)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newSkillInput,
                                onValueChange = { newSkillInput = it.take(40) },
                                placeholder = { Text("Add skill (e.g., Kotlin, AutoCAD, R)") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("profile_add_skill_input")
                            )
                            IconButton(
                                onClick = {
                                    val clean = newSkillInput.trim()
                                    if (clean.isNotEmpty() && skills.size < 25 && skills.none { it.equals(clean, ignoreCase = true) }) {
                                        skills = skills + clean
                                        newSkillInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("profile_add_skill_button")
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = "Add skill")
                            }
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            skills.forEach { sk ->
                                InputChip(
                                    selected = true,
                                    onClick = { skills = skills.filterNot { it == sk } },
                                    label = { Text(sk) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Rounded.Close,
                                            contentDescription = "Remove $sk",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Interests tag editor
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Career Interests (${interests.size}/15)", style = MaterialTheme.typography.titleSmall)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newInterestInput,
                                onValueChange = { newInterestInput = it.take(40) },
                                placeholder = { Text("Add interest (e.g., FinTech, Global Health)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val clean = newInterestInput.trim()
                                    if (clean.isNotEmpty() && interests.size < 15 && interests.none { it.equals(clean, ignoreCase = true) }) {
                                        interests = interests + clean
                                        newInterestInput = ""
                                    }
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = "Add interest")
                            }
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            interests.forEach { item ->
                                InputChip(
                                    selected = false,
                                    onClick = { interests = interests.filterNot { it == item } },
                                    label = { Text(item) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Rounded.Close,
                                            contentDescription = "Remove $item",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            onSavePersonalProfile(
                                fullName,
                                university,
                                program,
                                yearOfStudy,
                                gpa,
                                skills,
                                interests
                            )
                        },
                        enabled = !isSaving && fullName.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_profile_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save My Profile")
                        }
                    }
                }
            }

            // 3. Appearance (Light / Dark / System Theme)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("Theme Appearance", style = MaterialTheme.typography.titleMedium)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { onSelectTheme(mode) },
                                label = { Text(mode.label) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("theme_mode_${mode.name}")
                            )
                        }
                    }
                }
            }

            // 4. Data Safety & Sign Out
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("Safety & User Data Protection", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        text = "• Authenticated via Google Sign-In with strict per-user Firestore ownership rules.\n• Your saved notes, bookmarks, and imported forwards are unreadable by other accounts.\n• Imported WhatsApp/LinkedIn forwards are permanently flagged as Unverified so they never mix into Verified-Only AI Advisor recommendations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sign_out_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Sign Out")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
