# ===== 핵심 =====
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# suspend 안정화
-keep class kotlin.coroutines.Continuation
