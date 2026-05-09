package com.exempleclasse;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Actor que representa el cotxe del jugador (Ferrari).
 * S'inicialitza a la part inferior de la pantalla i només es desplaça horitzontalment.
 * L'alçada de visualització es calcula automàticament a partir de la proporció real
 * de la textura, de manera que la imatge mai apareix deformada.
 */
public class CotxeJugador extends Image {

    // Amplada de visualització fixa (en píxels de món). L'alçada s'escala proporcionalment.
    public static final float AMPLADA = 72f;

    /**
     * Crea el cotxe del jugador centrat horitzontalment a la part inferior de la pantalla.
     * L'alçada es calcula per mantenir la proporció original de la imatge.
     *
     * @param textura textura del Ferrari carregada prèviament
     */
    public CotxeJugador(Texture textura) {
        super(textura);
        // Calculem l'alçada proporcional a partir de la relació amplada/alçada de la textura
        float ratio   = (float) textura.getHeight() / textura.getWidth();
        float alcada  = AMPLADA * ratio;
        setSize(AMPLADA, alcada);

        // Posicionem el cotxe centrat horitzontalment, prop del fons de la pantalla
        float xInicial = (Main.AMPLADA - AMPLADA) / 2f;
        setPosition(xInicial, 50f);
    }

    /**
     * Retorna el rectangle de col·lisió del cotxe del jugador.
     * El rectangle s'encongeix lleugerament als costats per a una col·lisió més justa.
     */
    public Rectangle getRectangle() {
        float margeH = 10f; // marge horitzontal
        float margeV = 8f;  // marge vertical
        return new Rectangle(
            getX() + margeH,
            getY() + margeV,
            getWidth()  - margeH * 2,
            getHeight() - margeV * 2
        );
    }
}
