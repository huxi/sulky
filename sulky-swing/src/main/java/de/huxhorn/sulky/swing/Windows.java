/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2015 Joern Huxhorn
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
 * Copyright 2007-2015 Joern Huxhorn
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

import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class Windows
{
	private Windows()
	{}

	//  Just call Window.setIconImage
	@Deprecated
	public static void setIconImage(Window window, Image image)
	{
		window.setIconImage(image);
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
