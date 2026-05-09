package com.exempleclasse;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Processa l'entrada tàctil de l'usuari per moure el cotxe del jugador.
 * En tocar o arrossegar la pantalla, el cotxe es desplaça a la posició horitzontal tocada.
 * Les coordenades de pantalla es converteixen a coordenades de món usant la càmera de l'Stage.
 */
public class GestorEntrada implements InputProcessor {

    // Referència a la pantalla de joc per accedir a l'Stage i al cotxe del jugador
    private final PantallaJoc pantallaJoc;

    /**
     * @param pantallaJoc la pantalla de joc activa
     */
    public GestorEntrada(PantallaJoc pantallaJoc) {
        this.pantallaJoc = pantallaJoc;
    }

    /**
     * Quan l'usuari toca la pantalla, el cotxe salta immediatament a la posició tocada.
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        moureCotxe(screenX, screenY);
        return true;
    }

    /**
     * Quan l'usuari arrossega el dit, el cotxe segueix el moviment en temps real.
     */
    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        moureCotxe(screenX, screenY);
        return true;
    }

    /**
     * Converteix la posició tocada de pantalla a coordenades de món i mou el cotxe del jugador.
     * El cotxe queda centrat horitzontalment respecte al punt tocat.
     * La posició es limita perquè el cotxe no surti per les vores laterals.
     *
     * @param screenX posició X de la pantalla física (en píxels)
     * @param screenY posició Y de la pantalla física (en píxels)
     */
    private void moureCotxe(int screenX, int screenY) {
        // Desprojectem les coordenades de pantalla a coordenades de món (800x480)
        Vector3 posicioMon = pantallaJoc.stage.getCamera()
            .unproject(new Vector3(screenX, screenY, 0));

        // Centrem el cotxe respecte al punt tocat (usem getWidth per respectar proporcions reals)
        float ampladaCotxe = pantallaJoc.cotxeJugador.getWidth();
        float nouX = posicioMon.x - ampladaCotxe / 2f;

        // Limitem la posició per no sortir dels límits laterals de la pantalla
        nouX = MathUtils.clamp(nouX, 30f, Main.AMPLADA - ampladaCotxe - 30f);

        pantallaJoc.cotxeJugador.setX(nouX);
    }

    // --- Mètodes de la interfície que no usem ---
    @Override public boolean keyDown(int keycode)              { return false; }
    @Override public boolean keyUp(int keycode)                { return false; }
    @Override public boolean keyTyped(char character)          { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
}
