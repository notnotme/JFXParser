package com.notnotme.jsparser.utils;

/* ================================================================
 * MuXM - MOD/XM/S3M player library for J2ME/J2SE
 * Copyright (C) 2005 Martin Cameron, Guillaume Legris
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * ================================================================
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.muforge.musound.muxm.Loader;
import org.muforge.musound.muxm.Module;
import org.muforge.musound.muxm.ModuleEngine;

/**
 * <p>A simple class that uses MuXM API and JavaSound to play audio from given file names.</p>
 *
 * <p>This example uses getAudio() methods of the MuXM class to get the audio data.</p>
 *
 * @author Martin Cameron
 */
public final class ModulePlayer implements Runnable {

	private final static String TAG = ModulePlayer.class.getSimpleName();
	private final int SAMPLE_RATE = 44100;

	private final ModuleEngine engine;
	private boolean songLoop, running;

	public ModulePlayer(URL modfile) throws IOException {
		Module m;
		try (InputStream i = modfile.openStream()) {
			m = Loader.load(i);
		}

		engine = new ModuleEngine(m);
		engine.setSampleRate(SAMPLE_RATE);
		songLoop = true;
	}

	/**
	 *	Set whether the song is to loop continuously or not.
	 *	The default is to loop.
	 * @param loop true if the song should loop
	 */
	public void setLoop(boolean loop) {
		songLoop = loop;
	}

	/**
	 *	Begin playback.
	 *	This method will return once the song has finished, or stop has been called.
	 */
	@Override
	public void run() {
		Logger.getLogger(TAG).log(Level.INFO, "> run");

		running = true;
		int bufframes = 1024;
		byte[] obuf = new byte[bufframes << 2];

		AudioFormat af = new AudioFormat(SAMPLE_RATE, 16, 2, true, false);
		DataLine.Info lineInfo = new DataLine.Info(SourceDataLine.class, af);

		try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(lineInfo)) {
			line.open();
			line.start();

			int songlen = engine.getSongLength();
			int remain = songlen;
			while (remain > 0 && running) {
				int count = bufframes;
				if (count > remain)
					count = remain;
				engine.getAudio(obuf, 0, count, true);
				line.write(obuf, 0, count << 2);
				remain -= count;
				if (remain <= 0 && songLoop)
					remain = songlen;
			}

			line.drain();
		} catch (LineUnavailableException e) {
			Logger.getLogger(TAG).log(Level.SEVERE, null, e);
		}

		Logger.getLogger(TAG).log(Level.INFO, "< run");
	}

	/**
	 *	Instruct the run() method to finish playing and return.
	 */
	public void stop() {
		running = false;
	}

}
