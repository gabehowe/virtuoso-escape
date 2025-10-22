package org.virtuoso.escape.speech;

/**
 * Control currently playing text to speech.
 * @author Andrew
 */
public class SpeechPlayer {
	private Thread currentClip;
	private static SpeechPlayer instance;

	/**
	 * The global singleton.
	 * @return The global singleton.
	 */
	public static SpeechPlayer instance(){
		if (instance == null)
			instance = new SpeechPlayer();
		return instance;
	}

	/**
	 * Prevents SoundPlayer from being constructed.
	 */
	private SpeechPlayer() {};

	/**
	 * Play text to speech based on text on a separate thread and end previously playing text to speech.
	 * @param text The text to speak.
	 */
	public void playSoundbite(String text) {
		Thread newClip = new Soundbite(text);
		if (currentClip != null && currentClip.isAlive()){
			currentClip.interrupt();
		}
		currentClip = newClip;
		currentClip.start();

	}
}
