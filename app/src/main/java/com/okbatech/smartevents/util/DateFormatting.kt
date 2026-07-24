package com.okbatech.smartevents.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatEventDate(epochMillis: Long): String =
    SimpleDateFormat("dd MMMM, yy", Locale.getDefault()).format(Date(epochMillis))
