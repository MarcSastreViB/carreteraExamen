package com.exempleclasse;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Actor que representa un cotxe rival que ve de cara al jugador.
 * Apareix a la part superior de la pantalla en un dels quatre carrils disponibles
 * i es desplaça cap avall usant accions de Scene2D.
 * L'alçada es calcula proporcionalment a partir de la textura per no deformar la imatge.
 */
public class CotxeRival extends Image {

    // Amplada de visualització fixa (igual que el cotxe del jugador per a coherència visual)
    public static final float AMPLADA = 72f;

    /**
     * Posicions X (cantó esquerre) dels quatre carrils possibles en una pantalla de 480 px.
     * Els carrils estan distribuïts uniformement deixant marge als laterals.
     *  Carril 1: x= 40   Carril 2: x=145   Carril 3: x=250   Carril 4: x=355
     */
    private static final float[] CARRILS = { 40f, 145f, 250f, 355f };

    /**
     * Crea un cotxe rival en un carril aleatori, just per sobre del límit superior visible.
     * @param textura textura del cotxe rival
     */
    public CotxeRival(Texture textura) {
        super(textura);
        // Calculem l'alçada per mantenir les proporcions reals de la imatge
        float ratio  = (float) textura.getHeight() / textura.getWidth();
        float alcada = AMPLADA * ratio;
        setSize(AMPLADA, alcada);

        // Col·loquem el cotxe en un carril aleatori, un poc per sobre de la pantalla
        float xCarril = CARRILS[MathUtils.random(0, CARRILS.length - 1)];
        setPosition(xCarril, Main.ALCADA + alcada);
    }

    /**
     * S'executa cada fotograma (via Stage.act).
     * Si el cotxe ha sortit completament per la part inferior, s'elimina de l'escena.
     */
    @Override
    public void act(float delta) {
        super.act(delta); // Executa les accions associades (moveTo cap avall)
        if (getY() + getHeight() < 0) {
            remove();
        }
    }

    /**
     * Comprova si aquest cotxe rival ha xocat amb el cotxe del jugador (col·lisió AABB).
     * @param jugador l'actor del cotxe del jugador
     * @return true si els rectangles es solapen
     */
    public boolean colisionaAmbJugador(CotxeJugador jugador) {
        float marge = 8f;
        Rectangle rectRival = new Rectangle(
            getX() + marge, getY() + marge,
            getWidth() - marge * 2, getHeight() - marge * 2
        );
        return rectRival.overlaps(jugador.getRectangle());
    }
}
