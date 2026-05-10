# Guia de preparacio examen practic - Carretera Boja

## Mapa de fitxers del projecte

```
core/src/main/java/com/exempleclasse/
  Main.java              → Classe principal (Game), fonts FreeType, viewport 480x800
  AssetDescriptors.java  → Noms centralitzats dels assets
  PantallaInici.java     → Splash Screen (3 segons)
  PantallaMenu.java      → Menu principal (toca per jugar)
  PantallaJoc.java       → Logica del joc (scroll, col·lisions, HUD, musica)
  PantallaGameOver.java  → Pantalla Game Over amb puntuacio final
  CotxeJugador.java      → Actor Image del jugador (Ferrari)
  CotxeRival.java        → Actor Image d'un cotxe rival
  GestorCotxesRivals.java→ Group que genera i gestiona rivals
  GestorEntrada.java     → InputProcessor (touchDown + touchDragged)
```

---

## Constants clau que has de coneixer

| Constant | Fitxer | Valor | Que fa |
|----------|--------|-------|--------|
| `AMPLADA / ALCADA` | Main.java | 480 / 800 | Mida virtual de la pantalla |
| `VELOCITAT_CARRETERA` | PantallaJoc.java | 180f | Velocitat scroll fons (px/s) |
| `INTERVAL_PUNT` | PantallaJoc.java | 0.2f | Cada 0.2s suma 1 punt |
| `VIDES_INICIALS` | PantallaJoc.java | 3 | Vides al principi |
| `DURACIO_INVULNERABILITAT` | PantallaJoc.java | 2f | Segons d'invulnerabilitat |
| `INTERVAL_SPAWN_NS` | GestorCotxesRivals.java | 2_000_000_000L | Interval entre rivals (2s) |
| `DURADA_TRAVESSIA` | GestorCotxesRivals.java | 3.5f | Segons per creuar pantalla |
| `CARRILS` | CotxeRival.java | {40,145,250,355} | Posicions X dels 4 carrils |

---

## PROVA 1 — Dificultat progressiva (velocitat augmenta amb el temps)

**Probabilitat alta.** Es la modificacio mes classica d'un joc d'esquivar.

### Que cal fer
A mesura que la puntuacio puja, els rivals apareixen mes rapid i es mouen mes rapid.

### Fitxer: `PantallaJoc.java`

Afegir una variable de dificultat que escala amb la puntuacio:

```java
// Afegir a les variables de la classe (linia ~69)
private float factorDificultat = 1.0f;
```

Dins de `actualitzarPuntuacio(float delta)`, just despres d'incrementar `punts`:

```java
// Recalculem la dificultat: cada 50 punts la velocitat puja un 10%
factorDificultat = 1.0f + (punts / 50) * 0.1f;
```

Aplicar-la al scroll de la carretera dins `actualitzarCarretera`:

```java
private void actualitzarCarretera(float delta) {
    float velocitatActual = VELOCITAT_CARRETERA * factorDificultat;
    carreteraY1 -= velocitatActual * delta;
    carreteraY2 -= velocitatActual * delta;
    // ... resta igual
}
```

### Fitxer: `GestorCotxesRivals.java`

Fer que el gestor rebi un factor de dificultat. Canviar `DURADA_TRAVESSIA` i `INTERVAL_SPAWN_NS` a no-finals:

```java
// Canviar les constants a variables
private float duradaTravessia = 3.5f;
private long intervalSpawn = 2_000_000_000L;

// Afegir un metode public per actualitzar la dificultat
public void setDificultat(float factor) {
    // Mes rapid = menys temps de travessia
    duradaTravessia = 3.5f / factor;
    // Menys interval entre spawns (minim 0.7 segons)
    long nouInterval = (long)(2_000_000_000L / factor);
    intervalSpawn = Math.max(700_000_000L, nouInterval);
}
```

Dins `generarCotxeRival()`, usar `duradaTravessia` en comptes de la constant.

Dins `PantallaJoc.actualitzarPuntuacio()`, cridar:

```java
gestorRivals.setDificultat(factorDificultat);
```

---

## PROVA 2 — Meta / linia d'arribada (la partida s'acaba als X punts)

**Probabilitat alta.** L'enunciat original diu "arriba a la meta".

### Que cal fer
Quan el jugador arriba a una puntuacio objectiu (per exemple 500), es mostra una pantalla de victoria.

### Fitxer: `PantallaJoc.java`

```java
// Afegir constant
private static final int PUNTS_META = 500;
```

Dins `actualitzarPuntuacio()`, despres d'incrementar `punts`:

```java
if (punts >= PUNTS_META) {
    joc.setScreen(new PantallaVictoria(joc, punts));
    return; // Important: sortim del metode
}
```

### Fitxer NOU: `PantallaVictoria.java`

Copiar `PantallaGameOver.java` i canviar:
- El titol de "GAME OVER" a "HAS GUANYAT!"
- El color de vermell a verd (`Color.GREEN`)
- El comentari motivador

