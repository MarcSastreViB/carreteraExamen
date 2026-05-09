package com.exempleclasse;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Pantalla principal del joc Carretera Boja.
 * Gestiona el bucle de joc: scroll de la carretera, moviment dels cotxes rivals,
 * col·lisions, puntuació, vides i sons.
 *
 * Sistema de vides:
 *   - Comença amb 3 vides
 *   - Perd una vida en xocar amb un cotxe rival
 *   - Guanya una vida extra cada múltiple de 100 punts
 *   - La partida acaba quan no queden vides
 */
public class PantallaJoc implements Screen {

    // -------------------------------------------------------------------------
    // Referència al joc principal
    // -------------------------------------------------------------------------
    final Main joc;

    // -------------------------------------------------------------------------
    // Escena (Stage) i gestor d'assets
    // -------------------------------------------------------------------------
    Stage       stage;
    AssetManager assetManager;

    // -------------------------------------------------------------------------
    // Actors del joc (visibles des de GestorEntrada)
    // -------------------------------------------------------------------------
    CotxeJugador     cotxeJugador;
    GestorCotxesRivals gestorRivals;

    // -------------------------------------------------------------------------
    // Assets carregats
    // -------------------------------------------------------------------------
    private Music   musica;
    private Sound   soImpacte;
    private Texture texturaCarretera;
    // Textura d'un sol píxel blanc per al flash vermell d'impacte
    private Texture texturaFlash;

    // -------------------------------------------------------------------------
    // Scroll de la carretera: dues còpies per a l'efecte de scroll continu
    // -------------------------------------------------------------------------
    private float carreteraY1;          // Posició Y de la primera còpia del fons
    private float carreteraY2;          // Posició Y de la segona còpia del fons
    private static final float VELOCITAT_CARRETERA = 180f; // píxels per segon

    // -------------------------------------------------------------------------
    // Sistema de puntuació
    // -------------------------------------------------------------------------
    private int   punts;
    private int   vitesPerPunts;        // Quants múltiples de 100 punts s'han acumulat
    private float cronometreScore;      // Temps acumulat per incrementar la puntuació
    private static final float INTERVAL_PUNT = 0.2f; // 1 punt cada 0,2 s = 5 punts/s

    // -------------------------------------------------------------------------
    // Sistema de vides
    // -------------------------------------------------------------------------
    private int   vides;
    private static final int VIDES_INICIALS = 3;

    // Període d'invulnerabilitat després d'una col·lisió (evita perdre múltiples vides)
    private float tempsInvulnerable;
    private static final float DURACIO_INVULNERABILITAT = 2f; // segons

    // -------------------------------------------------------------------------
    // Efecte visual d'impacte (flash vermell)
    // -------------------------------------------------------------------------
    private float tempsFlash;
    private static final float DURACIO_FLASH = 0.5f; // segons

    // Temporitzador per mostrar el missatge "+1 VIDA!" quan s'assoleix un múltiple de 100
    private float tempsAvisVida = 0f;
    private static final float DURACIO_AVIS_VIDA = 1.5f; // segons que es mostra l'avís

