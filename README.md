# Carretera Boja

Joc de curses d'esquivar obstacles inspirat en el classic **Road Fighter** (Konami, 1984), desenvolupat amb el framework [libGDX](https://libgdx.com/) per a Android.

El jugador condueix un Ferrari per una carretera plena de cotxes rivals que venen de cara. L'objectiu es sobreviure el maxim temps possible esquivant els vehicles enemics, acumulant punts i gestionant les vides.

---

## Captures de pantalla conceptuals

```
  +--------------+      +--------------+      +--------------+
  |  CARRETERA   |      |   Carretera  |      |  GAME OVER   |
  |    BOJA      |      |  (scroll)    |      |              |
  |              |      |  PUNTS: 142  |      |  Puntuacio   |
  |  [Ferrari]   |      |   VIDES: 3   |      |  final: 247  |
  |              |      |   [Rivals]   |      |              |
  |  Carregant.. |      |  [Ferrari]   |      | Toca per     |
  |  [========]  |      |              |      | tornar       |
  +--------------+      +--------------+      +--------------+
   Splash Screen          Pantalla Joc          Game Over
```

---

## Funcionalitats implementades

### Jugabilitat
- **Scroll vertical continu** de la carretera (dues copies del fons que es reposicionen ciclicament)
- **Cotxes rivals** que apareixen cada 2 segons en un dels 4 carrils disponibles i baixen cap al jugador
- **Control tactil** intuitu: tocar o arrossegar el dit per moure el Ferrari horitzontalment
- **Deteccio de col-lisions AABB** amb marges de tolerancia per a una experiencia justa

### Sistema de vides
- El jugador comenca amb **3 vides**
- **Perd una vida** en xocar amb un cotxe rival
- **Guanya una vida extra** cada vegada que la puntuacio arriba a un **multiple de 100** (100, 200, 300...)
- Periode d'**invulnerabilitat de 2 segons** despres de cada xoc (el cotxe parpelleja)
- Si les vides arriben a **0**, la partida acaba i es mostra la pantalla de Game Over

### Puntuacio
- S'acumula automaticament: **5 punts per segon** (1 punt cada 0,2 segons)
- Es mostra en temps real al canto superior esquerre del HUD
- Missatge visual **"+ 1 VIDA EXTRA!"** en verd quan s'atorga una vida per puntuacio

### So i musica
- **Musica de fons** (`musica.mp3`) reproduida en bucle al **30% del volum**
- **Efecte de so** (`impacte.wav`) en cada col-lisio amb un cotxe rival
- La musica es pausa/reprens automaticament quan l'app passa a segon pla

### Fonts personalitzades
- Tots els textos del joc utilitzen la font **BeatMark-Regular.ttf** (estil grafiti)
- Generada amb la llibreria **FreeType** de libGDX en dues mides:
  - 60 px per als titols
  - 36 px per a la puntuacio, vides i textos secundaris

### Efectes visuals
- **Flash vermell** semi-transparent durant 0,5 segons en cada col-lisio
- **Parpelleig del cotxe** del jugador durant el periode d'invulnerabilitat
- **Barra de progres** groga animada a la Splash Screen
- **Text parpellejant** als menus ("Toca per jugar", "Toca per tornar")
- **Cotxe capgirat** (rotat 180 graus) a la pantalla de Game Over

---

## Arquitectura del projecte

El projecte segueix el patro multi-pantalla de libGDX amb `Game` + `Screen`, actors de Scene2D i un `AssetManager` centralitzat.

### Diagrama de flux de pantalles

```
  PantallaInici (3s)  --->  PantallaMenu  --->  PantallaJoc
                               ^                    |
                               |                    v
                               +----  PantallaGameOver
```

### Estructura de classes

