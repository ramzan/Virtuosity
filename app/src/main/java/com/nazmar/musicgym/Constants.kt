package com.nazmar.musicgym

// Timer constants
const val MAX_TIMER_DURATION = 359999000L
const val DEFAULT_TIMER_DURATION = 300000L

// Used in saved instance state to prevent reloading certain data on orientation change
const val FIRST_RUN_KEY = "FIRST_RUN_KEY"

// Pref keys for saved session
const val SAVED_SESSION_ID = "SAVED_SESSION_ID"
const val SAVED_SESSION_NAME = "SAVED_SESSION_NAME"
const val SAVED_SESSION_TIME = "SAVED_SESSION_TIME"

// Timer service notification
const val TIMER_NOTIFICATION_ID = 1
const val REQUEST_CODE_RESTART = 0
const val REQUEST_CODE_PAUSE = 1
const val REQUEST_CODE_RESUME = 2