```java
package com.exempleclasse;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;

public class PantallaVictoria implements Screen {

    private final Main joc;
    private final int puntuacioFinal;
    private final GlyphLayout layout = new GlyphLayout();
    private float tempsParpelleig = 0f;

    public PantallaVictoria(Main joc, int puntuacioFinal) {
        this.joc = joc;
        this.puntuacioFinal = puntuacioFinal;
    }

    @Override
    public void render(float delta) {
        tempsParpelleig += delta;
        ScreenUtils.clear(Color.BLACK);
        joc.viewport.apply();
        joc.batch.setProjectionMatrix(joc.viewport.getCamera().combined);
        joc.batch.begin();

        // Titol en verd
        joc.fontTitol.setColor(Color.GREEN);
        layout.setText(joc.fontTitol, "HAS GUANYAT!");
        joc.fontTitol.draw(joc.batch, layout,
            (Main.AMPLADA - layout.width) / 2f, Main.ALCADA - 60f);

        // Puntuacio
        joc.fontPunts.setColor(Color.YELLOW);
        layout.setText(joc.fontPunts, "Puntuacio: " + puntuacioFinal);
        joc.fontPunts.draw(joc.batch, layout,
            (Main.AMPLADA - layout.width) / 2f, Main.ALCADA / 2f);

        // Text parpellejant
        if (tempsParpelleig % 1f < 0.65f) {
            joc.fontPunts.setColor(Color.WHITE);
            layout.setText(joc.fontPunts, "Toca per tornar al menu");
            joc.fontPunts.draw(joc.batch, layout,
                (Main.AMPLADA - layout.width) / 2f, 130f);
        }
        joc.batch.end();

        if (Gdx.input.justTouched()) {
            joc.setScreen(new PantallaMenu(joc));
        }
    }

    @Override public void show() {}
    @Override public void resize(int w, int h) { joc.viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
```

---

## PROVA 3 — Objectes col·leccionables (bonus de punts)

**Probabilitat alta.** Afegir un item que apareix a la carretera i dona punts extres.

### Que cal fer
1. Crear un actor `ItemBonus.java` similar a `CotxeRival.java`
2. Crear un gestor `GestorItems.java` similar a `GestorCotxesRivals.java`
3. A `PantallaJoc`, comprovar col·lisio amb el jugador i sumar punts

### Fitxer NOU: `ItemBonus.java`

```java
package com.exempleclasse;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class ItemBonus extends Image {

    public static final float MIDA = 50f;

    public ItemBonus(Texture textura) {
        super(textura);
        setSize(MIDA, MIDA);
        // Posicio aleatoria dins la carretera
        float x = MathUtils.random(40f, Main.AMPLADA - 40f - MIDA);
        setPosition(x, Main.ALCADA + MIDA);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (getY() + getHeight() < 0) remove();
    }

    public boolean colisionaAmbJugador(CotxeJugador jugador) {
        Rectangle rect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        return rect.overlaps(jugador.getRectangle());
    }
}
```

### Fitxer NOU: `GestorItems.java`

```java
package com.exempleclasse;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Iterator;

public class GestorItems extends Group {

    private static final long INTERVAL_SPAWN = 5_000_000_000L; // cada 5 segons
    private final Texture textura;
    private long darrereSpawnTime;

    public GestorItems(Texture textura) {
        this.textura = textura;
        darrereSpawnTime = TimeUtils.nanoTime();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (TimeUtils.nanoTime() - darrereSpawnTime > INTERVAL_SPAWN) {
            darrereSpawnTime = TimeUtils.nanoTime();
            ItemBonus item = new ItemBonus(textura);
            item.addAction(Actions.moveTo(item.getX(), -item.getHeight() - 10f, 4f));
            addActor(item);
        }
    }

    public boolean recollirItem(CotxeJugador cotxe) {
        boolean recollit = false;
        Iterator<Actor> it = getChildren().iterator();
        while (it.hasNext()) {
            ItemBonus item = (ItemBonus) it.next();
            if (item.colisionaAmbJugador(cotxe)) {
                item.remove();
                recollit = true;
            }
        }
        return recollit;
    }
}
```

### A `AssetDescriptors.java`, afegir:

```java
public static final AssetDescriptor<Texture> bonus =
    new AssetDescriptor<>("bonus.png", Texture.class);
```

### A `PantallaJoc.java`:

```java
// Variable nova
GestorItems gestorItems;

// A show(), despres de crear gestorRivals:
gestorItems = new GestorItems(assetManager.get(AssetDescriptors.bonus));
stage.addActor(gestorItems);

// A carregarAssets():
assetManager.load(AssetDescriptors.bonus);

// Dins render(), despres de comprovar col·lisions amb rivals:
if (gestorItems.recollirItem(cotxeJugador)) {
    punts += 25; // Bonus de 25 punts
}
```

**IMPORTANT:** Necessitaras un fitxer `bonus.png` a la carpeta `assets/`.

---

## PROVA 4 — Pantalla de pausa

**Probabilitat mitja.** Demana coneixer el cicle de vida de Screen.

### Que cal fer
Afegir un boolea `pausat` a `PantallaJoc`. Quan l'usuari toca una zona (per exemple, el centre superior), es pausa/despausa.

### Fitxer: `PantallaJoc.java`

```java
// Nova variable
private boolean pausat = false;
```

Al principi de `render()`, despres de `ScreenUtils.clear`:

