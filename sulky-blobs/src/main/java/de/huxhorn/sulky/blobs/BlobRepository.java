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

package de.huxhorn.sulky.blobs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * This interface specifies a repository for BLOBs (binary large object).
 *
 * It simply stores and retrieves bytes using an identifier.
 */
public interface BlobRepository
{
	
	/**
	 * Puts the data that's available from the InputStream into the repository and returns the id used to reference it.
	 *
	 * @param input the InputStream used to read the data to be put into the repository.
	 * @return the id that is used to reference the data.
	 * @throws IOException in case of I/O problems.
	 */
	String put(InputStream input)
		throws IOException;

	/**
	 * Puts the given data into the repository and returns the id used to reference it.
	 *
	 * @param bytes the data to be put into the repository.
	 * @return the id that is used to reference the data.
	 * @throws IOException in case of I/O problems.
	 */
	String put(byte[] bytes)
		throws IOException;

	/**
	 * Retrieves an InputStream providing the data associated with the given id.
	 *
	 * @param id the id of the blob to be retrieved from the repository.
	 * @return an InputStream that can be used to retrieve the data of the blob.
	 * @throws IOException in case of I/O problems.
	 * @throws AmbiguousIdException if the given id references more than a single blob.
	 */
	InputStream get(String id)
		throws IOException, AmbiguousIdException;

	/**
	 * Deletes the blob represented by the given id.
	 *
	 * @param id the id of the blob to be deleted.
	 * @return true if the blob was deleted, false otherwise.
	 * @throws AmbiguousIdException if the given id references more than a single blob.
	 */
	boolean delete(String id)
		throws AmbiguousIdException;

	/**
	 * Returns true if a blob for the given id exists.
	 *
	 * @param id the id of the blob.
	 * @return true if the blob exists, false otherwise.
	 * @throws AmbiguousIdException if the given id references more than a single blob.
	 */
	boolean contains(String id)
		throws AmbiguousIdException;

	/**
	 * Returns the size of the blob for the given id.
	 *
	 * @param id the id of the blob.
	 * @return the size of the blob in bytes or -1 if no blob with the given id exists.
	 * @throws AmbiguousIdException if the given id references more than a single blob.
	 */
	long sizeOf(String id)
		throws AmbiguousIdException;

	/**
	 * Returns a Set containing all ids of this repository.
	 *
	 * @return a Set containing all ids of this repository.
	 */
	Set<String> idSet();
}
