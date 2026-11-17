package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.CareerAiRepository
import com.example.data.CareerRepository
import com.example.data.UiState
import com.example.firebase.signOutUser
import com.example.ui.theme.ThemeMode
import com.example.viewmodel.CareerHubViewModel
import com.google.firebase.firestore.FirebaseFirestore

enum class CrestTab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DISCOVER("Discover", Icons.Rounded.Explore, Icons.Outlined.Explore),
    PARSE("Parse Post", Icons.Rounded.ContentPaste, Icons.Outlined.ContentPaste),
    ADVISOR("Advisor", Icons.Rounded.AutoAwesome, Icons.Outlined.AutoAwesome),
    PROFILE("Profile", Icons.Rounded.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShellScreen(
    currentUserId: String,
    userDisplayName: String?,
    userEmail: String?,
    themeMode: ThemeMode,
    onSelectTheme: (ThemeMode) -> Unit,
    onQuickToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: CareerHubViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                CareerHubViewModel(
                    repository = CareerRepository(db),
                    aiRepository = CareerAiRepository(),
                    currentUserId = currentUserId,
                    defaultDisplayName = userDisplayName.orEmpty()
                )
            }
        }
    )

    val hubState by viewModel.hubState.collectAsStateWithLifecycle()
    val selectedOpportunityId by viewModel.selectedOpportunityId.collectAsStateWithLifecycle()
    val rawForwardInput by viewModel.rawForwardInput.collectAsStateWithLifecycle()
    val parseState by viewModel.parseState.collectAsStateWithLifecycle()
    val advisorState by viewModel.advisorState.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val savingAction by viewModel.savingAction.collectAsStateWithLifecycle()

    var activeTab by rememberSaveable { mutableStateOf(CrestTab.DISCOVER) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        val msg = snackbarMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeSnackbar()
        }
    }

    // Back navigation from secondary tabs returns to Discover home tab
    if (activeTab != CrestTab.DISCOVER) {
        BackHandler { activeTab = CrestTab.DISCOVER }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "CrestPath",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = activeTab.label.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onQuickToggleTheme,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("topbar_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (themeMode == ThemeMode.DARK) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                contentDescription = "Toggle light and dark theme"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        CrestTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_bar_${tab.name}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        CrestTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_rail_${tab.name}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (val state = hubState) {
                        UiState.Loading -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(Modifier.size(12.dp))
                                Text(
                                    text = "Loading your CrestPath workspace...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        is UiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Workspace Sync Issue",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Text(
                                            text = state.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }

                        is UiState.Success -> {
                            val data = state.data
                            when (activeTab) {
                                CrestTab.DISCOVER -> {
                                    DashboardScreen(
                                        data = data,
                                        selectedOpportunityId = selectedOpportunityId,
                                        onSelectOpportunity = viewModel::selectOpportunity,
                                        onSearchChange = viewModel::updateSearchQuery,
                                        onSelectCategory = viewModel::selectCategory,
                                        onSelectRegion = viewModel::selectRegion,
                                        onSelectCity = viewModel::selectCity,
                                        onClearLocationFilter = viewModel::clearLocationFilter,
                                        onClearAllFilters = viewModel::clearAllFilters,
                                        onSelectTrackerFilter = viewModel::selectTrackerFilter,
                                        onSelectSort = viewModel::selectSort,
                                        onToggleSave = viewModel::toggleSaved,
                                        onUpdateStatus = viewModel::updateApplicationStatus,
                                        onDeleteImported = viewModel::deleteImportedOpportunity,
                                        onSwitchDemoProfile = viewModel::switchDemoProfile,
                                        onOpenParser = { activeTab = CrestTab.PARSE }
                                    )
                                }

                                CrestTab.PARSE -> {
                                    ForwardParserScreen(
                                        rawInput = rawForwardInput,
                                        parseState = parseState,
                                        isSaving = savingAction,
                                        onInputChange = viewModel::updateRawForwardInput,
                                        onLoadSample = viewModel::loadSampleForward,
                                        onParseClick = viewModel::parseCurrentForward,
                                        onClearClick = viewModel::clearParser,
                                        onUpdateDraft = viewModel::updateParsedDraft,
                                        onSaveDraft = { draft ->
                                            viewModel.saveParsedOpportunity(draft) {
                                                activeTab = CrestTab.DISCOVER
                                            }
                                        }
                                    )
                                }

                                CrestTab.ADVISOR -> {
                                    CareerAdvisorScreen(
                                        activeProfile = data.activeProfile,
                                        allItems = data.allItems,
                                        advisorState = advisorState,
                                        onSendQuestion = viewModel::askAdvisor,
                                        onClearChat = viewModel::clearAdvisorChat,
                                        onOpenVerifiedOpportunity = { oppId ->
                                            viewModel.selectOpportunity(oppId)
                                            activeTab = CrestTab.DISCOVER
                                        }
                                    )
                                }

                                CrestTab.PROFILE -> {
                                    ProfileSettingsScreen(
                                        userEmail = userEmail,
                                        personalProfile = data.personalProfile,
                                        activeProfile = data.activeProfile,
                                        themeMode = themeMode,
                                        isSaving = savingAction,
                                        onSelectTheme = onSelectTheme,
                                        onSwitchDemoProfile = viewModel::switchDemoProfile,
                                        onSavePersonalProfile = viewModel::savePersonalProfile,
                                        onSignOut = {
                                            signOutUser(
                                                credentialManager = credentialManager,
                                                scope = scope
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
