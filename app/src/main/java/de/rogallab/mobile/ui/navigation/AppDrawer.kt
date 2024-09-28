package de.rogallab.mobile.ui.navigation


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppDrawer(
   drawerState: DrawerState,
   scope: CoroutineScope
) {
   ModalDrawerSheet {
      Text(
         text = "Navigation Drawer",
         style = MaterialTheme.typography.headlineMedium,
         modifier = Modifier.padding(16.dp)
      )

      HorizontalDivider()

      DrawerItem(
         icon = Icons.Default.Home,
         label = "Home",
         onClick = {
            scope.launch { drawerState.close() }
            // Handle navigation
         }
      )

      DrawerItem(
         icon = Icons.Default.Home,
         label = "Home",
         onClick = {
            scope.launch { drawerState.close() }
            // Handle navigation
         }
      )

      DrawerItem(
         icon = Icons.Default.Settings,
         label = "Settings",
         onClick = {
            scope.launch { drawerState.close() }
            // Handle navigation
         }
      )
      // Add more items as needed
   }
}

@Composable
fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit) {
   Row(
      modifier = Modifier
         .fillMaxWidth()
         .clickable(onClick = onClick)
         .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
   ) {
      Icon(imageVector = icon, contentDescription = label)
      Spacer(modifier = Modifier.width(16.dp))
      Text(text = label, style = MaterialTheme.typography.bodyLarge)
   }
}