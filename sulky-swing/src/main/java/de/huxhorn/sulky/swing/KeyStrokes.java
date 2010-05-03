/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2010 Joern Huxhorn
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
 * Copyright 2007-2010 Joern Huxhorn
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.InputEvent;

import javax.swing.*;

public class KeyStrokes
{
	public static final String COMMAND_ALIAS = "command";

	/**
	 * The string representation of the system-dependant command modifier.
	 * <p/>
	 * It is obtained by calling getModifiersString(Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()).
	 */
	public static final String COMMAND_MODIFIERS;

	/**
	 * Contains Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() 
	 */
	public static final int COMMAND_KEYMASK;

	static
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		COMMAND_KEYMASK = toolkit.getMenuShortcutKeyMask();

		COMMAND_MODIFIERS = getModifiersString(COMMAND_KEYMASK);
	}

	/**
	 * Creates a string containing the text representation of the given modifiers.
	 * Calling this method with the value (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK)
	 * would return the string "shift control". Calling this method
	 * without any modifiers set in the modifiers will return an empty string.
	 * Calling it with all modifiers set will return "shift control meta alt altGraph".
	 * <p/>
	 * This method is only used to initialize the system-dependant COMMAND_MODIFIERS
	 * attribute but was left public because it can be quite handy for debugging.
	 *
	 * @param modifiers the modifiers that can be obtained by calling getModifiers() on any InputEvent.
	 * @return the string representation of the given modifiers.
	 * @see java.awt.event.InputEvent#getModifiers()
	 */
	public static String getModifiersString(int modifiers)
	{
		StringBuilder result = new StringBuilder();
		if((modifiers & InputEvent.SHIFT_MASK) != 0)
		{
			result.append("shift");
		}
		if((modifiers & InputEvent.CTRL_MASK) != 0)
		{
			if(result.length() != 0)
			{
				result.append(" ");
			}
			result.append("control");
		}
		if((modifiers & InputEvent.META_MASK) != 0)
		{
			if(result.length() != 0)
			{
				result.append(" ");
			}
			result.append("meta");
		}
		if((modifiers & InputEvent.ALT_MASK) != 0)
		{
			if(result.length() != 0)
			{
				result.append(" ");
			}
			result.append("alt");
		}
		if((modifiers & InputEvent.ALT_GRAPH_MASK) != 0)
		{
			if(result.length() != 0)
			{
				result.append(" ");
			}
			result.append("altGraph");
		}
		return result.toString();
	}

	/**
	 * Shortcut for accel.replaceAll(COMMAND_ALIAS, COMMAND_MODIFIERS).
	 *
	 * @param accel
	 */
	public static String preprocessAccelerator(String accel)
	{
		return accel.replaceAll(COMMAND_ALIAS, COMMAND_MODIFIERS);
	}

	public static KeyStroke resolveAcceleratorKeyStroke(String keyStroke)
	{
		final Logger logger = LoggerFactory.getLogger(KeyStrokes.class);

		KeyStroke result;
		String preprocessedKeyStroke = preprocessAccelerator(keyStroke);
		result = KeyStroke.getKeyStroke(preprocessedKeyStroke);
		if(logger.isDebugEnabled())
		{
			logger
				.debug("keyStroke {} resolved to {} resulted in {}.", new Object[]{keyStroke, preprocessedKeyStroke, result});
		}
		return result;
	}

	public static void registerCommand(JComponent component, Action action, String commandName)
	{
		final Logger logger = LoggerFactory.getLogger(KeyStrokes.class);

		KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
		if(keyStroke != null)
		{
			if(logger.isDebugEnabled())
			{
				logInputMaps(component, "BEFORE");
			}
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

			if(logger.isDebugEnabled())
			{
				logInputMaps(component, "AFTER");
			}
		}
	}

	private static void logInputMaps(JComponent component, String identifier)
	{
		final Logger logger = LoggerFactory.getLogger(KeyStrokes.class);

		StringBuilder buffer = new StringBuilder();
		buffer.append("Component: ").append(component).append(":\n");
		buffer.append("\t").append(identifier).append(":\n");
		InputMap inputMap;
		inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
		appendInputMap(buffer, "WHEN_FOCUSED", inputMap);
		inputMap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		appendInputMap(buffer, "WHEN_ANCESTOR_OF_FOCUSED_COMPONENT", inputMap);
		inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		appendInputMap(buffer, "WHEN_IN_FOCUSED_WINDOW", inputMap);
		logger.debug(buffer.toString());
	}

	private static void appendInputMap(StringBuilder buffer, String mapName, InputMap inputMap)
	{
		buffer.append("\tmapName: ").append(mapName).append("\n");
		KeyStroke[] keys = inputMap.allKeys();
		if(keys != null)
		{
			for(KeyStroke ks : keys)
			{
				buffer.append("\t\tKey  : ").append(ks).append("\n");
				buffer.append("\t\tValue: ").append(inputMap.get(ks)).append("\n");
				buffer.append("\t\t----------\n");
			}
		}
	}
}
