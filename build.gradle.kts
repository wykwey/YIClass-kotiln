plugins {
    alias(libs.plugins.android.application) apply false
    // kotlin-android 已移除：AGP 9.0+ 内置 Kotlin 支持
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.androidx.room) apply false
}
