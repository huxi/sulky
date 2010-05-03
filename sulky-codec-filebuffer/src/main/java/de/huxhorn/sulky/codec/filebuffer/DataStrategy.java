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
package de.huxhorn.sulky.codec.filebuffer;

import de.huxhorn.sulky.codec.Codec;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public interface DataStrategy<E>
{
	void add(E element,
	         RandomAccessFile indexFile,
	         RandomAccessFile dataFile,
	         Codec<E> codec,
	         IndexStrategy indexStrategy)
		throws IOException;


	void addAll(List<E> elements,
	            RandomAccessFile indexFile,
	            RandomAccessFile dataFile,
	            Codec<E> codec,
	            IndexStrategy indexStrategy)
		throws IOException;

	boolean set(long index, E element,
	            RandomAccessFile indexFile,
	            RandomAccessFile dataFile,
	            Codec<E> codec,
	            IndexStrategy indexStrategy)
		throws IOException, UnsupportedOperationException;

	boolean isSetSupported();

	E get(long index,
	      RandomAccessFile indexFile,
	      RandomAccessFile dataFile,
	      Codec<E> codec,
	      IndexStrategy indexStrategy)
		throws IOException, ClassNotFoundException;
}
