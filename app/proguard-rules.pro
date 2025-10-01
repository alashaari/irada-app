# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Google Play Billing
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# Keep data classes
-keep class com.irada.blockerheroar.data.** { *; }

# Keep model classes
-keep class com.irada.blockerheroar.data.User { *; }
-keep class com.irada.blockerheroar.data.AppSettings { *; }
-keep class com.irada.blockerheroar.data.BlockedContent { *; }
-keep class com.irada.blockerheroar.data.PartnerRequest { *; }

# Keep service classes
-keep class com.irada.blockerheroar.service.** { *; }

# Keep receiver classes
-keep class com.irada.blockerheroar.receiver.** { *; }

# Keep utility classes
-keep class com.irada.blockerheroar.utils.** { *; }

# Keep billing classes
-keep class com.irada.blockerheroar.billing.** { *; }

# Accessibility Service
-keep class com.irada.blockerheroar.service.ContentBlockingService { *; }

# Device Admin
-keep class com.irada.blockerheroar.receiver.DeviceAdminReceiver { *; }

# Keep application class
-keep class com.irada.blockerheroar.IradaApplication { *; }

# Keep all activities
-keep class com.irada.blockerheroar.ui.** { *; }

# Keep Compose classes
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep Kotlin serialization
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Keep WorkManager
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# Keep DataStore
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**
