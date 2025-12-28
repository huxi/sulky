/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2017 Joern Huxhorn
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

/*
 * Copyright 2007-2017 Joern Huxhorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.huxhorn.sulky.swing;

import java.awt.AWTKeyStroke;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class KeyStrokes
{
	private static final Logger LOGGER = LoggerFactory.getLogger(KeyStrokes.class);

	/**
	 * The command alias that is replaced with the system-dependent command modifiers.
	 */
	public static final String COMMAND_ALIAS = "command";

	/**
	 * The string representation of the system-dependent command modifiers.
	 */
	public static final String COMMAND_MODIFIERS_STRING;

	/**
	 * The system-dependent command modifiers.
	 */
	public static final int COMMAND_MODIFIERS;

	static
	{
		String keyMaskString = "control";
		try
		{
			int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
			keyMaskString = AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_A, keyMask).toString();
			keyMaskString = keyMaskString.substring(0, keyMaskString.length()-" pressed A".length());
			if(LOGGER.isDebugEnabled()) LOGGER.debug("Resolved system-dependent command modifiers '{}'.", keyMaskString);
		}
		catch(HeadlessException ignore)
		{
			if(LOGGER.isWarnEnabled()) LOGGER.warn("Failed to resolve system-dependent command modifiers! Falling back to '{}'.", keyMaskString);
		}

		COMMAND_MODIFIERS = AWTKeyStroke.getAWTKeyStroke(keyMaskString + " A").getModifiers();
		COMMAND_MODIFIERS_STRING = keyMaskString;
	}

	private KeyStrokes()
	{}

	private static String processAccelerator(String accelerator)
	{
		if(accelerator == null)
		{
			return null;
		}
		return accelerator.replaceAll(COMMAND_ALIAS, COMMAND_MODIFIERS_STRING);
	}

	/**
	 * This method expects a keyStrokeString as required by KeyStroke.getKeyStroke(String).
	 *
	 * Additionally, this String may contain the special modifier alias "command".
	 * This special modifier is replaced by the system-dependent command modifiers,
	 * i.e. Ctrl on Windows, Cmd on Mac.
	 *
	 * @param keyStrokeString a String formatted as described above
	 * @return a KeyStroke object for that String, or null if the specified
	 *         String is null, or is formatted incorrectly
	 * @see javax.swing.KeyStroke#getKeyStroke(String)
	 */
	public static KeyStroke resolveAcceleratorKeyStroke(String keyStrokeString)
	{
		String processedKeyStrokeString = processAccelerator(keyStrokeString);
		KeyStroke result = KeyStroke.getKeyStroke(processedKeyStrokeString);
		if(LOGGER.isDebugEnabled()) LOGGER.debug("keyStrokeString {} was changed to {} and resulted in {}.", keyStrokeString, processedKeyStrokeString, result);

		return result;
	}

	public static void registerCommand(JComponent component, Action action, String commandName)
	{
		KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
		if(keyStroke == null)
		{
			return;
		}

		logInputMaps(component, "BEFORE");

		InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = component.getActionMap();
		inputMap.put(keyStroke, commandName);
		actionMap.put(commandName, action);

		Object value;
		inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
		value = inputMap.get(keyStroke);
		if(value != null)
		{
			inputMap.put(keyStroke, commandName);
		}

		inputMap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		value = inputMap.get(keyStroke);
		if(value != null)
		{
			inputMap.put(keyStroke, commandName);
		}

		logInputMaps(component, "AFTER");
	}

	private static void logInputMaps(JComponent component, String identifier)
	{
		if(LOGGER.isDebugEnabled())
		{
			StringBuilder buffer = new StringBuilder();
			buffer.append("Component: ").append(component).append(":\n\t")
					.append(identifier).append(":\n");

			appendInputMap(buffer, "WHEN_FOCUSED", component.getInputMap(JComponent.WHEN_FOCUSED));

			appendInputMap(buffer, "WHEN_ANCESTOR_OF_FOCUSED_COMPONENT", component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));

			appendInputMap(buffer, "WHEN_IN_FOCUSED_WINDOW", component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW));

			LOGGER.debug(buffer.toString());
		}
	}

	private static void appendInputMap(StringBuilder buffer, String mapName, InputMap inputMap)
	{
		buffer.append("\tmapName: ").append(mapName).append('\n');
		KeyStroke[] keys = inputMap.allKeys();
		if(keys != null)
		{
			for(KeyStroke ks : keys)
			{
				buffer.append("\t\tKey  : ").append(ks)
						.append("\n\t\tValue: ").append(inputMap.get(ks))
						.append("\n\t\t----------\n");
			}
		}
	}
}
