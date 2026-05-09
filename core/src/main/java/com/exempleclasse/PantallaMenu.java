package com.exempleclasse;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Pantalla del menú principal del joc Carretera Boja.
 * Mostra el títol, els cotxes i les instruccions del joc.
 * En tocar la pantalla, inicia una nova partida.
 * Dissenyada per a pantalla vertical (480 × 800 px).
 */
public class PantallaMenu implements Screen {

    private final Main joc;

    private Texture texturaCarretera;
    private Texture texturaFerrari;
    private Texture texturaCotxeRival;

    private final GlyphLayout layout = new GlyphLayout();
    private float tempsParpelleig = 0f;

    public PantallaMenu(Main joc) {
        this.joc = joc;
        texturaCarretera  = new Texture(Gdx.files.internal("carretera.jpg"));
        texturaFerrari    = new Texture(Gdx.files.internal("ferrari.png"));
        texturaCotxeRival = new Texture(Gdx.files.internal("cotxeRival.png"));
    }

    @Override
    public void show() { }

    @Override
    public void render(float delta) {
        tempsParpelleig += delta;

        ScreenUtils.clear(Color.BLACK);
        joc.viewport.apply();
        joc.batch.setProjectionMatrix(joc.viewport.getCamera().combined);

        joc.batch.begin();

        // --- Fons: carretera fosca ---
        joc.batch.setColor(0.3f, 0.3f, 0.3f, 1f);
        joc.batch.draw(texturaCarretera, 0, 0, Main.AMPLADA, Main.ALCADA);
        joc.batch.setColor(Color.WHITE);

        // --- Títol en dues línies (BeatMark gran, groc) ---
        joc.fontTitol.setColor(Color.YELLOW);
        layout.setText(joc.fontTitol, "CARRETERA");
        joc.fontTitol.draw(joc.batch, layout,
            (Main.AMPLADA - layout.width) / 2f,
            Main.ALCADA - 50f);

        layout.setText(joc.fontTitol, "BOJA");
        joc.fontTitol.draw(joc.batch, layout,
            (Main.AMPLADA - layout.width) / 2f,
            Main.ALCADA - 120f);

        // --- Cotxe del jugador (centrat) ---
        float ratioF  = (float) texturaFerrari.getHeight()    / texturaFerrari.getWidth();
        float ratioR  = (float) texturaCotxeRival.getHeight() / texturaCotxeRival.getWidth();
        float ampF    = 90f;  float altF = ampF * ratioF;
        float ampR    = 72f;  float altR = ampR * ratioR;

        float xF = (Main.AMPLADA - ampF) / 2f;
        float yF = Main.ALCADA / 2f - altF / 2f + 60f;

        joc.batch.draw(texturaFerrari, xF, yF, ampF, altF);

        // --- Cotxes rivals als costats ---
        float yCentratRival = yF + (altF - altR) / 2f;
        joc.batch.draw(texturaCotxeRival, xF - ampR - 25f, yCentratRival, ampR, altR);
        joc.batch.draw(texturaCotxeRival, xF + ampF + 25f, yCentratRival, ampR, altR);

        // --- Text "Toca per jugar" parpellejant ---
        if (tempsParpelleig % 1f < 0.65f) {
            joc.fontPunts.setColor(Color.WHITE);
            layout.setText(joc.fontPunts, ">> Toca per jugar <<");
            joc.fontPunts.draw(joc.batch, layout,
                (Main.AMPLADA - layout.width) / 2f, yF - 60f);
        }

        // --- Instruccions (font punts, color gris clar) ---
        joc.fontPunts.setColor(Color.LIGHT_GRAY);

        layout.setText(joc.fontPunts, "Esquiva els cotxes rivals");
        joc.fontPunts.draw(joc.batch, layout,
            (Main.AMPLADA - layout.width) / 2f, 200f);

        layout.setText(joc.fontPunts, "100 punts = +1 vida");
        joc.fontPunts.draw(joc.batch, layout,
            (Main.AMPLADA - layout.width) / 2f, 155f);

        layout.setText(joc.fontPunts, "0 vides = fi de partida");
        joc.fontPunts.draw(joc.batch, layout,
            (Main.AMPLADA - layout.width) / 2f, 110f);

        joc.batch.end();

        // Toca la pantalla → comença la partida
        if (Gdx.input.justTouched()) {
            joc.setScreen(new PantallaJoc(joc));
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
        if (texturaCarretera  != null) texturaCarretera.dispose();
        if (texturaFerrari    != null) texturaFerrari.dispose();
        if (texturaCotxeRival != null) texturaCotxeRival.dispose();
    }
}
