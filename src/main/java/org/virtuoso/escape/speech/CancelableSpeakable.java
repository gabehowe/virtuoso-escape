package org.virtuoso.escape.speech;

import java.io.InputStream;
import org.w3c.dom.Document;

import com.sun.speech.freetts.FreeTTSSpeakable;

/**
 * An implementation of FreeTTS Speakable similar to
 * {@link com.sun.speech.freetts.FreeTTSSpeakableImpl},
 * but that won't print an error when canceled.
 * 
 * @author Andrew
 */
public class CancelableSpeakable implements FreeTTSSpeakable {
	private Document doc;
	private String text;
	private InputStream inputStream;
	volatile boolean completed = false;
	volatile boolean cancelled = false;

	/**
	 * Construct a speakable that will speak a text input.
	 * 
	 * @param text The text input to speak.
	 */
	public CancelableSpeakable(String text) {
		this.text = text;
	}

	/**
	 * Construct a speakable that will speak a document input.
	 * 
	 * @param text The document input to speak.
	 */
	public CancelableSpeakable(Document doc) {
		this.doc = doc;
	}

	/**
	 * Construct a speakable that will speak a stream input.
	 * 
	 * @param text The stream input to speak.
	 */
	public CancelableSpeakable(InputStream is) {
		this.inputStream = is;
	}

	/**
	 * Must be implemented to implement
	 * {@link com.sun.speech.freetts.FreeTTSSpeakable}.
	 */
	public void started() {
	}

	/**
	 * Set completed to true.
	 */
	public synchronized void completed() {
		this.completed = true;
		this.notifyAll();
	}

	/**
	 * Set cancelled to true.
	 */
	public synchronized void cancelled() {
		this.completed = true;
		this.cancelled = true;
		this.notifyAll();
	}

	/**
	 * Returns the completion status of the speakable.
	 * 
	 * @return the completion status of the speakable.
	 */
	public synchronized boolean isCompleted() {
		return this.completed;
	}

	/**
	 * Returns whether or not the speakable is still waiting.
	 * 
	 * @returns whether or not the speakable is still waiting.
	 */
	public synchronized boolean waitCompleted() {
		while (!this.completed) {
			try {
				this.wait();
			} catch (InterruptedException var2) {
				return false;
			}
		}

		return !this.cancelled;
	}

	/**
	 * Returns whether or not the input was plane text
	 * 
	 * @return whether or not the input was plane text.
	 */
	public boolean isPlainText() {
		return this.text != null;
	}

	/**
	 * Returns the input if the input was plane text.
	 * 
	 * @return the text input.
	 */
	public String getText() {
		return this.text;
	}

	/**
	 * Returns whether or not the input was a stream.
	 * 
	 * @return whether or not the input was a stream.
	 */
	public boolean isStream() {
		return this.inputStream != null;
	}

	/**
	 * Returns the input if the input was plane a stream.
	 * 
	 * @return the stream input.
	 */
	public InputStream getInputStream() {
		return this.inputStream;
	}

	/**
	 * Returns whether or not the input was a document.
	 * 
	 * @return whether or not the input was a document.
	 */
	public boolean isDocument() {
		return this.doc != null;
	}

	/**
	 * Returns the input if the input was a document.
	 * 
	 * @return the document input.
	 */
	public Document getDocument() {
		return this.doc;
	}
}
