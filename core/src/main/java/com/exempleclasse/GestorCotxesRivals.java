package com.exempleclasse;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

/**
 * Grup d'actors que gestiona l'aparició, el moviment i la col·lisió dels cotxes rivals.
 * Hereta de Group (Scene2D) per contenir múltiples CotxeRival com a actors fills.
 * Cada cert interval de temps, genera un nou cotxe rival a la part superior de la pantalla.
 */
public class GestorCotxesRivals extends Group {

    // Interval entre l'aparició de nous cotxes rivals (en nanosegons)
    private static final long INTERVAL_SPAWN_NS = 2_000_000_000L; // 2 segons

    // Temps que tarda un rival a recórrer tota l'alçada de la pantalla (de dalt a baix).
    // Calibrat per a la pantalla vertical de 800 px.
    private static final float DURADA_TRAVESSIA = 3.5f; // segons

    // Textura compartida per tots els cotxes rivals (la mateixes instància per estalviar memòria)
    private final Texture textura;

    // Marca de temps de l'últim cotxe generat (en nanosegons del sistema)
    private long darrereSpawnTime;

    /**
     * Constructor: guarda la textura i genera el primer cotxe de seguida.
     * @param textura textura del cotxe rival
     */
    public GestorCotxesRivals(Texture textura) {
        this.textura = textura;
        darrereSpawnTime = TimeUtils.nanoTime();
        generarCotxeRival();
    }

    /**
     * S'executa cada fotograma. Comprova si ja és hora de generar un nou cotxe rival.
     */
    @Override
    public void act(float delta) {
        super.act(delta); // Actualitza tots els actors fills (i les seves accions de moviment)
        if (TimeUtils.nanoTime() - darrereSpawnTime > INTERVAL_SPAWN_NS) {
            darrereSpawnTime = TimeUtils.nanoTime();
            generarCotxeRival();
        }
    }

    /**
     * Crea un nou CotxeRival i li afegeix l'acció de desplaçar-se cap al fons de la pantalla.
     * La posició de destí és per sota del límit inferior visible (getHeight() + 10 px).
     */
    private void generarCotxeRival() {
        CotxeRival rival = new CotxeRival(textura);
        // Movem el rival des de la seva posició inicial (sobre la pantalla) fins al fons
        rival.addAction(Actions.moveTo(rival.getX(), -rival.getHeight() - 10f, DURADA_TRAVESSIA));
        addActor(rival);
    }

    /**
     * Recorre tots els cotxes rivals actius i comprova si algun ha xocat amb el jugador.
     * En cas de col·lisió, elimina el cotxe rival de l'escena.
     *
     * @param cotxeJugador l'actor del cotxe del jugador
     * @return true si s'ha produït almenys una col·lisió en aquest fotograma
     */
    public boolean comprovarCollisio(CotxeJugador cotxeJugador) {
        boolean hiHaCollisio = false;
        // SnapshotArray de LibGDX és segur per a modificacions durant la iteració
        Iterator<Actor> it = getChildren().iterator();
        while (it.hasNext()) {
            CotxeRival rival = (CotxeRival) it.next();
            if (rival.colisionaAmbJugador(cotxeJugador)) {
                rival.remove();
                hiHaCollisio = true;
            }
        }
        return hiHaCollisio;
    }
}