```
com.exempleclasse/
|
|-- Main.java                  Classe principal (extends Game)
|                               - SpriteBatch, FitViewport i fonts compartides
|                               - Genera BitmapFont amb FreeTypeFontGenerator
|                               - Inicia amb PantallaInici
|
|-- AssetDescriptors.java      Constants AssetDescriptor<T> per a tots els fitxers
|                               - carretera.jpg, ferrari.png, cotxeRival.png
|                               - impacte.wav, musica.mp3
|
|-- PantallaInici.java         Splash Screen (implements Screen)
|                               - Titol "CARRETERA BOJA" en dues linies
|                               - Mostra Ferrari + 2 rivals com a decoracio
|                               - Barra de progres animada (Pixmap 1x1 blanc)
|                               - Transicio automatica als 3 segons
|
|-- PantallaMenu.java          Menu principal (implements Screen)
|                               - Fons: carretera fosca (batch.setColor al 30%)
|                               - Instruccions del joc centrades
|                               - Gdx.input.justTouched() -> PantallaJoc
|
|-- PantallaJoc.java           Pantalla de joc (implements Screen)
|                               - Stage amb FitViewport 480x800
|                               - AssetManager propi amb finishLoading()
|                               - Scroll de carretera (2 copies, repositionament ciclic)
|                               - Logica de puntuacio, vides, invulnerabilitat
|                               - HUD: punts (esquerra), vides (dreta), avis vida (centre)
|                               - Flash vermell amb textura Pixmap 1x1
|                               - Musica al 30%, so d'impacte
|
|-- PantallaGameOver.java      Game Over (implements Screen)
|                               - Titol "GAME OVER" en vermell
|                               - Cotxe rotat 180 graus (batch.draw amb rotacio)
|                               - Puntuacio final + comentari motivador
|                               - Toca -> PantallaMenu
|
|-- CotxeJugador.java          Actor del jugador (extends Image)
|                               - Amplada fixa 72px, alcada proporcional a la textura
|                               - getRectangle() amb marges per a col-lisions justes
|                               - Posicio inicial: centrat, y=50
|
|-- CotxeRival.java            Actor rival (extends Image)
|                               - Amplada fixa 72px, alcada proporcional
|                               - 4 carrils predefinits: x = {40, 145, 250, 355}
|                               - act(): auto-remove si surt per sota
|                               - colisionaAmbJugador(): Rectangle.overlaps()
|
|-- GestorCotxesRivals.java    Gestor de rivals (extends Group)
|                               - Spawn cada 2 segons (TimeUtils.nanoTime)
|                               - Actions.moveTo() per moure cada rival cap avall (3,5s)
|                               - comprovarCollisio(): itera fills, elimina en cas de xoc
|
|-- GestorEntrada.java         Entrada tactil (implements InputProcessor)
|                               - touchDown + touchDragged -> moureCotxe()
|                               - camera.unproject() per convertir coordenades
|                               - MathUtils.clamp() per limitar dins la pantalla
```

---

## Assets

| Fitxer | Tipus | Descripcio |
|--------|-------|------------|
| `carretera.jpg` | Texture | Imatge de fons de la carretera (scroll vertical) |
| `ferrari.png` | Texture | Sprite del cotxe del jugador |
| `cotxeRival.png` | Texture | Sprite dels cotxes rivals |
| `impacte.wav` | Sound | Efecte de so en cada col-lisio |
| `musica.mp3` | Music | Musica de fons (bucle, 30% volum) |
| `BeatMark-Regular.ttf` | Font | Font estil grafiti per a tots els textos |

---

## Constants del joc

| Constant | Valor | Ubicacio | Descripcio |
|----------|-------|----------|------------|
| `AMPLADA` | 480 | Main.java | Amplada virtual de la pantalla (px) |
| `ALCADA` | 800 | Main.java | Alcada virtual de la pantalla (px) |
| `VELOCITAT_CARRETERA` | 180 | PantallaJoc.java | Velocitat de scroll del fons (px/s) |
| `INTERVAL_PUNT` | 0.2 | PantallaJoc.java | Interval entre punts (segons) |
| `VIDES_INICIALS` | 3 | PantallaJoc.java | Vides al comencar la partida |
| `DURACIO_INVULNERABILITAT` | 2.0 | PantallaJoc.java | Segons d'invulnerabilitat post-xoc |
| `DURACIO_FLASH` | 0.5 | PantallaJoc.java | Durada del flash vermell (segons) |
| `DURACIO_AVIS_VIDA` | 1.5 | PantallaJoc.java | Durada del missatge "+1 VIDA" (segons) |
| `INTERVAL_SPAWN_NS` | 2e9 | GestorCotxesRivals.java | Interval entre rivals (nanosegons = 2s) |
| `DURADA_TRAVESSIA` | 3.5 | GestorCotxesRivals.java | Temps que un rival tarda a creuar (segons) |
| `CARRILS` | {40,145,250,355} | CotxeRival.java | Posicions X dels 4 carrils |
| `AMPLADA` (cotxe) | 72 | CotxeJugador/Rival | Amplada de renderitzat dels cotxes (px) |
| `DURACIO_SPLASH` | 3.0 | PantallaInici.java | Durada de la pantalla d'inici (segons) |

