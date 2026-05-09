package com.exempleclasse;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Pantalla d'inici (Splash Screen) del joc Carretera Boja.
 * Es mostra durant 3 segons amb el títol del joc, els cotxes i una barra de progrés.
 * Desprès transita automàticament al menú principal.
 * Dissenyada per a pantalla vertical (480 × 800 px).
 */
public class PantallaInici implements Screen {

    private final Main joc;

    private static final float DURACIO_SPLASH = 3f;
    private float tempsTranscorregut = 0f;

    // Textures locals (alliberades a dispose)
    private Texture texturaFerrari;
    private Texture texturaRival;
    // Textura d'un sol píxel blanc per dibuixar la barra de progrés
    private Texture texturaPixel;

    private final GlyphLayout layout = new GlyphLayout();

    public PantallaInici(Main joc) {
        this.joc = joc;
        texturaFerrari = new Texture(Gdx.files.internal("ferrari.png"));
        texturaRival   = new Texture(Gdx.files.internal("cotxeRival.png"));

        // Creem un píxel blanc per usar com a rectangle sòlid (barra de progrés, etc.)
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        texturaPixel = new Texture(pm);
        pm.dispose();
    }

    @Override
    public void show() {
        tempsTranscorregut = 0f;
    }

    @Override
    public void render(float delta) {
        tempsTranscorregut += delta;
        ScreenUtils.clear(Color.BLACK);

        joc.viewport.apply();
        joc.batch.setProjectionMatrix(joc.viewport.getCamera().combined);

        joc.batch.begin();

        // --- Títol en dues línies (BeatMark gran, groc) ---
        joc.fontTitol.setColor(Color.YELLOW);
        layout.setText(joc.fontTitol, "CARRETERA");
        joc.fontTitol.draw(joc.batch, layout,
            (Main.AMPLADA - layout.width) / 2f, Main.ALCADA - 60f);

        layout.setText(joc.fontTitol, "BOJA");
        joc.fontTitol.draw(joc.batch, layout,
            (Main.AMPLADA - layout.width) / 2f, Main.ALCADA - 130f);

        // --- Cotxe del jugador centrat, proporcions reals ---
        float ratioFerrari = (float) texturaFerrari.getHeight() / texturaFerrari.getWidth();
        float ampFerrari   = 90f;
        float altFerrari   = ampFerrari * ratioFerrari;
        float xFerrari     = (Main.AMPLADA - ampFerrari) / 2f;
        float yFerrari     = Main.ALCADA / 2f - altFerrari / 2f + 40f;
        joc.batch.draw(texturaFerrari, xFerrari, yFerrari, ampFerrari, altFerrari);

        // --- Cotxes rivals als costats, proporcions reals ---
        float ratioRival = (float) texturaRival.getHeight() / texturaRival.getWidth();
        float ampRival   = 72f;
        float altRival   = ampRival * ratioRival;
        float yRival     = yFerrari + (altFerrari - altRival) / 2f; // Alineat verticalment
        joc.batch.draw(texturaRival, xFerrari - ampRival - 22f, yRival, ampRival, altRival);
        joc.batch.draw(texturaRival, xFerrari + ampFerrari + 22f, yRival, ampRival, altRival);

        // --- Text "Carregant" parpellejant ---
        if (tempsTranscorregut % 1f < 0.7f) {
            joc.fontPunts.setColor(Color.LIGHT_GRAY);
            layout.setText(joc.fontPunts, "Carregant el joc...");
            joc.fontPunts.draw(joc.batch, layout,
                (Main.AMPLADA - layout.width) / 2f, 130f);
        }

        // --- Barra de progrés (fons gris fosc + ompliment groc) ---
        float margeBar  = 40f;
        float ampTotalBar = Main.AMPLADA - margeBar * 2;
        float altBar    = 10f;
        float yBar      = 80f;

        // Fons de la barra
        joc.batch.setColor(0.25f, 0.25f, 0.25f, 1f);
        joc.batch.draw(texturaPixel, margeBar, yBar, ampTotalBar, altBar);

        // Ompliment proporcional al temps transcorregut
        float progres = tempsTranscorregut / DURACIO_SPLASH;
        joc.batch.setColor(Color.YELLOW);
        joc.batch.draw(texturaPixel, margeBar, yBar, ampTotalBar * progres, altBar);

        joc.batch.setColor(Color.WHITE);

        joc.batch.end();

        // Transitem al menú quan ha passat la durada
        if (tempsTranscorregut >= DURACIO_SPLASH) {
            joc.setScreen(new PantallaMenu(joc));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        joc.viewport.update(width, height, true);
    }

    @Override public void pause()  { }
    @Override public void resume() { }
    @Override public void hide()   { }

    @Override
    public void dispose() {
        if (texturaFerrari != null) texturaFerrari.dispose();
        if (texturaRival   != null) texturaRival.dispose();
        if (texturaPixel   != null) texturaPixel.dispose();
    }
}
