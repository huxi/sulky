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

package de.huxhorn.sulky.codec.filebuffer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface FileHeaderStrategy
{
	/**
	 * @param dataFile the file to read the magic value from.
	 * @return the magic value of the file if it is compatible, null otherwise.
	 * @throws java.io.IOException in case of IOException :p
	 */
	Integer readMagicValue(File dataFile)
		throws IOException;

	/**
	 * @param dataFile   the file to write the header to. Must be empty or nonexistent.
	 * @param magicValue the magic value of the file.
	 * @param sparse     if the file is suposed to support sparse entries.
	 * @param metaData   the meta data of the file, can be null.
	 * @return the written FileHeader
	 * @throws IllegalArgumentException if file exists and isn't empty.
	 * @throws java.io.IOException      in case of IOException :p
	 */
	FileHeader writeFileHeader(File dataFile, int magicValue, Map<String, String> metaData, boolean sparse)
		throws IOException;

	/**
	 * @param dataFile the file to read the header from.
	 * @return the FileHeader read from the file.
	 * @throws java.io.IOException in case of IOException :p
	 */
	FileHeader readFileHeader(File dataFile)
		throws IOException;

	int getMinimalSize();
}
