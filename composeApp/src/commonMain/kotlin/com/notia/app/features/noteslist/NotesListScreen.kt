package com.notia.app.features.noteslist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Timer // Added import
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notia.app.core.utils.randomUUID
import com.notia.app.features.noteslist.components.NotebookCover
import com.notia.app.ui.theme.NeonBlue
import com.notia.app.ui.theme.PremiumBackground
import com.notia.app.ui.theme.TextPrimary
import com.notia.app.ui.theme.TextSecondary
import com.notia.app.ui.theme.TextTertiary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    onNavigateToEditor: (String) -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    // FAB press animation
    var isFabPressed by remember { mutableStateOf(false) }
    val fabScale by animateFloatAsState(
        targetValue = if (isFabPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "fab_scale"
    )
    
    // Data Source
    val noteDao = com.notia.app.di.ServiceLocator.noteDao
    val notes by noteDao.getAllNotes().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = PremiumBackground, // OLED Black
        topBar = {
            NotesListTopBar(
                scrollBehavior = scrollBehavior,
                onTimerClick = onNavigateToTimer,
                onSettingsClick = onNavigateToSettings
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Create new note with random UUID and save to DB
                    val newNoteId = randomUUID()
                    val currentTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                    
                    val newNote = com.notia.app.data.database.entity.NoteEntity(
                        id = newNoteId,
                        title = "Untitled Note", // English fallback or localized
                        createdAt = currentTime,
                        updatedAt = currentTime,
                        isPinned = false,
                        thumbPath = null
                    )
                    
                    val newMeta = com.notia.app.filestore.models.NoteMeta(
                        noteId = newNoteId,
                        title = newNote.title,
                        createdAt = currentTime,
                        updatedAt = currentTime
                    )

                    coroutineScope.launch(Dispatchers.Default) {
                       // 1. Create package on disk
                       // val packageStore = com.notia.app.filestore.NotiaPackageStore()
                       // packageStore.createPackage(newMeta)
                       
                       // 2. Insert into DB
                       noteDao.insertNote(newNote)
                    }
                    onNavigateToEditor(newNoteId)
                },
                modifier = Modifier
                    .scale(fabScale)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when {
                                    event.changes.any { it.pressed } -> isFabPressed = true
                                    else -> isFabPressed = false
                                }
                            }
                        }
                    },
                containerColor = NeonBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Note"
                )
            }
        }
    ) { paddingValues ->
        if (notes.isEmpty()) {
            // Empty State - Minimalist
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Create your first masterpiece.",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    
                    // Simple text button instead of illustration
                    Box(modifier = Modifier
                        .background(Color.White.copy(0.05f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Tap + to start", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
             // Masonry-like Grid
             LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp), // Wider cards
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(notes.size) { index ->
                    val note = notes[index]
                    NotebookCover(
                        title = note.title,
                        coverIndex = index,
                        onClick = { onNavigateToEditor(note.id) },
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesListTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onTimerClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    LargeTopAppBar(
        title = {
            Text(
                text = "Documents", // Clean, minimal
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        },
        actions = {
            IconButton(onClick = onTimerClick) {
                Icon(
                    imageVector = Icons.Default.Timer, // Changed to Timer icon
                    contentDescription = "Timer",
                    tint = TextPrimary.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextPrimary.copy(alpha = 0.7f)
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = Color.Transparent, // Glass effect
            scrolledContainerColor = PremiumBackground.copy(alpha = 0.9f),
            titleContentColor = TextPrimary
        )
    )
}