```java
// Comprovar si l'usuari vol pausar/despausar (tocant el centre superior)
if (Gdx.input.justTouched()) {
    Vector3 pos = stage.getCamera().unproject(
        new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
    // Si toca a la zona central superior (rectangle de pausa)
    if (pos.y > Main.ALCADA - 80f && pos.x > Main.AMPLADA / 2f - 60f
            && pos.x < Main.AMPLADA / 2f + 60f) {
        pausat = !pausat;
        if (pausat) musica.pause();
        else musica.play();
    }
}

// Si estem pausats, nomes dibuixem i no actualitzem
if (pausat) {
    // Dibuixar fons i actors sense actualitzar
    stage.getViewport().apply();
    stage.getBatch().setProjectionMatrix(stage.getCamera().combined);
    stage.getBatch().begin();
    stage.getBatch().draw(texturaCarretera, 0, carreteraY1, Main.AMPLADA, Main.ALCADA);
    stage.getBatch().draw(texturaCarretera, 0, carreteraY2, Main.AMPLADA, Main.ALCADA);
    stage.getBatch().end();
    stage.draw(); // Dibuixa sense act()

    // Overlay semi-transparent
    stage.getBatch().begin();
    stage.getBatch().setColor(0, 0, 0, 0.5f);
    stage.getBatch().draw(texturaFlash, 0, 0, Main.AMPLADA, Main.ALCADA);
    stage.getBatch().setColor(Color.WHITE);

    joc.fontTitol.setColor(Color.WHITE);
    layout.setText(joc.fontTitol, "PAUSA");
    joc.fontTitol.draw(stage.getBatch(), layout,
        (Main.AMPLADA - layout.width) / 2f, Main.ALCADA / 2f + 30f);
    stage.getBatch().end();
    return; // No processem la resta del render
}
```

**Nota:** Caldra importar `com.badlogic.gdx.math.Vector3` a PantallaJoc.

---

## PROVA 5 — Guardar millor puntuacio (Preferences)

**Probabilitat mitja-alta.** LibGDX te `Preferences` per guardar dades persistents.

### Fitxer: `PantallaJoc.java`

Quan el jugador perd, guardem el record si es supera:

```java
// Dins el bloc on vides <= 0, ABANS de setScreen:
import com.badlogic.gdx.Preferences;

Preferences prefs = Gdx.app.getPreferences("CarreteraBoja");
int record = prefs.getInteger("record", 0);
if (punts > record) {
    prefs.putInteger("record", punts);
    prefs.flush(); // Guardar a disc
}
joc.setScreen(new PantallaGameOver(joc, punts));
```

### Fitxer: `PantallaMenu.java` o `PantallaGameOver.java`

Per mostrar el record:

```java
// Dins render(), on vulguis mostrar-lo:
import com.badlogic.gdx.Preferences;

Preferences prefs = Gdx.app.getPreferences("CarreteraBoja");
int record = prefs.getInteger("record", 0);

joc.fontPunts.setColor(Color.YELLOW);
layout.setText(joc.fontPunts, "Record: " + record);
joc.fontPunts.draw(joc.batch, layout,
    (Main.AMPLADA - layout.width) / 2f, 60f);
```

---

## PROVA 6 — Mostrar les vides amb icones (imatges) en comptes de text

**Probabilitat mitja.** Es un exercici classic de HUD grafic.

### Fitxer: `PantallaJoc.java`

Necessitaras una textura de cor o vida (o reutilitzar `ferrari.png` en petit).

A la seccio del HUD, substituir el text "VIDES: X" per imatges:

```java
// Substituir la linia "VIDES: X" per:
Texture texturaVida = assetManager.get(AssetDescriptors.ferrari); // o una icona de cor
float midaIcona = 28f;
float ratioVida = (float) texturaVida.getHeight() / texturaVida.getWidth();
float altIcona = midaIcona * ratioVida;

for (int i = 0; i < vides; i++) {
    stage.getBatch().draw(texturaVida,
        Main.AMPLADA - 40f - (i * (midaIcona + 5f)),
        Main.ALCADA - 10f - altIcona,
        midaIcona, altIcona);
}
```

Si tens un fitxer `cor.png` a assets, afegir a `AssetDescriptors`:

```java
public static final AssetDescriptor<Texture> cor =
    new AssetDescriptor<>("cor.png", Texture.class);
```

---

## PROVA 7 — Comptador de cotxes esquivats

**Probabilitat mitja.** Senzill pero demostra comprensio del flux del joc.

### Que cal fer
Comptar quants cotxes rivals surten per la part inferior sense xocar i mostrar-ho al HUD.

### Fitxer: `GestorCotxesRivals.java`

Afegir un comptador:

```java
private int cotxesEsquivats = 0;

public int getCotxesEsquivats() { return cotxesEsquivats; }

public void incrementarEsquivats() { cotxesEsquivats++; }
```

### Fitxer: `CotxeRival.java`

Modificar `act()` per notificar el gestor quan el cotxe surt per sota:

```java
@Override
public void act(float delta) {
    super.act(delta);
    if (getY() + getHeight() < 0) {
        // El cotxe ha sortit per sota -> ha estat esquivat
        if (getParent() instanceof GestorCotxesRivals) {
            ((GestorCotxesRivals) getParent()).incrementarEsquivats();
        }
        remove();
    }
}
```

### Fitxer: `PantallaJoc.java`

Mostrar al HUD:

```java
// A la seccio del HUD (dins el bloc stage.getBatch().begin/end):
joc.fontPunts.setColor(Color.CYAN);
joc.fontPunts.draw(stage.getBatch(),
    "Esquivats: " + gestorRivals.getCotxesEsquivats(),
    12f, Main.ALCADA - 50f);
```

