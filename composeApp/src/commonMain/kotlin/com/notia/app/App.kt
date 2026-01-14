package com.notia.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.notia.app.navigation.NotiaNavGraph
import com.notia.app.ui.theme.NotiaTheme

import com.notia.app.di.ServiceLocator

@Composable
fun App(context: Any? = null) {
    ServiceLocator.init(context)

    NotiaTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NotiaNavGraph()
        }
    }
}
