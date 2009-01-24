/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2009 Joern Huxhorn
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.huxhorn.sulky.sounds.jlayer;

import de.huxhorn.sulky.sounds.Sounds;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JLayerSounds
	implements Sounds
{
	private final Logger logger = LoggerFactory.getLogger(JLayerSounds.class);

	private final List<String> playList;
	private Map<String, String> soundLocations;
	private boolean mute;


	public JLayerSounds()
	{
		playList = new ArrayList<String>();
		soundLocations = new HashMap<String, String>();
		Thread t = new Thread(new PlayRunnable(), "SoundPlayRunnable");
		t.setDaemon(true);
		t.start();
	}

	public boolean isMute()
	{
		return mute;
	}

	public void setMute(boolean mute)
	{
		this.mute = mute;
	}

	public Map<String, String> getSoundLocations()
	{
		return soundLocations;
	}

	public void setSoundLocations(Map<String, String> soundLocations)
	{
		this.soundLocations = soundLocations;
	}

	public void play(String soundName, boolean ignoreDuplicates)
	{
		if(!isMute())
		{
			synchronized(playList)
			{
				if(ignoreDuplicates && playList.contains(soundName))
				{
					if(logger.isInfoEnabled()) logger.info("Ignoring duplicate sound '{}'.", soundName);
					return;
				}
				playList.add(soundName);
				playList.notifyAll();
			}
		}
	}

	/**
	 * Shortcut for play(soundName, true).
	 *
	 * @param soundName
	 */
	public void play(String soundName)
	{
		play(soundName, true);
	}

	private Player resolvePlayer(String soundName)
	{
		String soundLocation = null;
		if(soundLocations != null)
		{
			soundLocation = soundLocations.get(soundName);
		}
		if(soundLocation == null)
		{
			if(logger.isInfoEnabled()) logger.info("No soundlocation defined for sound {}.", soundName);
			return null;
		}
		InputStream soundStream = JLayerSounds.class.getResourceAsStream(soundLocation);
		if(soundStream == null)
		{
			if(logger.isInfoEnabled()) logger.info("Couldn't retrieve {} as a resource...", soundLocation);
			File file = new File(soundLocation);
			if(file.isFile())
			{
				try
				{
					soundStream = new FileInputStream(file);
				}
				catch(FileNotFoundException e)
				{
					if(logger.isInfoEnabled()) logger.info("Couldn't open {} as a file.", soundLocation);
				}
			}
			if(soundStream == null)
			{
				try
				{
					URL url = new URL(soundLocation);
					soundStream = url.openStream();
				}
				catch(MalformedURLException e)
				{
					if(logger.isInfoEnabled()) logger.info("Couldn't open {} as a URL.", soundLocation);
				}
				catch(IOException e)
				{
					if(logger.isInfoEnabled()) logger.info("Couldn't open {} as a URL.", soundLocation);
				}
			}
		}
		if(soundStream != null)
		{
			try
			{
				return new Player(soundStream);
			}
			catch(JavaLayerException ex)
			{
				if(logger.isWarnEnabled())
				{
					logger.warn("Exception while creating player for sound '" + soundName + "'!", ex);
				}
			}
		}
		return null;
	}

	private class PlayRunnable
		implements Runnable
	{
		public void run()
		{
			for(; ;)
			{
				String nextSound;
				synchronized(playList)
				{
					for(; ;)
					{
						if(playList.size() == 0)
						{
							try
							{
								playList.wait();
							}
							catch(InterruptedException e)
							{
								if(logger.isInfoEnabled()) logger.info("Interrupted...");
								return;
							}
						}
						else
						{
							nextSound = playList.get(0);
							break;
						}
					}
				}
				if(!isMute())
				{
					if(logger.isInfoEnabled()) logger.info("Playing sound {}.", nextSound);
					Player player = resolvePlayer(nextSound);
					if(player != null)
					{
						try
						{
							player.play();
							player.close();
						}
						catch(JavaLayerException ex)
						{
							if(logger.isWarnEnabled())
							{
								logger.warn("Exception while playing sound " + nextSound + "'!", ex);
							}
						}
					}
					else
					{
						if(logger.isInfoEnabled()) logger.info("Couldn't resolve player for sound '{}'.", nextSound);
					}
				}

				synchronized(playList)
				{
					playList.remove(0);
				}
			}
		}

	}
}
