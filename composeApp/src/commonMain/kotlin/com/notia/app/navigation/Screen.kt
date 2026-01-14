package com.notia.app.navigation

/**
 * Navigation routes for Notia app
 */
sealed class Screen(val route: String) {
    data object NotesList : Screen("notes_list")
    data object Editor : Screen("editor/{noteId}") {
        fun createRoute(noteId: String) = "editor/$noteId"
    }
    data object Timer : Screen("timer")
    data object Settings : Screen("settings")
}