---

## Tecnologies i llibreries

- **libGDX 1.13+** — Framework multiplataforma per a jocs 2D/3D
- **Scene2D** — Sistema d'actors (Image, Group, Stage) per gestionar entitats del joc
- **FreeType** (`gdx-freetype`) — Renderitzacio de fonts TrueType a qualsevol mida
- **AssetManager** — Carrega i gestio centralitzada d'assets amb tipatge segur
- **FitViewport** — Viewport que escala el mon virtual mantenint la relacio d'aspecte
- **Gradle** — Sistema de build amb moduls `core` i `android`

---

## Plataformes

- **`core`**: Modul principal amb tota la logica del joc, compartit per totes les plataformes.
- **`android`**: Plataforma mobil Android. Requereix Android SDK (minSdk 21, targetSdk 35).

L'orientacio de la pantalla es **portrait** (vertical), configurada a `AndroidManifest.xml`.

---

## Com compilar i executar

### Requisits previs
- Java JDK 8 o superior
- Android SDK amb API 35
- Android Studio (recomanat) o IntelliJ IDEA

### Compilar
```bash
./gradlew build
```

### Executar a un dispositiu Android connectat
```bash
./gradlew android:installDebug android:run
```

### Netejar
```bash
./gradlew clean
```

---

## Estructura de directoris

```
Carreteraboja/
├── android/                    Modul Android
│   ├── AndroidManifest.xml     Configuracio (portrait, OpenGL ES 2.0)
│   ├── build.gradle            Deps Android + natives (gdx-freetype-platform)
│   └── src/main/java/          AndroidLauncher.java
├── assets/                     Assets compartits (textures, sons, fonts)
│   ├── BeatMark-Regular.ttf
│   ├── carretera.jpg
│   ├── cotxeRival.png
│   ├── ferrari.png
│   ├── impacte.wav
│   └── musica.mp3
├── core/                       Modul principal
│   ├── build.gradle            Deps core (gdx, gdx-freetype, gdx-box2d)
│   └── src/main/java/com/exempleclasse/
│       ├── Main.java
│       ├── AssetDescriptors.java
│       ├── PantallaInici.java
│       ├── PantallaMenu.java
│       ├── PantallaJoc.java
│       ├── PantallaGameOver.java
│       ├── CotxeJugador.java
│       ├── CotxeRival.java
│       ├── GestorCotxesRivals.java
│       └── GestorEntrada.java
├── build.gradle                Build principal (appName = 'Carretera Boja')
├── settings.gradle             Moduls: android, core
└── gradle.properties           Versions de gdx, kotlin, etc.
```

---

## Patrons de disseny utilitzats

| Patro | On s'aplica | Descripcio |
|-------|-------------|------------|
| **Game + Screen** | Main.java + Pantalla*.java | Gestio de multiples pantalles amb cicle de vida |
| **Actor / Group** | CotxeJugador, CotxeRival, GestorCotxesRivals | Jerarquia d'actors de Scene2D |
| **Observer** | GestorEntrada (InputProcessor) | Processament d'events d'entrada |
| **Descriptor centralitzat** | AssetDescriptors.java | Referencies tipades als assets |
| **Flyweight** | GestorCotxesRivals | Tots els rivals comparteixen la mateixa instancia de Texture |

---

## Autor

Projecte academic desenvolupat com a practica de programacio de jocs amb libGDX.
