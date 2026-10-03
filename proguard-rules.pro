# Release Forge 1.16.5: ofuscación sin shrinking ni optimización.
# Forge/Minecraft permanecen como librerías y solo se transforma el código del mod.
-dontshrink
-dontoptimize
# Mantiene/genera StackMapTable para que Java 8 pueda verificar los saltos
# del bytecode transformado por ProGuard.
-dontwarn
-dontnote
-ignorewarnings
-useuniqueclassmembernames
-dontusemixedcaseclassnames
-adaptclassstrings
-allowaccessmodification
-printmapping build/obfuscation/pocketdimension-1.0.3.map

# Se conservan los metadatos que Forge, EventBus y los mods opcionales leen por reflexión.
-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature,Exceptions,Synthetic
-keep @interface *

# Los métodos del mod que sobrescriben clases de Minecraft/Forge deben conservar
# su nombre reobfuscado. Si ProGuard los renombra, el juego no puede enlazarlos
# al ejecutarse (por ejemplo ItemGroup.makeIcon -> func_78016_d) y aparece
# AbstractMethodError al abrir una pantalla o inventario.
-keepclassmembers class com.fosder.pocketdimension.** {
    *;
}

# Entry point declarado por META-INF/mods.toml.
-keep class com.fosder.pocketdimension.PocketDimensionMod {
    public <init>();
}

# Suscriptores automáticos de Forge y sus callbacks.
-keep @net.minecraftforge.fml.common.Mod$EventBusSubscriber class * { *; }
-keepclassmembers class * {
    @net.minecraftforge.eventbus.api.SubscribeEvent <methods>;
}

# Descubrimiento opcional de JEI.
-keep @mezz.jei.api.JeiPlugin class * { *; }