---

## PROVA 8 — Afegir un segon tipus de rival (mes gran o mes rapid)

**Probabilitat mitja.** Demostra polimorfisme i diversitat de gameplay.

### Que cal fer
Crear un `CotxeRivalRapid.java` que hereti de `Image` directament, amb una mida o velocitat diferent.

### Fitxer NOU: `CotxeRivalRapid.java`

```java
package com.exempleclasse;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.math.Rectangle;

public class CotxeRivalRapid extends Image {

    public static final float AMPLADA = 60f; // Mes estret

    private static final float[] CARRILS = { 40f, 145f, 250f, 355f };

    public CotxeRivalRapid(Texture textura) {
        super(textura);
        float ratio = (float) textura.getHeight() / textura.getWidth();
        setSize(AMPLADA, AMPLADA * ratio);
        float xCarril = CARRILS[MathUtils.random(0, CARRILS.length - 1)];
        setPosition(xCarril, Main.ALCADA + getHeight());
        // Color diferent per distingir-lo (vermell)
        setColor(1f, 0.3f, 0.3f, 1f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (getY() + getHeight() < 0) remove();
    }

    public boolean colisionaAmbJugador(CotxeJugador jugador) {
        float marge = 6f;
        Rectangle rect = new Rectangle(
            getX() + marge, getY() + marge,
            getWidth() - marge * 2, getHeight() - marge * 2);
        return rect.overlaps(jugador.getRectangle());
    }
}
```

### Fitxer: `GestorCotxesRivals.java`

A `generarCotxeRival()`, decidir aleatoriament quin tipus crear:

```java
private void generarCotxeRival() {
    if (MathUtils.random(0, 3) == 0) {
        // 25% de probabilitat: cotxe rapid
        CotxeRivalRapid rapid = new CotxeRivalRapid(textura);
        rapid.addAction(Actions.moveTo(rapid.getX(), -rapid.getHeight() - 10f, 2.0f));
        addActor(rapid);
    } else {
        // 75% de probabilitat: cotxe normal
        CotxeRival rival = new CotxeRival(textura);
        rival.addAction(Actions.moveTo(rival.getX(), -rival.getHeight() - 10f, DURADA_TRAVESSIA));
        addActor(rival);
    }
}
```

A `comprovarCollisio`, comprovar tambe el nou tipus:

```java
public boolean comprovarCollisio(CotxeJugador cotxeJugador) {
    boolean hiHaCollisio = false;
    Iterator<Actor> it = getChildren().iterator();
    while (it.hasNext()) {
        Actor actor = it.next();
        boolean colisio = false;

        if (actor instanceof CotxeRival) {
            colisio = ((CotxeRival) actor).colisionaAmbJugador(cotxeJugador);
        } else if (actor instanceof CotxeRivalRapid) {
            colisio = ((CotxeRivalRapid) actor).colisionaAmbJugador(cotxeJugador);
        }

        if (colisio) {
            actor.remove();
            hiHaCollisio = true;
        }
    }
    return hiHaCollisio;
}
```

Caldra importar `MathUtils` al GestorCotxesRivals.

---

## PROVA 9 — Afegir un so al guanyar una vida extra

**Probabilitat alta (facil).** Exercici simple de Sound.

### Fitxer: `AssetDescriptors.java`

```java
public static final AssetDescriptor<Sound> vidaExtra =
    new AssetDescriptor<>("vida_extra.wav", Sound.class);
```

### Fitxer: `PantallaJoc.java`

```java
// Nova variable
private Sound soVidaExtra;

// A show(), despres de carregar assets:
soVidaExtra = assetManager.get(AssetDescriptors.vidaExtra);

// A carregarAssets():
assetManager.load(AssetDescriptors.vidaExtra);

// A actualitzarPuntuacio(), dins el bloc if (nousMultiples > vitesPerPunts):
vides++;
vitesPerPunts = nousMultiples;
tempsAvisVida = DURACIO_AVIS_VIDA;
soVidaExtra.play(); // <-- AFEGIR AQUI
```

**IMPORTANT:** Necessitaras un fitxer `vida_extra.wav` a la carpeta `assets/`.

---

## PROVA 10 — Pantalla de seleccio de cotxe

**Probabilitat baixa-mitja.** Mes llarg, pero demostra comprensio de Screen i Actor.

### Que cal fer
Crear una `PantallaSeleccio.java` que mostri 2-3 cotxes i l'usuari toqui per escollir. Es guarda la textura seleccionada i es passa a `PantallaJoc`.

### Fitxer NOU: `PantallaSeleccio.java`

