# ProGuard & R8 Optimization Rules for Thunder Dome (3000 Studios)
# Target: Google Play Production Release

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# AndroidX Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.example.data.** { *; }

# Google Play Billing Client
-keep class com.android.billingclient.api.** { *; }
-dontwarn com.android.billingclient.**

# Google Mobile Ads / AdMob
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}
-keep class com.google.android.gms.ads.mediation.** { *; }
-dontwarn com.google.android.gms.ads.**

# Firebase Core, Auth & Firestore
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keepattributes *Annotation*
-keep class com.google.android.gms.tasks.** { *; }

# Google Identity / Credential Manager
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**
-dontwarn com.google.android.libraries.identity.googleid.**

# Moshi JSON Models
-keepclassmembers class * {
    @com.squareup.moshi.Json *;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-dontwarn com.squareup.moshi.**

# Game Models & Entity Catalogs
-keep class com.example.game.model.** { *; }
-keep class com.example.data.** { *; }
-keep class com.example.game.audio.** { *; }
-keep class com.example.game.engine.** { *; }
