/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2016 Joern Huxhorn
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
 * Copyright 2007-2016 Joern Huxhorn
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

package de.huxhorn.sulky.ulid;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/*
 * https://github.com/alizain/ulid
 */
public class ULID
{
	private static final char[] CHARS = {
			'0','1','2','3','4','5','6','7','8','9',
			'A','B','C','D','E','F','G','H','J','K',
			'M','N','P','Q','R','S','T','V','W','X',
			'Y','Z'
	};

	private static final int MASK = 0x1F;
	private static final int MASK_BITS = 5;
	private static final long TIMESTAMP_MASK = 0x0000_FFFF_FFFF_FFFFL;

	/*
	 * http://crockford.com/wrmg/base32.html
	 */
	static void internalAppendCrockford(StringBuilder builder, long value, int count)
	{
		for(int i = count-1; i >= 0; i--)
		{
			int index = (int)((value >>> (i * MASK_BITS)) & MASK);
			builder.append(CHARS[index]);
		}
	}

	static void internalAppendULID(StringBuilder builder, long timeStamp, Random random)
	{
		// this will be extremely important in the summer of 10889.
		timeStamp = timeStamp & TIMESTAMP_MASK;
		internalAppendCrockford(builder, timeStamp, 10);
		// could use nextBytes(byte[] bytes) instead
		internalAppendCrockford(builder, random.nextLong(), 8);
		internalAppendCrockford(builder, random.nextLong(), 8);
	}

	private final Random random;

	public ULID()
	{
		this(new SecureRandom());
	}

	public ULID(Random random)
	{
		Objects.requireNonNull(random, "random must not be null!");
		this.random = random;
	}

	public void appendULID(StringBuilder stringBuilder)
	{
		Objects.requireNonNull(stringBuilder, "stringBuilder must not be null!");
		internalAppendULID(stringBuilder, System.currentTimeMillis(), random);
	}

	public String nextULID()
	{
		StringBuilder stringBuilder = new StringBuilder(26);
		appendULID(stringBuilder);
		return stringBuilder.toString();
	}
}
