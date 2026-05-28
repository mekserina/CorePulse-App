# CorePulse ProGuard Rules

# Keep Compose-related classes
-dontwarn androidx.compose.**

# Keep data model classes used for telemetry
-keep class com.architecture.corepulse.data.model.** { *; }