```java
package com.exempleclasse;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

public class PantallaSeleccio implements Screen {
    private final Main joc;
    private Texture[] textures;
    private String[] noms = {"ferrari.png", "cotxeRival.png"}; // o altres
    private GlyphLayout layout = new GlyphLayout();

    public PantallaSeleccio(Main joc) {
        this.joc = joc;
        textures = new Texture[noms.length];
        for (int i = 0; i < noms.length; i++) {
            textures[i] = new Texture(Gdx.files.internal(noms[i]));
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        joc.viewport.apply();
        joc.batch.setProjectionMatrix(joc.viewport.getCamera().combined);
        joc.batch.begin();

        // Titol
        joc.fontTitol.setColor(Color.YELLOW);
        layout.setText(joc.fontTitol, "ESCULL COTXE");
        joc.fontTitol.draw(joc.batch, layout,
            (Main.AMPLADA - layout.width) / 2f, Main.ALCADA - 60f);

        // Dibuixar els cotxes
        float x = 60f;
        for (int i = 0; i < textures.length; i++) {
            float ratio = (float) textures[i].getHeight() / textures[i].getWidth();
            float amp = 100f;
            float alt = amp * ratio;
            joc.batch.draw(textures[i], x, Main.ALCADA / 2f - alt / 2f, amp, alt);
            x += amp + 80f;
        }
        joc.batch.end();

        // Detectar quin cotxe ha tocat
        if (Gdx.input.justTouched()) {
            Vector3 pos = joc.viewport.getCamera().unproject(
                new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

            float checkX = 60f;
            for (int i = 0; i < textures.length; i++) {
                float amp = 100f;
                if (pos.x >= checkX && pos.x <= checkX + amp) {
                    // L'usuari ha escollit el cotxe i
                    joc.setScreen(new PantallaJoc(joc, noms[i]));
                    dispose();
                    return;
                }
                checkX += amp + 80f;
            }
        }
    }

    @Override public void show() {}
    @Override public void resize(int w, int h) { joc.viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        for (Texture t : textures) {
            if (t != null) t.dispose();
        }
    }
}
```

Caldra modificar el constructor de `PantallaJoc` per acceptar un nom de textura:

```java
private String texturaJugadorNom;

public PantallaJoc(Main joc, String texturaJugadorNom) {
    this.joc = joc;
    this.texturaJugadorNom = texturaJugadorNom;
    // ... resta del constructor igual
}
```

I a `show()`:
```java
Texture texturaJugador = new Texture(Gdx.files.internal(texturaJugadorNom));
cotxeJugador = new CotxeJugador(texturaJugador);
```

I a `PantallaMenu`, canviar la linia `joc.setScreen(new PantallaJoc(joc))` per `joc.setScreen(new PantallaSeleccio(joc))`.

---

## PROVA 11 — Canviar el color del flash d'impacte o afegir vibracio

**Probabilitat alta (molt facil).** Canvi de 1-2 linies.

### Flash en un altre color (per exemple groc)

A `PantallaJoc.java`, linia del flash:

```java
// Canviar de:
stage.getBatch().setColor(1f, 0f, 0f, alpha); // Vermell
// A:
stage.getBatch().setColor(1f, 1f, 0f, alpha); // Groc
```

### Afegir vibracio (nomes Android)

```java
// Dins el bloc de col·lisio (on es fa soImpacte.play()):
Gdx.input.vibrate(200); // Vibrar 200 milisegons
```

**Nota:** La vibracio requereix el permis `VIBRATE` a `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.VIBRATE" />
```

---

## PROVA 12 — Canviar nombre de vides inicials o la regla del bonus

**Probabilitat molt alta (la mes facil).** Canvi d'una constant.

### Exemples de possibles enunciats:
- "El jugador comenca amb 5 vides" → Canviar `VIDES_INICIALS = 5`
- "Guanya una vida cada 200 punts" → Canviar `punts / 100` per `punts / 200`
- "Perd 2 vides en xocar" → Canviar `vides--` per `vides -= 2`
- "Maxim 5 vides" → Afegir `if (vides < 5)` abans de `vides++`

### Fitxer: `PantallaJoc.java`

Per limitar les vides a un maxim:
```java
if (nousMultiples > vitesPerPunts) {
    if (vides < 5) { // Maxim 5 vides
        vides++;
    }
    vitesPerPunts = nousMultiples;
    tempsAvisVida = DURACIO_AVIS_VIDA;
}
```

Per canviar el bonus a cada 200 punts:
```java
int nousMultiples = punts / 200; // Ara es cada 200 punts
```

Per perdre 2 vides:
```java
vides -= 2; // En comptes de vides--
```

---

## PROVA 13 — Afegir un temporitzador visible (countdown)

**Probabilitat mitja.** El joc dura X segons i si sobrevius, guanyes.

### Fitxer: `PantallaJoc.java`

```java
// Variables noves
private float tempsRestant = 60f; // 60 segons de partida
private static final float DURACIO_PARTIDA = 60f;
```

Dins render(), despres d'actualitzar puntuacio:

```java
tempsRestant -= delta;
if (tempsRestant <= 0) {
    // El jugador ha sobreviscut!
    joc.setScreen(new PantallaVictoria(joc, punts));
    return;
}
```

Per mostrar-ho al HUD:

```java
// Al centre de la part superior (dins el bloc stage.getBatch().begin/end)
joc.fontPunts.setColor(Color.YELLOW);
String tempsText = "TEMPS: " + (int) tempsRestant;
layout.setText(joc.fontPunts, tempsText);
joc.fontPunts.draw(stage.getBatch(), tempsText,
    (Main.AMPLADA - layout.width) / 2f, Main.ALCADA - 10f);
```

**Nota:** Necessites la classe `PantallaVictoria` (veure PROVA 2).

---

## PROVA 14 — Obstacles estatics a la carretera (cons o barreres)

**Probabilitat mitja.** Afegir obstacles fixes que NO es mouen, sino que baixen amb la carretera.

