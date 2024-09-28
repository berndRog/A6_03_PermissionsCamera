package de.rogallab.mobile.domain.utilities

fun formatf52(value: Double): String {
   if(value >= 0) return "%05.2f".format(value)
   return "%06.2f".format(value)
}