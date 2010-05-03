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
package de.huxhorn.sulky.swing;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"unchecked"})
public class ListComboBoxModel
		extends AbstractListModel
		implements MutableComboBoxModel
{
	private static final long serialVersionUID = 4914401784245069859L;

	private final List store;
	private Object selectedItem;

	public ListComboBoxModel()
	{
		this.store = new ArrayList();
	}

	public ListComboBoxModel(List store)
	{
		this.store = new ArrayList(store);
	}

	public Object getElementAt(int index)
	{
		if(index >= 0 && index < store.size())
		{
			return store.get(index);
		}
		return null;
	}

	public int getSize()
	{
		return store.size();
	}

	public Object getSelectedItem()
	{
		return this.selectedItem;
	}

	public void setSelectedItem(Object anObject)
	{
		if ((selectedItem != null && !selectedItem.equals(anObject)) ||
				selectedItem == null && anObject != null)
		{
			selectedItem = anObject;
			fireContentsChanged(this, 0, getSize());
		}
	}

	public void addElement(Object anObject)
	{
		store.add(anObject);
		fireIntervalAdded(this, store.size() - 1, store.size() - 1);
		if (store.size() == 1 && selectedItem == null && anObject != null)
		{
			setSelectedItem(anObject);
		}
	}

	public void insertElementAt(Object anObject, int index)
	{
		store.add(index, anObject);
		fireIntervalAdded(this, index, index);
	}

	public void removeElementAt(int index)
	{
		if (getElementAt(index) == selectedItem)
		{
			if (index == 0)
			{
				setSelectedItem(getSize() == 1 ? null : getElementAt(index + 1));
			}
			else
			{
				setSelectedItem(getElementAt(index - 1));
			}
		}

		store.remove(index);

		fireIntervalRemoved(this, index, index);
	}

	public void removeElement(Object anObject)
	{
		int index = store.indexOf(anObject);
		if (index != -1)
		{
			removeElementAt(index);
		}
	}

	/**
	 * Empties the list.
	 */
	public void removeAllElements()
	{
		if (store.size() > 0)
		{
			int firstIndex = 0;
			int lastIndex = store.size() - 1;
			store.clear();
			selectedItem = null;
			fireIntervalRemoved(this, firstIndex, lastIndex);
		}
		else
		{
			selectedItem = null;
		}
	}

	/**
	 * Replaces the content of this model with the contents of the list.
	 *
	 * @param list the new values.
	 */
	public void replace(List list)
	{
		int firstIndex = 0;
		int lastIndex = store.size() - 1;
		store.clear();
		store.addAll(list);
		int newLastIndex = store.size() - 1;
		if (lastIndex < newLastIndex)
		{
			fireContentsChanged(this, firstIndex, lastIndex);
			fireIntervalAdded(this, lastIndex + 1, newLastIndex);
		}
		else if (lastIndex > newLastIndex)
		{
			fireContentsChanged(this, firstIndex, newLastIndex);
			fireIntervalRemoved(this, newLastIndex + 1, lastIndex);
		}
		else
		{
			fireContentsChanged(this, firstIndex, newLastIndex);
		}
		if (selectedItem != null)
		{
			int index = store.indexOf(selectedItem);
			if (index < 0)
			{
				selectedItem = null;
			}
		}
		if (selectedItem == null && store.size() > 0)
		{
			setSelectedItem(store.get(0));
		}
	}

	/**
	 * Replaces the content of this model with the contents of the array.
	 *
	 * @param values the new values.
	 */
	public void replace(Object[] values)
	{
		if (values == null)
		{
			return;
		}

		replace(Arrays.asList(values));
	}
}
