package com.exempleclasse;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * Classe principal del joc "Carretera Boja".
 * Hereta de Game per gestionar múltiples pantalles.
 * Conté els recursos compartits entre totes les pantalles: batch, fonts i viewport.
 */
public class Main extends Game {

    // Dimensions virtuals de la pantalla en mode vertical (portrait)
    public static final int AMPLADA = 480;
    public static final int ALCADA  = 800;

    // Recursos compartits entre pantalles (accés directe des de les subclasses)
    public SpriteBatch batch;
    public BitmapFont  fontTitol;  // Font gran BeatMark per als títols
    public BitmapFont  fontPunts;  // Font mitjana BeatMark per a puntuació i vides
    public FitViewport viewport;

    @Override
    public void create() {
        batch    = new SpriteBatch();
        viewport = new FitViewport(AMPLADA, ALCADA);

        // Generem les fonts personalitzades a partir del fitxer TTF BeatMark
        generarFonts();

        // Primer que es veu: la pantalla d'inici (splash screen)
        setScreen(new PantallaInici(this));
    }

    /**
     * Genera les fonts BitmapFont des del fitxer BeatMark-Regular.ttf
     * usant la llibreria FreeType, que permet escalar fonts TrueType a qualsevol mida.
     */
    private void generarFonts() {
        FreeTypeFontGenerator generador = new FreeTypeFontGenerator(
            Gdx.files.internal("BeatMark-Regular.ttf")
        );

        // Font gran per als títols (60 píxels — ajustada per a l'amplada de 480 px)
        FreeTypeFontGenerator.FreeTypeFontParameter paramsTitol =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        paramsTitol.size = 60;
        fontTitol = generador.generateFont(paramsTitol);

        // Font per a la puntuació, vides i textos secundaris (36 píxels)
        FreeTypeFontGenerator.FreeTypeFontParameter paramsPunts =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        paramsPunts.size = 36;
        fontPunts = generador.generateFont(paramsPunts);

        // Alliberem el generador un cop creades les fonts
        generador.dispose();
    }

    @Override
    public void render() {
        // Deleguem el render a la pantalla activa
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        fontTitol.dispose();
        fontPunts.dispose();
    }
}
