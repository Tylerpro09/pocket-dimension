# ⛔ PROHIBIDO HACER FORK ⛔

<h1 align="center">🚫 PROHIBIDO HACER FORK DE ESTE REPOSITORIO 🚫</h1>

<p align="center"><strong>NO COPIES · NO MODIFIQUES · NO HAGAS FORK · NO REDISTRIBUYAS SIN CRÉDITOS</strong></p>

Este código fuente se publica únicamente para **consulta y lectura**. No está permitido hacer fork, copiar, modificar, traducir, crear derivados, clonar para redistribuir el código ni incorporarlo a otro proyecto sin autorización escrita del autor.

## Redistribución permitida del JAR oficial

Sí puedes subir una copia **sin modificar del JAR oficial** a otra página, siempre que la publicación muestre claramente:

- Autor: **tylerpro08**.
- Página oficial del proyecto: [GitHub](https://github.com/Tylerpro09/pocket-dimension).
- Página oficial de descarga: [CurseForge](https://www.curseforge.com/minecraft/mc-mods/pockets-dimension).

No puedes presentarlo como tuyo, quitar los créditos, modificar el JAR, subir forks ni redistribuir el código fuente. Aternos y otros servicios pueden obtener el JAR desde CurseForge.

Consulta la licencia completa en [`LICENSE`](LICENSE) y [`LICENSE-POCKET-DIMENSION.txt`](LICENSE-POCKET-DIMENSION.txt).

# Pocket Dimension Mod - Forge 1.16.5

Mod base para Minecraft Java 1.16.5 con Forge 36.2.42.

Version actual: `1.0.3`.

## Dependencias

- Minecraft 1.16.5 y cualquier Forge de la rama 36.x son obligatorios para la versión Forge; el proyecto se compila con Forge 36.2.42.
- JEI 7.7.1 o superior es opcional y muestra las 11 recetas privadas de la Mesa Dimensional.
- Curios 1.16.5-4.0.5.3 o superior es opcional y añade la ranura de amuleto para el Estabilizador del Vacío.
- Patchouli 1.16.4-53.3 o superior es opcional y convierte el libro dimensional en una guía navegable dentro del juego.
- GeckoLib 3.0.106 o superior es obligatoria y activa las animaciones 3D del portal, la grieta oscura y el colapso.
- La generación del mundo usa el generador nativo de Forge/JSON y el motor propio del mod; no necesita otra librería externa de worldgen.
- La sección `Mapa` permite editar tamaño, separación, altura, isla, borde, grietas y daño del vacío.
- El modo editor incluye una `Tienda de materiales`: compra lotes de construcción con energía dimensional para no depender del creativo.
- `Ajustes > Créditos de traducción` muestra un registro comunitario desplazable, con el idioma y la persona o comunidad responsable de cada traducción.
- La pantalla puede consultar una distribución pública de Crowdin mediante HTTPS; no guarda ni expone tokens de Crowdin dentro del JAR.
- Fabric dejó de tener soporte en la versión `1.0.3`.

## Incluye

- Mesa Dimensional.
- Llave Dimensional vinculada al jugador.
- Llave Hacker rara con 5% de éxito.
- Integración opcional con Curios: el Estabilizador del Vacío puede equiparse como amuleto y recupera estabilidad dentro del bolsillo.
- Integración opcional con Patchouli: el Libro de Recetas Dimensional abre el Manual de Campo con recetas y consejos.
- Integración con GeckoLib: la Grieta Dimensional pulsa y gira suavemente; la Grieta Oscura muestra una animación de absorción durante el colapso.
- Regla OP: si el jugador es OP, tiene acceso seguro; la idea de protección OP está marcada para extender con selector de objetivo.
- Dimensión `pocketdimension:pocket_world` mediante JSON datapack.
- Recetas JSON.

## Uso dentro del juego

1. Craftea la Mesa Dimensional.
2. Colócala.
3. Shift + click derecho sobre la mesa para crear tu Llave Dimensional.
4. Click derecho con la llave para entrar/salir de la dimensión bolsillo.
5. La Llave Hacker se craftea con materiales muy raros y tiene baja probabilidad de entrar.

En `Ajustes > Mapa` puedes guardar una configuración personalizada. La `Máquina Estabilizadora` también tiene el botón `Editor de mapa`: al entrar se activa el modo editor, que solo concede vuelo, bloquea la muerte y fuerza supervivencia; nunca usa creativo. Al salir, se restaura el modo de juego anterior.

Mientras el modo editor está activo, abre `Tienda` desde el editor para comprar materiales. Los precios iniciales son: piedra x16 (20), tierra x16 (12), césped x16 (24), roca x16 (16), cristal x16 (24), tronco de roble x8 (20), hojas de roble x16 (12), piedra luminosa x8 (32) y cubo de agua (40). Cada compra descuenta energía de la máquina vinculada; si el inventario está lleno, la energía se devuelve. El servidor valida todas las compras y la tienda nunca cambia al modo creativo.

El panel de `Créditos de traducción` tiene un diseño compacto inspirado en menús de sandbox como WorldBox, con tarjetas por idioma, desplazamiento con la rueda del ratón y el crédito visible. Para atribuir una traducción a una persona concreta, cambia la clave `credits.pocketdimension.author.<idioma>` del archivo de idioma correspondiente.

## Crowdin comunitario

El proyecto incluye [`crowdin.yml`](crowdin.yml) y `translation_credits.json` como base para subir los idiomas a Crowdin. La API v2 de Crowdin requiere autorización, por eso el cliente del mod usa una distribución CDN pública de solo lectura. Crowdin documenta estas distribuciones como archivos accesibles directamente por URL.

Después de crear y publicar una distribución en Crowdin, configura en `config/pocketdimension-client.toml`:

```toml
[community]
translationApiUrl = "https://distributions.crowdin.net/95af9b78fd3175bd3b60699o28i/content/es-EC/translation_credits.json"
translationApiTimeoutMs = 3500
```

El archivo remoto debe devolver uno de estos formatos:

```json
{
  "credits": {
    "es_es": { "language": "Spanish", "author": "Nombre", "status": "Community translation" }
  }
}
```

Al abrir los créditos, el mod descarga la lista en segundo plano, la guarda en caché y vuelve a la lista local si Crowdin no responde.

Para regenerar el bolsillo con los nuevos valores usa `/pocketdim rebuild` como OP; el comando reconstruye el mapa del jugador y puede reemplazar terreno dentro del área del bolsillo.

## Compilar

1. Instala JDK 8.
2. Descarga Forge MDK 1.16.5 versión 36.2.42.
3. Copia estos archivos dentro de la carpeta del MDK o usa este proyecto como base.
4. Ejecuta:

```bat
gradlew genIntellijRuns
gradlew build
```

El JAR saldrá en:

```txt
build/libs/pocketdimension-1.0.3.jar
```

## Distribución protegida

El código Java no puede quedar cifrado de forma absoluta en un mod que Minecraft debe ejecutar, pero la distribución puede ofuscar nombres de clases y métodos y retirar metadatos de depuración. El JAR normal se conserva para desarrollo y el JAR protegido se genera con:

```bat
gradlew obfuscateRelease
```

Resultado:

```txt
build/libs/pocketdimension-1.0.3.jar
```

La copia ofuscada conserva el punto de entrada de Forge y las anotaciones necesarias para EventBus/JEI. El archivo de mapeo queda solo en `build/obfuscation/` y no se incluye en el JAR distribuible.

## Licencia del código fuente

El código original de Pocket Dimension se publica en GitHub únicamente para
consulta y lectura. No se permite copiarlo, modificarlo, hacer forks ni crear
derivados. La redistribución del código fuente requiere autorización escrita.

Se permite subir una copia sin modificar del JAR oficial si se conserva el
nombre del autor **Fosder** y se incluyen la [página oficial de GitHub](https://github.com/Tylerpro09/pocket-dimension) y la [página oficial de CurseForge](https://www.curseforge.com/minecraft/mc-mods/pockets-dimension).

La licencia propia del mod está en
[`LICENSE`](LICENSE) y en su copia descriptiva
[`LICENSE-POCKET-DIMENSION.txt`](LICENSE-POCKET-DIMENSION.txt). El archivo
`LICENSE.txt` conserva los avisos y licencias de Forge y de otros componentes
de terceros, que siguen sujetos a sus respectivas licencias.

Los JAR oficiales compilados sí pueden descargarse y utilizarse desde los
canales autorizados del proyecto, incluidos CurseForge y servicios que lo
obtengan desde CurseForge, como Aternos.

## Nota importante

La parte de dimensión real en 1.16.5 puede requerir ajustes según tu entorno Forge/Gradle. Si la dimensión no carga, revisa que el archivo exista aquí:

```txt
src/main/resources/data/pocketdimension/dimension/pocket_world.json
```

## Siguiente mejora recomendada

- Crear GUI real para la Mesa Dimensional.
- Selector de objetivo para la Llave Hacker.
- Bloquear invasión si el propietario objetivo tiene OP.
- Sistema de cofres blindados y registro de robos.

# Build Forge

Este repositorio genera un único JAR oficial:

- Forge 1.16.5: `gradlew.bat build`

La publicación de este repositorio es únicamente Forge 1.16.5. La implementación
Fabric histórica no forma parte del build ni recibe soporte.

La versión Forge mantiene el aviso normal de funciones experimentales de Minecraft; el mod no modifica ni omite esa pantalla.
