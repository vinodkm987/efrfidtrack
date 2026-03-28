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

 -dontwarn android.os.ServiceManager
 -dontwarn org.slf4j.impl.StaticLoggerBinder
 -dontwarn javax.lang.model.element.Modifier
 -dontwarn org.bouncycastle.crypto.BlockCipher
 -dontwarn org.bouncycastle.crypto.CipherParameters
 -dontwarn org.bouncycastle.crypto.InvalidCipherTextException
 -dontwarn org.bouncycastle.crypto.engines.AESEngine
 -dontwarn org.bouncycastle.crypto.engines.RFC5649WrapEngine
 -dontwarn org.bouncycastle.crypto.params.KeyParameter

 -dontwarn com.zebra.rfidhost.RFIDHostEventAndReason
 -dontwarn com.zebra.rfidhostlib.BuildConfig
 -dontwarn com.zebra.rfidserial.RfidSerial
 -dontwarn com.zebra.rfidhost.IRFIDHostCallBack
 -dontwarn com.zebra.rfidhost.RfidHost
 -dontwarn com.zebra.rfidhost.RfidHost$ConnectionListener
 -dontwarn com.zebra.rfidhost.IRFIDHostCallBack$Stub
 -dontwarn com.zebra.rfidserial.RfidSerial$ConnectionListener
 -dontwarn vendor.zebra.hardware.rfidserial.IPort
 -dontwarn com.zebra.rfid.api3.**




 -keep public class com.zebra.rfid.** { *; }
 -keep public class com.zebra.rfidhost.** { *; }
 -keep public class com.zebra.rfidhostlib.** { *; }
 -keep public class com.zebra.rfid.**$* { *; }
 -keep public class com.zebra.rfidhost.**$* { *; }
 -keep public class com.zebra.rfidhostlib.**$* { *; }
 -keep public class com.zebra.rfidserial.** { *; }
 -keep public class com.zebra.rfidserial.**$* { *; }

 -keep class com.zebra.rfid.RfidServiceMgr
 -keep class android.os.ServiceManager.** { *; }
 -keep class org.slf4j.impl.StaticLoggerBinder.** { *; }

 -keep class com.zebra.rfidhost.** { *; }
 -keep class com.zebra.rfidhostlib.** { *; }
 -keep class com.zebra.rfidserial.** { *; }
 -keep class vendor.zebra.hardware.rfidserial.** { *; }
 -keep class com.zebra.rfid.api.** { *; }
 -keep class com.zebra.scannercontrol.**{ *; }
 -keepattributes *Annotation*
 -keep class * extends java.lang.annotation.Annotation{ *; }
 -keep class javax.lang.model.**{ *; }
 -keep class javax.lang.model.element.Modifier.**{ *; }
 -keep class com.google.errorprone.** { *; }
 -keep class com.zebra.rfid.api3.HexDump.** { *; }
 -keep class com.zebra.rfid.api3.** { *; }
 -keep interface com.zebra.rfid.** { *; }

 -keep public class * extends android.app.Application
 -keep public class * extends android.app.Activity
 -keep public class * extends android.app.Service
 -keep public class * extends android.content.BroadcastReceiver
 -keep public class * extends android.content.ContentProvider


 -keep class com.zebra.rfid.api3.InvalidUsageException { *; }
 -keep class com.zebra.rfid.api3.OperationFailureException { *; }
 -keep class com.zebra.rfid.api3.RFIDResults { *; }

-keepclassmembers class com.zebra.rfid.api3.** { *; }

-keepclassmembers class com.zebra.rfid.api3.PreFilters {
    int length();
}

 -keep class com.zebra.LTK.org.llrp.ltk.** { *; }
 -keep class com.zebra.LTK.org.llrp.ltk.**$* { *; }
 -keep class com.zebra.LTK.org.apache.mina.** { *; }
 -keep class com.zebra.LTK.org.apache.mina.**$* { *; }
 -dontwarn com.zebra.LTK.org.apache.**
 -keep class com.zebra.LTK.org.apache.** { *; }
 -keep class com.zebra.LTK.org.apache.**$* { *; }
 -keepclassmembers class com.zebra.LTK.org.apache.mina.transport.socket.nio.NioProcessor {
     public <init>(...);
 }
 -keep class com.zebra.LTK.org.apache.mina.transport.socket.nio.NioProcessor { *; }
 # Keep all classes and inner classes in llrp and its subpackages
 -keep class com.zebra.LTK.org.apache.mina.core.** { *; }
 -keep class com.zebra.LTK.org.apache.mina.core.**$* { *; }
 -keep class com.zebra.LTK.org.apache.mina.core.file.** { *; }
 -keep class com.zebra.LTK.org.apache.mina.core.future.** { *; }
 -keep class com.zebra.LTK.org.apache.mina.core.filterchain.** { *; }
 -keep class com.zebra.LTK.org.apache.mina.core.polling.** { *; }
 -keep class com.zebra.LTK.org.apache.mina.transport.socket.** { *; }
 -keep class com.zebra.LTK.org.apache.mina.transport.socket.**$* { *; }
 -keep class com.zebra.LTK.org.apache.mina.util.** { *; }
-keepattributes Exceptions