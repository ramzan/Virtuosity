package com.nazmar.musicgym.common

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

// Confirmation dialog result
const val CONFIRMATION_RESULT = "dialog_result"
const val POSITIVE_RESULT = "positive_result"

// Text input dialog result
const val TEXT_INPUT_RESULT = "text_input_result"
const val INPUT_TEXT = "input_text"

// Duration picker dialog result
const val DURATION_PICKER_RESULT = "duration_picker_result"
const val DURATION_VALUE = "duration_value"