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
package de.huxhorn.sulky.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class Windows
{
	public static void setIconImage(Window window, Image image)
	{
		final Logger logger = LoggerFactory.getLogger(Windows.class);

		Throwable error = null;
		try
		{
			Method setIconMethod = Window.class.getMethod("setIconImage", Image.class);

			setIconMethod.invoke(window, image);
		}
		catch(NoSuchMethodException e)
		{
			if(logger.isInfoEnabled()) logger.info("No setIconImage-method found...");
		}
		catch(IllegalAccessException e)
		{
			error = e;
		}
		catch(InvocationTargetException e)
		{
			error = e;
		}
		if(error != null)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while executing setIconImage-method!", error);
		}
	}

	public static void showWindow(Window window, Window centerParent, boolean pack)
	{
		final Logger logger = LoggerFactory.getLogger(Windows.class);

		if(pack)
		{
			window.pack();
		}
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle maxBounds = ge.getMaximumWindowBounds();
		if(logger.isDebugEnabled()) logger.debug("MaximumWindowBounds: {}", maxBounds);

		Rectangle windowBounds = window.getBounds();
		if(logger.isDebugEnabled()) logger.debug("Original windowBounds: {}", windowBounds);
		if(windowBounds.width > maxBounds.width)
		{
			windowBounds.width = maxBounds.width;
		}
		if(windowBounds.height > maxBounds.height)
		{
			windowBounds.height = maxBounds.height;
		}
		if(logger.isDebugEnabled()) logger.debug("Corrected windowBounds: {}", windowBounds);

		Rectangle centerBounds = maxBounds;
		if(centerParent != null && centerParent.isVisible())
		{
			centerBounds = centerParent.getBounds();
			if(logger.isDebugEnabled()) logger.debug("Retrieved parent container bounds...");
		}
		if(logger.isDebugEnabled()) logger.debug("centerBounds: {}", centerBounds);
		windowBounds.x = centerBounds.x + (centerBounds.width - windowBounds.width) / 2;
		windowBounds.y = centerBounds.y + (centerBounds.height - windowBounds.height) / 2;
		if(logger.isDebugEnabled()) logger.debug("centered bounds: {}", windowBounds);

		// first, correct upper left corner
		if(windowBounds.x < maxBounds.x)
		{
			windowBounds.x = maxBounds.x;
		}
		if(windowBounds.y < maxBounds.y)
		{
			windowBounds.y = maxBounds.y;
		}
		if(logger.isDebugEnabled()) logger.debug("corrected1: {}", windowBounds);

		// second, check bottom right
		if(windowBounds.x + windowBounds.width > maxBounds.x + maxBounds.width)
		{
			windowBounds.x = maxBounds.width - windowBounds.width;
		}
		if(windowBounds.y + windowBounds.height > maxBounds.y + maxBounds.height)
		{
			windowBounds.y = maxBounds.height - windowBounds.height;
		}
		if(logger.isDebugEnabled()) logger.debug("corrected1: {}", windowBounds);

		// third, correct upper left corner again because upper left is more important and
		// has probably again moved out of screen because of bottom right correction
		// if window/window is simply too large.
		if(windowBounds.x < maxBounds.x)
		{
			windowBounds.x = maxBounds.x;
		}
		if(windowBounds.y < maxBounds.y)
		{
			windowBounds.y = maxBounds.y;
		}
		if(logger.isDebugEnabled()) logger.debug("changed bounds: {}", windowBounds);

		window.setBounds(windowBounds);
		window.setVisible(true);
	}


}
