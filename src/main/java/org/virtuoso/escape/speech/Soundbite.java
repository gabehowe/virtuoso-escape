package org.virtuoso.escape.speech;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

/**
 * Play text to speech for a string on a thread.
 *
 * @author Andrew
 */
public class Soundbite extends Thread {
    private final String VOICE_NAME = "kevin16";
    Voice voice;
    private String text;

    /**
     * Construct a soundbite from text.
     *
     * @param text Text to play as sound.
     */
    public Soundbite(String text) {
        this.text = text;
    }

    /** Play the soundbite. */
    public void run() {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        VoiceManager voiceManger = VoiceManager.getInstance();
        voice = voiceManger.getVoice(VOICE_NAME);
        if (voice == null) {
            System.err.println("Voice not found: " + VOICE_NAME);
            return;
        }
        voice.allocate();
        voice.speak(new CancelableSpeakable(text));
        voice.deallocate();
    }

    /** Stop the soundbite from playing. */
    public void interrupt() {
        voice.deallocate();
        voice.getAudioPlayer().cancel();
        super.interrupt();
    }
}
