# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/adil/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-dontwarn kotlinx.parcelize.Parcelize

-dontwarn androidx.appcompat.R$attr
-dontwarn androidx.appcompat.R$dimen
-dontwarn androidx.appcompat.R$id
-dontwarn androidx.appcompat.R$layout
-dontwarn androidx.appcompat.R$string
-dontwarn androidx.appcompat.R$style
-dontwarn androidx.appcompat.R$styleable
-dontwarn androidx.appcompat.resources.R$drawable
-dontwarn androidx.appcompat.resources.R$styleable
-dontwarn androidx.core.R$attr
-dontwarn androidx.core.R$id
-dontwarn androidx.core.R$styleable
-dontwarn androidx.work.R$bool
-dontwarn com.google.android.gms.base.R$drawable
-dontwarn com.google.android.gms.base.R$string
-dontwarn com.google.android.gms.common.R$string

#R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

#R8 full mode strips generic signatures from return types if not kept.
-keep,allowobfuscation,allowshrinking class retrofit2.Response

-keepattributes Signature
-keep class kotlin.coroutines.Continuation
