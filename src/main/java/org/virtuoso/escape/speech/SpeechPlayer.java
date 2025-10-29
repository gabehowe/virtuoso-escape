package org.virtuoso.escape.speech;

import org.virtuoso.escape.model.GameState;

/**
 * Control currently playing text to speech.
 *
 * @author Andrew
 */
public class SpeechPlayer {
    private static SpeechPlayer instance;
    private Thread currentClip;

    /** Prevents SoundPlayer from being constructed. */
    private SpeechPlayer() {}

    /**
     * The global singleton.
     *
     * @return The global singleton.
     */
    public static SpeechPlayer instance() {
        if (instance == null) instance = new SpeechPlayer();
        return instance;
    }

    /**
     * Play text to speech based on text on a separate thread and end previously playing text to speech.
     *
     * @param text The text to speak.
     */
    public void playSoundbite(String text) {
        if (GameState.instance().account() != null
                && !GameState.instance().account().ttsOn()) return;
        Thread newClip = new Soundbite(text);
        stopSoundbite();
        currentClip = newClip;
        currentClip.start();
    }

    /** Stop the currently playing soundbite. */
    public void stopSoundbite() {
        if (currentClip != null && currentClip.isAlive()) {
            currentClip.interrupt();
        }
    }
}