### Que cal fer
1. Crear un actor `Obstacle.java` que es mou amb el scroll de la carretera
2. Crear un gestor `GestorObstacles.java` similar a `GestorCotxesRivals.java`
3. A `PantallaJoc`, comprovar col·lisions amb el jugador

### Fitxer NOU: `Obstacle.java`

```java
package com.exempleclasse;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Actor que representa un obstacle estatic a la carretera (con, barrera, forat...).
 * Es mes petit que un cotxe rival i no te accio de moveTo propia:
 * es mou cap avall a la velocitat de la carretera.
 */
public class Obstacle extends Image {

    public static final float AMPLADA_OBS = 45f;
    public static final float ALCADA_OBS  = 45f;

    // Posicions X possibles (centrades als carrils)
    private static final float[] POSICIONS = { 55f, 160f, 265f, 370f };

    public Obstacle(Texture textura) {
        super(textura);
        setSize(AMPLADA_OBS, ALCADA_OBS);
        // Apareix en una posicio X aleatoria dins un carril
        float x = POSICIONS[MathUtils.random(0, POSICIONS.length - 1)];
        setPosition(x, Main.ALCADA + ALCADA_OBS);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // S'auto-elimina si surt per sota
        if (getY() + getHeight() < 0) {
            remove();
        }
    }

    /**
     * Comprova col·lisio amb el jugador (AABB, sense marges)
     */
    public boolean colisionaAmbJugador(CotxeJugador jugador) {
        Rectangle rect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        return rect.overlaps(jugador.getRectangle());
    }
}
```

### Fitxer NOU: `GestorObstacles.java`

```java
package com.exempleclasse;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Iterator;

/**
 * Gestor d'obstacles estatics. Genera obstacles periodicament
 * que baixen per la carretera a la mateixa velocitat que el fons.
 */
public class GestorObstacles extends Group {

    // Apareix un obstacle cada 4 segons
    private static final long INTERVAL_SPAWN = 4_000_000_000L;
    // Temps que tarda en baixar (similar a la carretera per coherencia visual)
    private static final float DURADA_BAIXADA = 4.5f;

    private final Texture textura;
    private long darrereSpawnTime;

    public GestorObstacles(Texture textura) {
        this.textura = textura;
        darrereSpawnTime = TimeUtils.nanoTime();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (TimeUtils.nanoTime() - darrereSpawnTime > INTERVAL_SPAWN) {
            darrereSpawnTime = TimeUtils.nanoTime();
            generarObstacle();
        }
    }

    private void generarObstacle() {
        Obstacle obs = new Obstacle(textura);
        // Movem l'obstacle cap avall amb una accio
        obs.addAction(Actions.moveTo(obs.getX(), -obs.getHeight() - 10f, DURADA_BAIXADA));
        addActor(obs);
    }

    /**
     * Comprova si algun obstacle ha xocat amb el jugador.
     * Retorna true si hi ha col·lisio (i elimina l'obstacle).
     */
    public boolean comprovarCollisio(CotxeJugador cotxeJugador) {
        boolean hiHaCollisio = false;
        Iterator<Actor> it = getChildren().iterator();
        while (it.hasNext()) {
            Obstacle obs = (Obstacle) it.next();
            if (obs.colisionaAmbJugador(cotxeJugador)) {
                obs.remove();
                hiHaCollisio = true;
            }
        }
        return hiHaCollisio;
    }
}
```

### Fitxer: `AssetDescriptors.java`

```java
public static final AssetDescriptor<Texture> obstacle =
    new AssetDescriptor<>("obstacle.png", Texture.class);
```

### Fitxer: `PantallaJoc.java`

Afegir la variable i integrar-la:

```java
// Nova variable
GestorObstacles gestorObstacles;

// A carregarAssets():
assetManager.load(AssetDescriptors.obstacle);

// A show(), despres de crear gestorRivals:
gestorObstacles = new GestorObstacles(assetManager.get(AssetDescriptors.obstacle));
stage.addActor(gestorObstacles);

// A render(), DESPRES de comprovar col·lisions amb rivals:
if (tempsInvulnerable <= 0 && gestorObstacles.comprovarCollisio(cotxeJugador)) {
    soImpacte.play();
    vides--;
    tempsInvulnerable = DURACIO_INVULNERABILITAT;
    tempsFlash        = DURACIO_FLASH;

    if (vides <= 0) {
        joc.setScreen(new PantallaGameOver(joc, punts));
        return;
    }
}
```

