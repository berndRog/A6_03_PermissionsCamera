package de.rogallab.mobile.ui.people.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PersonListItem(
   firstName: String,
   lastName: String,
   email: String?,
   phone: String?,
   imagePath: String?,
   onClick: () -> Unit
) {
   Column {
      Row(
         verticalAlignment = Alignment.CenterVertically,
         modifier = Modifier.clickable {
            onClick()
         }
      ) {
         Column {
            Text(
               text = "$firstName $lastName",
               style = MaterialTheme.typography.bodyLarge,
            )
            email?.let {
               Text(
                  modifier = Modifier.padding(top = 4.dp),
                  text = it,
                  style = MaterialTheme.typography.bodyMedium
               )
            }
            phone?.let {
               Text(
                  text = phone,
                  style = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier
               )
            }
         }
      }
      HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
   }
}