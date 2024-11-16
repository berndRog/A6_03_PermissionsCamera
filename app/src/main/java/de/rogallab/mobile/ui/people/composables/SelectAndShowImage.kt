package de.rogallab.mobile.ui.people.composables
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun SelectAndShowImage(
   imagePath: String?,                                 // State ↓
   onImagePathChange: (String) -> Unit,              // Event ↑
) {

   Row(
      modifier = Modifier
         .padding(vertical = 8.dp)
         .fillMaxWidth()
   ) {
      imagePath?.let { url:String ->                  // State ↓
         // logDebug("<-SelectAndShowImage","imageUrl $url")
         AsyncImage(
            modifier = Modifier
               .size(width = 150.dp, height = 200.dp)
               .clip(RoundedCornerShape(percent = 5)),
            model = url,
            contentDescription = "Bild des Kontakts",
            alignment = Alignment.Center,
            contentScale = ContentScale.Crop
         )
      }
//    ?: run {          // else ... show chips
      .run {            // and ... always show chips
         Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center
         ) {
            SelectPhotoFromGallery(
               onImagePathChanged = onImagePathChange // Event ↑
            )

            Spacer(modifier = Modifier.padding(vertical = 4.dp))

            TakePhotoWithCamera(
               onImagePathChanged = onImagePathChange // Event ↑
            )
         }
      }
   }
}