**IMPORTANT:** Necessitaras un fitxer `obstacle.png` a la carpeta `assets/` (pot ser una imatge d'un con, una barrera o un forat).

**Alternativa sense nou asset:** Pots reutilitzar la textura d'1 pixel blanc (`texturaFlash`) pintant-la de color taronja:

```java
// En comptes de carregar un asset, crear l'obstacle amb texturaFlash tenyida
gestorObstacles = new GestorObstacles(texturaFlash);
// I a Obstacle, afegir setColor(Color.ORANGE) al constructor
```

---

## PROVA 15 — Power-up d'escut (invulnerabilitat temporal recollible)

**Probabilitat mitja.** Combina col·leccionables (PROVA 3) amb el sistema d'invulnerabilitat existent.

### Que cal fer
1. Crear un actor `PowerUpEscut.java` similar a `ItemBonus.java`
2. Crear un gestor `GestorPowerUps.java`
3. Quan el jugador el recull, activar un periode d'invulnerabilitat (5 segons)
4. Mostrar un indicador visual al HUD quan l'escut esta actiu

### Fitxer NOU: `PowerUpEscut.java`

```java
package com.exempleclasse;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Power-up que atorga un periode d'invulnerabilitat al jugador.
 * Apareix a la carretera i el jugador l'ha de recollir tocant-lo.
 */
public class PowerUpEscut extends Image {

    public static final float MIDA = 45f;

    public PowerUpEscut(Texture textura) {
        super(textura);
        setSize(MIDA, MIDA);
        // Posicio X aleatoria dins la carretera
        float x = MathUtils.random(40f, Main.AMPLADA - 40f - MIDA);
        setPosition(x, Main.ALCADA + MIDA);
        // Color blau brillant per distingir-lo
        setColor(0.3f, 0.5f, 1f, 1f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (getY() + getHeight() < 0) remove();
    }

    public boolean colisionaAmbJugador(CotxeJugador jugador) {
        Rectangle rect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        return rect.overlaps(jugador.getRectangle());
    }
}
```

### Fitxer NOU: `GestorPowerUps.java`

```java
package com.exempleclasse;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Iterator;

/**
 * Gestor de power-ups d'escut. Genera un power-up cada 10 segons.
 */
public class GestorPowerUps extends Group {

    // Un power-up cada 10 segons
    private static final long INTERVAL_SPAWN = 10_000_000_000L;
    private final Texture textura;
    private long darrereSpawnTime;

    public GestorPowerUps(Texture textura) {
        this.textura = textura;
        darrereSpawnTime = TimeUtils.nanoTime();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (TimeUtils.nanoTime() - darrereSpawnTime > INTERVAL_SPAWN) {
            darrereSpawnTime = TimeUtils.nanoTime();
            PowerUpEscut pu = new PowerUpEscut(textura);
            pu.addAction(Actions.moveTo(pu.getX(), -pu.getHeight() - 10f, 5f));
            addActor(pu);
        }
    }

    /**
     * Comprova si el jugador ha recollit algun power-up.
     * @return true si s'ha recollit almenys un power-up
     */
    public boolean recollirPowerUp(CotxeJugador cotxe) {
        boolean recollit = false;
        Iterator<Actor> it = getChildren().iterator();
        while (it.hasNext()) {
            PowerUpEscut pu = (PowerUpEscut) it.next();
            if (pu.colisionaAmbJugador(cotxe)) {
                pu.remove();
                recollit = true;
            }
        }
        return recollit;
    }
}
```

### Fitxer: `AssetDescriptors.java`

```java
// Pots reutilitzar una textura existent o crear-ne una de nova
public static final AssetDescriptor<Texture> escut =
    new AssetDescriptor<>("escut.png", Texture.class);
```

### Fitxer: `PantallaJoc.java`

```java
// Noves variables
GestorPowerUps gestorPowerUps;
private float tempsEscut = 0f;
private static final float DURACIO_ESCUT = 5f; // 5 segons d'escut

// A carregarAssets():
assetManager.load(AssetDescriptors.escut);

// A show(), despres de crear gestorRivals:
gestorPowerUps = new GestorPowerUps(assetManager.get(AssetDescriptors.escut));
stage.addActor(gestorPowerUps);
```

A `render()`, DESPRES de `stage.act(delta)` i `stage.draw()`:

```java
// Comprovar si el jugador recull un power-up
if (gestorPowerUps.recollirPowerUp(cotxeJugador)) {
    tempsEscut = DURACIO_ESCUT; // Activar escut
}

// Actualitzar temporitzador de l'escut
if (tempsEscut > 0) {
    tempsEscut -= delta;
}
```

Modificar la comprovacio de col·lisions per tenir en compte l'escut:

```java
// Canviar la condicio existent de:
if (tempsInvulnerable <= 0 && gestorRivals.comprovarCollisio(cotxeJugador)) {
// A:
if (tempsInvulnerable <= 0 && tempsEscut <= 0
        && gestorRivals.comprovarCollisio(cotxeJugador)) {
```

Mostrar al HUD quan l'escut esta actiu:

```java
// Dins el bloc stage.getBatch().begin/end del HUD:
if (tempsEscut > 0) {
    joc.fontPunts.setColor(Color.CYAN);
    String textEscut = "ESCUT: " + (int) tempsEscut + "s";
    layout.setText(joc.fontPunts, textEscut);
    joc.fontPunts.draw(stage.getBatch(), textEscut,
        (Main.AMPLADA - layout.width) / 2f, Main.ALCADA - 50f);

    // Opcional: efecte visual al cotxe (contorn blau)
    cotxeJugador.setColor(0.5f, 0.8f, 1f, 1f);
} else {
    cotxeJugador.setColor(Color.WHITE); // Color normal
}
```

**IMPORTANT:** Necessitaras un fitxer `escut.png` a `assets/`. Alternativament, pots reutilitzar `texturaFlash` (el pixel blanc) amb `setColor(Color.CYAN)` al constructor del `PowerUpEscut`.

**Alternativa sense nou asset (reutilitzant texturaFlash):**

```java
// A show(), en comptes de carregar un asset nou:
gestorPowerUps = new GestorPowerUps(texturaFlash);
// El color ja es defineix al constructor de PowerUpEscut (blau)
```

---

## RESUM RAPID — Que estudiar per prioritat

| Prioritat | Prova | Tema | Fitxers que tocaras |
|-----------|-------|------|---------------------|
| **ALTA** | 12 | Canviar constants (vides, punts, velocitats) | PantallaJoc |
| **ALTA** | 11 | Canviar flash / afegir vibracio | PantallaJoc |
| **ALTA** | 9 | Afegir un so nou | AssetDescriptors + PantallaJoc |
| **ALTA** | 1 | Dificultat progressiva | PantallaJoc + GestorCotxesRivals |
| **ALTA** | 2 | Meta / victoria | PantallaJoc + NOU PantallaVictoria |
| **MITJA-ALTA** | 5 | Guardar record (Preferences) | PantallaJoc + PantallaGameOver |
| **MITJA** | 4 | Pantalla de pausa | PantallaJoc |
| **MITJA** | 13 | Temporitzador / countdown | PantallaJoc |
| **MITJA** | 3 | Objectes col·leccionables | NOU ItemBonus + GestorItems |
| **MITJA** | 6 | Vides amb icones | PantallaJoc |
| **MITJA** | 7 | Comptador d'esquivats | CotxeRival + GestorCotxesRivals |
| **MITJA** | 14 | Obstacles estatics | NOU Obstacle + GestorObstacles |
| **MITJA** | 15 | Power-up d'escut | NOU PowerUpEscut + GestorPowerUps |
| **BAIXA** | 8 | Segon tipus de rival | NOU CotxeRivalRapid |
| **BAIXA** | 10 | Seleccio de cotxe | NOU PantallaSeleccio |

---

## Conceptes clau que has de dominar

1. **Screen lifecycle:** `show()` es crida al fer `setScreen()`, `dispose()` allibera memoria
2. **AssetManager:** `load()` + `finishLoading()` + `get()` per obtenir assets
3. **Stage/Actor:** `stage.addActor()`, `stage.act(delta)`, `stage.draw()`
4. **Actions:** `Actions.moveTo(x, y, durada)` per moure actors automaticament
5. **Col·lisions:** `Rectangle.overlaps()` entre dos rectangles
6. **SpriteBatch:** `begin()`, `draw()`, `end()` — mai aniuar begin/end
7. **FreeType:** `FreeTypeFontGenerator` + `FreeTypeFontParameter` per crear fonts
8. **Preferences:** `Gdx.app.getPreferences("nom")` per guardar/llegir dades
9. **Sound vs Music:** Sound per efectes curts (`.play()`), Music per fons (`.setLooping`, `.setVolume`)
10. **GlyphLayout:** Per calcular l'amplada d'un text i centrar-lo: `layout.setText(font, text)` → `layout.width`

---

## Patro comu per afegir una entitat nova al joc

Moltes proves demanen crear un nou tipus d'objecte (bonus, obstacle, power-up). El patro es sempre el mateix:

### Pas 1: Crear la classe Actor

```java
public class NouActor extends Image {
    public NouActor(Texture textura) {
        super(textura);
        setSize(MIDA_X, MIDA_Y);
        setPosition(posicioAleatoria, Main.ALCADA + MIDA_Y);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (getY() + getHeight() < 0) remove();
    }

    public boolean colisionaAmbJugador(CotxeJugador jugador) {
        Rectangle rect = new Rectangle(getX(), getY(), getWidth(), getHeight());
        return rect.overlaps(jugador.getRectangle());
    }
}
```

### Pas 2: Crear el Gestor (Group)

```java
public class GestorNouActor extends Group {
    private static final long INTERVAL = 5_000_000_000L;
    private final Texture textura;
    private long darrereSpawnTime;

    public GestorNouActor(Texture textura) {
        this.textura = textura;
        darrereSpawnTime = TimeUtils.nanoTime();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (TimeUtils.nanoTime() - darrereSpawnTime > INTERVAL) {
            darrereSpawnTime = TimeUtils.nanoTime();
            NouActor actor = new NouActor(textura);
            actor.addAction(Actions.moveTo(actor.getX(), -actor.getHeight(), DURADA));
            addActor(actor);
        }
    }

    public boolean comprovar(CotxeJugador cotxe) {
        boolean trobat = false;
        Iterator<Actor> it = getChildren().iterator();
        while (it.hasNext()) {
            NouActor a = (NouActor) it.next();
            if (a.colisionaAmbJugador(cotxe)) {
                a.remove();
                trobat = true;
            }
        }
        return trobat;
    }
}
```

### Pas 3: Integrar a PantallaJoc

```java
// 1. Declarar variable
GestorNouActor gestorNou;

// 2. Carregar asset (carregarAssets)
assetManager.load(AssetDescriptors.nouAsset);

// 3. Crear i afegir a l'Stage (show)
gestorNou = new GestorNouActor(assetManager.get(AssetDescriptors.nouAsset));
stage.addActor(gestorNou);

// 4. Comprovar col·lisions (render)
if (gestorNou.comprovar(cotxeJugador)) {
    // Fer alguna cosa: sumar punts, restar vides, activar power-up...
}
```

### Pas 4: Declarar l'asset (AssetDescriptors)

```java
public static final AssetDescriptor<Texture> nouAsset =
    new AssetDescriptor<>("nou_asset.png", Texture.class);
```

**Memoritza aquest patro** — es la base de les proves 3, 8, 14 i 15.