    // Per calcular amplades de text centrat
    private final GlyphLayout layout = new GlyphLayout();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    /**
     * Crea l'Stage i l'AssetManager. Els actors es creen a show()
     * perquè show() s'invoca cada cop que la pantalla es fa activa.
     * @param joc instància de Main
     */
    public PantallaJoc(Main joc) {
        this.joc = joc;

        assetManager = new AssetManager();

        // Creem la càmera i l'Stage amb viewport 800×480 (mode horitzontal)
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, Main.AMPLADA, Main.ALCADA);
        stage = new Stage(new FitViewport(Main.AMPLADA, Main.ALCADA, camera));
    }

    // -------------------------------------------------------------------------
    // Cicle de vida de la pantalla
    // -------------------------------------------------------------------------

    @Override
    public void show() {
        // Carreguem tots els assets de forma síncrona (bloquejant)
        carregarAssets();

        // Obtenim els assets ja carregats
        musica          = assetManager.get(AssetDescriptors.musica);
        soImpacte       = assetManager.get(AssetDescriptors.impacte);
        texturaCarretera = assetManager.get(AssetDescriptors.carretera);

        // Creem una textura d'un sol píxel blanc per al flash d'impacte
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        texturaFlash = new Texture(pixmap);
        pixmap.dispose();

        // Creem els actors del joc
        cotxeJugador = new CotxeJugador(assetManager.get(AssetDescriptors.ferrari));
        gestorRivals = new GestorCotxesRivals(assetManager.get(AssetDescriptors.cotxeRival));

        // Afegim els actors a l'Stage (l'ordre determina qui es dibuixa per sobre)
        stage.addActor(gestorRivals);
        stage.addActor(cotxeJugador);

        // Assignem el gestor d'entrada tàctil
        Gdx.input.setInputProcessor(new GestorEntrada(this));

        // Iniciem la música de fons al 30% de volum, en bucle
        musica.setLooping(true);
        musica.setVolume(0.3f);
        musica.play();

        // Inicialitzem les variables de joc
        reiniciarPartida();
    }

    /** Restableix totes les variables al estat inicial d'una partida nova */
    private void reiniciarPartida() {
        punts             = 0;
        vides             = VIDES_INICIALS;
        vitesPerPunts     = 0;
        cronometreScore   = 0f;
        tempsInvulnerable = 0f;
        tempsFlash        = 0f;
        tempsAvisVida     = 0f;

        // Les dues còpies del fons s'inicien: una a baix (y=0) i l'altra a sobre (y=ALCADA)
        carreteraY1 = 0f;
        carreteraY2 = Main.ALCADA;
    }

    // -------------------------------------------------------------------------
    // Render (bucle principal)
    // -------------------------------------------------------------------------

    @Override
    public void render(float delta) {
        // Netejem la pantalla
        ScreenUtils.clear(Color.BLACK);

        // Actualitzem els temporitzadors
        actualitzarCarretera(delta);
        actualitzarPuntuacio(delta);
        actualitzarInvulnerabilitat(delta);

        // === 1. DIBUIXAR EL FONS (CARRETERA) ===
        // Establim la matriu de projecció de l'Stage i dibuixem les dues còpies del fons
        stage.getViewport().apply();
        stage.getBatch().setProjectionMatrix(stage.getCamera().combined);
        stage.getBatch().begin();
        stage.getBatch().draw(texturaCarretera, 0, carreteraY1, Main.AMPLADA, Main.ALCADA);
        stage.getBatch().draw(texturaCarretera, 0, carreteraY2, Main.AMPLADA, Main.ALCADA);
        stage.getBatch().end();

        // === 2. ACTUALITZAR I DIBUIXAR ELS ACTORS (cotxes) ===
        // Parpelleig del cotxe del jugador durant la invulnerabilitat
        if (tempsInvulnerable > 0) {
            // Alternem la visibilitat 5 vegades per segon
            boolean visible = ((int)(tempsInvulnerable * 5)) % 2 == 0;
            cotxeJugador.getColor().a = visible ? 0.25f : 1f;
        } else {
            cotxeJugador.getColor().a = 1f;
        }

        stage.act(delta);
        stage.draw();

        // === 3. COMPROVAR COL·LISIONS (només si no estem en període d'invulnerabilitat) ===
        if (tempsInvulnerable <= 0 && gestorRivals.comprovarCollisio(cotxeJugador)) {
            soImpacte.play();
            vides--;
            tempsInvulnerable = DURACIO_INVULNERABILITAT;
            tempsFlash        = DURACIO_FLASH;

            // Si no queden vides, passem a la pantalla de Game Over
            if (vides <= 0) {
                joc.setScreen(new PantallaGameOver(joc, punts));
                return;
            }
        }

        // === 4. DIBUIXAR HUD I EFECTES VISUALS ===
        // Reutilitzem el batch de l'Stage; la matriu de projecció ja és correcta
        stage.getBatch().setColor(Color.WHITE); // Ens assegurem que el color és blanc
        stage.getBatch().begin();

        // Flash vermell en cas de col·lisió recent
        if (tempsFlash > 0) {
            tempsFlash -= delta;
            float alpha = Math.max(0f, tempsFlash / DURACIO_FLASH) * 0.55f;
            stage.getBatch().setColor(1f, 0f, 0f, alpha);
            stage.getBatch().draw(texturaFlash, 0, 0, Main.AMPLADA, Main.ALCADA);
            stage.getBatch().setColor(Color.WHITE);
        }

        // Puntuació (cantó superior esquerre)
        joc.fontPunts.setColor(Color.WHITE);
        joc.fontPunts.draw(stage.getBatch(), "PUNTS: " + punts, 12f, Main.ALCADA - 10f);

        // Vides (cantó superior dret)
        String textVides = "VIDES: " + vides;
        layout.setText(joc.fontPunts, textVides);
        joc.fontPunts.draw(stage.getBatch(), textVides,
            Main.AMPLADA - layout.width - 12f, Main.ALCADA - 10f);

        // Missatge "+ 1 VIDA!" quan s'ha guanyat una vida per puntuació
        if (tempsAvisVida > 0) {
            tempsAvisVida -= delta;
            joc.fontPunts.setColor(Color.GREEN);
            layout.setText(joc.fontPunts, "+ 1 VIDA EXTRA!");
            joc.fontPunts.draw(stage.getBatch(), layout,
                (Main.AMPLADA - layout.width) / 2f, Main.ALCADA / 2f + 40f);
        }

        stage.getBatch().end();
    }

    // -------------------------------------------------------------------------
    // Mètodes d'actualització interns
    // -------------------------------------------------------------------------

    /**
     * Desplaça les dues còpies de la carretera cap avall per crear l'efecte de scroll.
     * Quan una còpia surt per baix, es repositiona per sobre de l'altra.
     */
    private void actualitzarCarretera(float delta) {
        carreteraY1 -= VELOCITAT_CARRETERA * delta;
        carreteraY2 -= VELOCITAT_CARRETERA * delta;

        if (carreteraY1 < -Main.ALCADA) {
            carreteraY1 = carreteraY2 + Main.ALCADA;
        }
        if (carreteraY2 < -Main.ALCADA) {
            carreteraY2 = carreteraY1 + Main.ALCADA;
        }
    }

    /**
     * Incrementa la puntuació cada INTERVAL_PUNT segons.
     * Comprova si s'ha assolit un nou múltiple de 100 per atorgar una vida extra.
     */
    private void actualitzarPuntuacio(float delta) {
        cronometreScore += delta;
        while (cronometreScore >= INTERVAL_PUNT) {
            cronometreScore -= INTERVAL_PUNT;
            punts++;

            // Comprovem si hem superat un nou múltiple de 100 punts → vida extra
            int nousMultiples = punts / 100;
            if (nousMultiples > vitesPerPunts) {
                vides++;
                vitesPerPunts = nousMultiples;
                tempsAvisVida = DURACIO_AVIS_VIDA; // Activem el missatge d'avís
            }
        }
    }

    /**
     * Decrementem el temps d'invulnerabilitat si és positiu.
     */
    private void actualitzarInvulnerabilitat(float delta) {
        if (tempsInvulnerable > 0) {
            tempsInvulnerable -= delta;
        }
    }

    /**
     * Carrega tots els assets necessaris per al joc de forma síncrona.
     */
    private void carregarAssets() {
        assetManager.load(AssetDescriptors.carretera);
        assetManager.load(AssetDescriptors.ferrari);
        assetManager.load(AssetDescriptors.cotxeRival);
        assetManager.load(AssetDescriptors.impacte);
        assetManager.load(AssetDescriptors.musica);
        // Esperem fins que tots els assets s'hagin carregat completament
        assetManager.finishLoading();
    }

    // -------------------------------------------------------------------------
    // Cicle de vida addicional
    // -------------------------------------------------------------------------

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // Pausem la música quan l'app passa a segon pla
        if (musica != null) musica.pause();
    }

    @Override
    public void resume() {
        // Reprenguem la música quan l'app torna al primer pla
        if (musica != null) musica.play();
    }

    @Override
    public void hide() {
        // Aturem la música quan abandonem aquesta pantalla
        if (musica != null) musica.stop();
    }

    @Override
    public void dispose() {
        stage.dispose();
        assetManager.dispose();
        if (texturaFlash != null) texturaFlash.dispose();
    }
}
