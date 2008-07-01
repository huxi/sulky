/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2008 Joern Huxhorn
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
package de.huxhorn.sulky.sounds;

import java.util.Map;

public interface Sounds
{
	/**
	 * Plays the sound with the given name.
	 *
	 * If ignoreDuplicates is true, another instance
	 * of the same sound will not be
	 * added to the playlist while it is still played or waiting to be played .
	 *
	 * @param soundName
	 * @param ignoreDuplicates
	 */
	void play(String soundName, boolean ignoreDuplicates);

	/**
	 * Shortcut for play(soundName, true).
	 * @param soundName
	 */
	void play(String soundName);

	void setMute(boolean mute);

	boolean isMute();

	Map<String, String> getSoundLocations();
	void setSoundLocations(Map<String, String> soundLocations);
}
