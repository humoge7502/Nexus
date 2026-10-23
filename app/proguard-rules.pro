# ============================================================
# NEXUS ProGuard Rules — Production Complete
# ============================================================

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================================
# KOTLIN
# ============================================================
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Lazy { *; }
-dontwarn kotlin.**

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer {
    kotlinx.serialization.descriptors.SerialDescriptor descriptor;
}
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class * { *; }

# ============================================================
# HILT / DAGGER
# ============================================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.** { *; }
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# ============================================================
# FIREBASE
# ============================================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.firebase.firestore.** { *; }

# ============================================================
# ROOM
# ============================================================
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**

# ============================================================
# RETROFIT + OKHTTP
# ============================================================
-keepattributes Signature, Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ============================================================
# WORKMANAGER
# ============================================================
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class androidx.work.** { *; }

# ============================================================
# FIREBASE FUNCTIONS (cloud backend proxy)
# ============================================================
-keep class com.google.firebase.functions.** { *; }
-dontwarn com.google.firebase.functions.**

# ============================================================
# MIGRATION REGISTRY + MIGRATIONS
# ============================================================
-keep class com.nexus.database.migration.** { *; }

# ============================================================
# NETWORK SERVICES (FirebaseFunctionsAIService, AIRequestRouter, etc.)
# ============================================================
-keep class com.nexus.network.service.** { *; }

# ============================================================
# COIL
# ============================================================
-keep class coil.** { *; }
-dontwarn coil.**

# ============================================================
# LOTTIE
# ============================================================
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ============================================================
# VICO CHARTS
# ============================================================
-keep class com.patrykandpatrick.vico.** { *; }
-dontwarn com.patrykandpatrick.vico.**

# ============================================================
# KONFETTI
# ============================================================
-keep class nl.dionsegijn.konfetti.** { *; }
-dontwarn nl.dionsegijn.konfetti.**

# ============================================================
# TIMBER
# ============================================================
-keep class timber.log.Timber { *; }
-dontwarn timber.log.**

# ============================================================
# DOMAIN MODELS (Keep for Firestore deserialization)
# ============================================================
-keep class com.nexus.domain.model.** { *; }
-keep class com.nexus.network.dto.** { *; }
-keep class com.nexus.database.entity.** { *; }
-keepclassmembers class com.nexus.** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ============================================================
# NEXUS APP SPECIFIC
# ============================================================

-keep @dagger.hilt.android.qualifiers.ApplicationContext class *
-keep class * extends androidx.hilt.work.HiltWorker { *; }

# ============================================================
# SECURITY — API keys must be moved to a backend proxy
# ============================================================
# WARNING: BuildConfig fields are compile-time constants (static final).
# The Java compiler inlines them into every call site before R8 sees them,
# so -assumevalues CANNOT redact them. API keys in BuildConfig are
# embedded in the APK bytecode literally.
#
# RECOMMENDED: Move all secret keys (OpenRouter, GitHub tokens) to a
# backend API proxy. The Android app should call your backend, which
# forwards to the third-party API. Never embed keys in the client.
#-assumevalues class com.nexus.app.BuildConfig {
#    java.lang.String OPENROUTER_API_KEY return "redacted";
#    java.lang.String GITHUB_TOKEN return "redacted";
#    java.lang.String GITHUB_CLIENT_SECRET return "redacted";
#}
#-assumevalues class com.nexus.network.BuildConfig {
#    java.lang.String OPENROUTER_API_KEY return "redacted";
#    java.lang.String GITHUB_TOKEN return "redacted";
#    java.lang.String GITHUB_CLIENT_SECRET return "redacted";
#}

# ============================================================
# SECURITY — Strip Timber logs in release
# ============================================================
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# ============================================================
# SECURITY — Strip debug logging in release
# ============================================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ============================================================
# SECURITY — Remove BuildConfig references
# ============================================================
-keepclassmembers class **.BuildConfig {
    *** DEBUG;
    *** APPLICATION_ID;
    *** BUILD_TYPE;
    *** VERSION_CODE;
    *** VERSION_NAME;
}
