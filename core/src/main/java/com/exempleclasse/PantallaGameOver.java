package com.exempleclasse;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Pantalla de fi de partida (Game Over) del joc Carretera Boja.
 * Mostra el missatge "GAME OVER", la puntuació final i permet tornar al menú.
 * Dissenyada per a pantalla vertical (480 × 800 px).
 */
public class PantallaGameOver implements Screen {

    private final Main joc;
    private final int  puntuacioFinal;

    private Texture texturaCotxe;

    private final GlyphLayout layout = new GlyphLayout();
    private float tempsParpelleig = 0f;

    /**
     * @param joc            instància de Main
     * @param puntuacioFinal punts acumulats durant la partida
     */
    public PantallaGameOver(Main joc, int puntuacioFinal) {
        this.joc            = joc;
        this.puntuacioFinal = puntuacioFinal;
        texturaCotxe = new Texture(Gdx.files.internal("ferrari.png"));
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

        // --- Títol "GAME OVER" en vermell ---
        joc.fontTitol.setColor(Color.RED);
        layout.setText(joc.fontTitol, "GAME OVER");
        joc.fontTitol.draw(joc.batch, layout,
            (Main.AMPLADA - layout.width) / 2f,
            Main.ALCADA - 60f);

        // --- Cotxe capgirat (simulant accident) ---
        float ratio    = (float) texturaCotxe.getHeight() / texturaCotxe.getWidth();
        float ampCotxe = 90f;
        float altCotxe = ampCotxe * ratio;
        float xCotxe   = (Main.AMPLADA - ampCotxe) / 2f;
        float yCotxe   = Main.ALCADA / 2f + 30f;

        // Dibuixem el cotxe rotat 180° per simular que ha quedat capgirat
        joc.batch.draw(
            texturaCotxe,
            xCotxe, yCotxe,
            ampCotxe / 2f, altCotxe / 2f,   // origen de rotació (centre)
            ampCotxe, altCotxe,
            1f, 1f,
            180f,                             // rotació 180° = capgirat
            0, 0,
            texturaCotxe.getWidth(), texturaCotxe.getHeight(),
            false, false
        );

        // --- Puntuació final ---
        joc.fontPunts.setColor(Color.YELLOW);
        layout.setText(joc.fontPunts, "Puntuacio final:");
        joc.fontPunts.draw(joc.batch, layout,
            (Main.AMPLADA - layout.width) / 2f, 360f);

        layout.setText(joc.fontPunts, puntuacioFinal + " punts");
        joc.fontPunts.draw(joc.batch, layout,
            (Main.AMPLADA - layout.width) / 2f, 315f);

        // --- Comentari personalitzat ---
        joc.fontPunts.setColor(Color.LIGHT_GRAY);
        String comentari = obtenirComentari(puntuacioFinal);
        layout.setText(joc.fontPunts, comentari);
        joc.fontPunts.draw(joc.batch, layout,
            (Main.AMPLADA - layout.width) / 2f, 260f);

        // --- "Toca per tornar" parpellejant ---
        if (tempsParpelleig % 1f < 0.65f) {
            joc.fontPunts.setColor(Color.WHITE);
            layout.setText(joc.fontPunts, "Toca per tornar al menu");
            joc.fontPunts.draw(joc.batch, layout,
                (Main.AMPLADA - layout.width) / 2f, 130f);
        }

        joc.batch.end();

        if (Gdx.input.justTouched()) {
            joc.setScreen(new PantallaMenu(joc));
            dispose();
        }
    }

    /** Comentari motivador o irònic segons la puntuació */
    private String obtenirComentari(int punts) {
        if (punts < 50)  return "Ja milloraras, principiant!";
        if (punts < 150) return "No esta malament!";
        if (punts < 300) return "Bon conductor!";
        if (punts < 500) return "Impressionant! Ets un pro!";
        return "Increible! El millor pilot!";
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
        if (texturaCotxe != null) texturaCotxe.dispose();
    }
}
