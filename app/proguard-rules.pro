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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ===== 핵심 =====
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*

# Retrofit
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

-keep interface  com.project.locarm.data.remote.ApiService

# Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class **$$serializer { *; }

# DTO
-keep class com.project.locarm.data.model.AddressDTO { *; }
-keep class com.project.locarm.data.model.Result { *; }
-keep class com.project.locarm.data.model.Common { *; }
-keep class com.project.locarm.data.model.Juso { *; }

# suspend 안정화
-keep class kotlin.coroutines.Continuation
-keepclassmembers class * {
    kotlin.coroutines.Continuation *;
}
