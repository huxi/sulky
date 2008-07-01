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
package de.huxhorn.sulky.swing;

import de.huxhorn.sulky.generics.GenericWrapper;

import javax.swing.SwingUtilities;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * A wrapper for PropertyChangeListener that ensures that the wrapped listeners propertyChange method is invoked
 * on the EventDispatchThread.
 */
public class PropertyChangeWrapper
	extends GenericWrapper<PropertyChangeListener> implements PropertyChangeListener
{
	public PropertyChangeWrapper(PropertyChangeListener wrapped)
	{
		super(wrapped);
	}

	public void propertyChange(final PropertyChangeEvent evt)
	{
		final PropertyChangeListener wrapped=getWrapped();
		if(SwingUtilities.isEventDispatchThread())
		{
			wrapped.propertyChange(evt);
		}
		else
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					wrapped.propertyChange(evt);
				}
			});
		}

	}
}
