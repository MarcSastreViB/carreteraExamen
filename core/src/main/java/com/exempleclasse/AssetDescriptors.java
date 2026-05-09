package com.exempleclasse;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

/**
 * Descriptors centralitzats de tots els assets del joc.
 * Usar descriptors evita duplicar els noms dels fitxers per tot el codi
 * i aprofita la gestió de tipus de l'AssetManager.
 */
public class AssetDescriptors {

    // --- Textures ---
    /** Imatge de la carretera (fons desplaçable) */
    public static final AssetDescriptor<Texture> carretera =
        new AssetDescriptor<>("carretera.jpg", Texture.class);

    /** Sprite del cotxe del jugador (Ferrari) */
    public static final AssetDescriptor<Texture> ferrari =
        new AssetDescriptor<>("ferrari.png", Texture.class);

    /** Sprite del cotxe rival */
    public static final AssetDescriptor<Texture> cotxeRival =
        new AssetDescriptor<>("cotxeRival.png", Texture.class);

    // --- So ---
    /** So de l'impacte quan el jugador xoca amb un rival */
    public static final AssetDescriptor<Sound> impacte =
        new AssetDescriptor<>("impacte.wav", Sound.class);

    // --- Música ---
    /** Música de fons del joc (es reproducirà en bucle al 30% de volum) */
    public static final AssetDescriptor<Music> musica =
        new AssetDescriptor<>("musica.mp3", Music.class);
}
