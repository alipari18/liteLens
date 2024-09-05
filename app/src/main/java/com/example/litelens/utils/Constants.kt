package com.example.litelens.utils

object Constants {

    // DataStore
    const val USER_CONFIG_DATASTORE_NAME = "userConfigDatastore"
    const val USER_CONFIG = "userConfig"

    // Values
    const val INITIAL_CONFIDENCE_SCORE = 0.1f
    const val ORIGINAL_IMAGE_WIDTH = 480f
    const val ORIGINAL_IMAGE_HEIGHT = 640f
    const val SMOOTHING_DURATION = 50L

    // TensorFlow Lite
    const val MODEL_MAX_RESULTS_COUNT = 1
    const val MODEL_PATH = "efficientdet-lite2.tflite"
}