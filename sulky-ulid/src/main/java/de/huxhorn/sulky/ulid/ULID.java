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

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/*
 * https://github.com/alizain/ulid
 */
public class ULID
{
	private static final char[] ENCODING_CHARS = {
			'0','1','2','3','4','5','6','7','8','9',
			'A','B','C','D','E','F','G','H','J','K',
			'M','N','P','Q','R','S','T','V','W','X',
			'Y','Z'
	};

	private static final byte[] DECODING_CHARS = new byte[128];
	static
	{
		for(int i=0;i<DECODING_CHARS.length;i++)
		{
			DECODING_CHARS[i] = -1; // illegal value
		}

		DECODING_CHARS['0'] = 0;
		DECODING_CHARS['O'] = 0;
		DECODING_CHARS['o'] = 0;

		DECODING_CHARS['1'] = 1;
		DECODING_CHARS['I'] = 1;
		DECODING_CHARS['i'] = 1;
		DECODING_CHARS['L'] = 1;
		DECODING_CHARS['l'] = 1;

		DECODING_CHARS['2'] = 2;
		DECODING_CHARS['3'] = 3;
		DECODING_CHARS['4'] = 4;
		DECODING_CHARS['5'] = 5;
		DECODING_CHARS['6'] = 6;
		DECODING_CHARS['7'] = 7;
		DECODING_CHARS['8'] = 8;
		DECODING_CHARS['9'] = 9;

		DECODING_CHARS['A'] = 10;
		DECODING_CHARS['a'] = 10;

		DECODING_CHARS['B'] = 11;
		DECODING_CHARS['b'] = 11;

		DECODING_CHARS['C'] = 12;
		DECODING_CHARS['c'] = 12;

		DECODING_CHARS['D'] = 13;
		DECODING_CHARS['d'] = 13;

		DECODING_CHARS['E'] = 14;
		DECODING_CHARS['e'] = 14;

		DECODING_CHARS['F'] = 15;
		DECODING_CHARS['f'] = 15;

		DECODING_CHARS['G'] = 16;
		DECODING_CHARS['g'] = 16;

		DECODING_CHARS['H'] = 17;
		DECODING_CHARS['h'] = 17;

		DECODING_CHARS['J'] = 18;
		DECODING_CHARS['j'] = 18;

		DECODING_CHARS['K'] = 19;
		DECODING_CHARS['k'] = 19;

		DECODING_CHARS['M'] = 20;
		DECODING_CHARS['m'] = 20;

		DECODING_CHARS['N'] = 21;
		DECODING_CHARS['n'] = 21;

		DECODING_CHARS['P'] = 22;
		DECODING_CHARS['p'] = 22;

		DECODING_CHARS['P'] = 22;
		DECODING_CHARS['p'] = 22;

		DECODING_CHARS['Q'] = 23;
		DECODING_CHARS['q'] = 23;

		DECODING_CHARS['R'] = 24;
		DECODING_CHARS['r'] = 24;

		DECODING_CHARS['S'] = 25;
		DECODING_CHARS['s'] = 25;

		DECODING_CHARS['T'] = 26;
		DECODING_CHARS['t'] = 26;

		DECODING_CHARS['V'] = 27;
		DECODING_CHARS['v'] = 27;

		DECODING_CHARS['W'] = 28;
		DECODING_CHARS['w'] = 28;

		DECODING_CHARS['X'] = 29;
		DECODING_CHARS['x'] = 29;

		DECODING_CHARS['Y'] = 30;
		DECODING_CHARS['y'] = 30;

		DECODING_CHARS['Z'] = 31;
		DECODING_CHARS['z'] = 31;
	}

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
			builder.append(ENCODING_CHARS[index]);
		}
	}

	static long internalParseCrockford(String input)
	{
		Objects.requireNonNull(input, "input must not be null!");
		long result = 0;
		int length = input.length();
		if(length > 12)
		{
			throw new IllegalArgumentException("input length must not exceed 12 but was "+length+"!");
		}
		for(int i=0;i<length;i++)
		{
			char current = input.charAt(i);
			byte value = -1;
			if(current < DECODING_CHARS.length)
			{
				value = DECODING_CHARS[current];
			}
			if(value < 0)
			{
				throw new IllegalArgumentException("Illegal character '"+current+"'!");
			}
			result |= ((long)value) << ((length - 1 - i)*MASK_BITS);
		}
		return result;
	}

	/*
	 * http://crockford.com/wrmg/base32.html
	 */
	static void internalWriteCrockford(char[] buffer, long value, int count, int offset)
	{
		for(int i = 0; i < count; i++)
		{
			int index = (int)((value >>> ((count - i - 1) * MASK_BITS)) & MASK);
			buffer[offset+i] = ENCODING_CHARS[index];
		}
	}

	static String internalUIDString(long timeStamp, Random random)
	{
		// this will be extremely important in the summer of 10889.
		timeStamp = timeStamp & TIMESTAMP_MASK;

		char[] buffer = new char[26];

		internalWriteCrockford(buffer, timeStamp, 10, 0);
		// could use nextBytes(byte[] bytes) instead
		internalWriteCrockford(buffer, random.nextLong(), 8, 10);
		internalWriteCrockford(buffer, random.nextLong(), 8, 18);

		return new String(buffer);
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

	static Value internalNextValue(long timeStamp, Random random)
	{
		// could use nextBytes(byte[] bytes) instead
		long mostSignificantBits = random.nextLong();
		long leastSignificantBits = random.nextLong();
		mostSignificantBits &= 0xFFFF;
		mostSignificantBits |= (timeStamp << 16);
		return new Value(mostSignificantBits, leastSignificantBits);
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
		return internalUIDString(System.currentTimeMillis(), random);
	}

	public Value nextValue()
	{
		return internalNextValue(System.currentTimeMillis(), random);
	}

	public static Value parseULID(String ulidString)
	{
		Objects.requireNonNull(ulidString, "ulidString must not be null!");
		if(ulidString.length() != 26)
		{
			throw new IllegalArgumentException("ulidString must be exactly 26 chars long.");
		}

		String timeString = ulidString.substring(0, 10);
		String part1String = ulidString.substring(10, 18);
		String part2String = ulidString.substring(18);
		long time = internalParseCrockford(timeString);
		long part1 = internalParseCrockford(part1String);
		long part2 = internalParseCrockford(part2String);

		long most = (time << 16) | (part1 >>> 24);
		long least = part2 | (part1 << 40);
		return new Value(most, least);
	}

	public static Value fromBytes(byte[] data)
	{
		Objects.requireNonNull(data, "data must not be null!");
		if(data.length != 16)
		{
			throw new IllegalArgumentException("data must be 16 bytes in length!");
		}
		long mostSignificantBits = 0;
		long leastSignificantBits = 0;
		for (int i=0; i<8; i++)
		{
			mostSignificantBits = (mostSignificantBits << 8) | (data[i] & 0xff);
		}
		for (int i=8; i<16; i++)
		{
			leastSignificantBits = (leastSignificantBits << 8) | (data[i] & 0xff);
		}
		return new Value(mostSignificantBits, leastSignificantBits);
	}

	public static class Value
		implements Comparable<Value>, Serializable
	{
		private static final long serialVersionUID = -3563159514112487717L;

		/*
		 * The most significant 64 bits of this ULID.
		 */
		private final long mostSignificantBits;

		/*
		 * The least significant 64 bits of this ULID.
		 */
		private final long leastSignificantBits;

		public Value(long mostSignificantBits, long leastSignificantBits)
		{
			this.mostSignificantBits = mostSignificantBits;
			this.leastSignificantBits = leastSignificantBits;
		}

		/**
		 * Returns the most significant 64 bits of this ULID's 128 bit value.
		 *
		 * @return  The most significant 64 bits of this ULID's 128 bit value
		 */
		public long getMostSignificantBits() {
			return mostSignificantBits;
		}

		/**
		 * Returns the least significant 64 bits of this ULID's 128 bit value.
		 *
		 * @return  The least significant 64 bits of this ULID's 128 bit value
		 */
		public long getLeastSignificantBits() {
			return leastSignificantBits;
		}


		public long timestamp()
		{
			return mostSignificantBits >>> 16;
		}

		public byte[] toBytes()
		{
			byte[] result=new byte[16];
			for (int i=0; i<8; i++)
			{
				result[i] = (byte)((mostSignificantBits >> ((7-i)*8)) & 0xFF);
			}
			for (int i=8; i<16; i++)
			{
				result[i] = (byte)((leastSignificantBits >> ((15-i)*8)) & 0xFF);
			}

			return result;
		}

		@Override
		public int hashCode() {
			long hilo = mostSignificantBits ^ leastSignificantBits;
			return ((int)(hilo >> 32)) ^ (int) hilo;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Value value = (Value) o;

			return mostSignificantBits == value.mostSignificantBits
					&& leastSignificantBits == value.leastSignificantBits;
		}

		@Override
		public int compareTo(Value val)
		{
			// The ordering is intentionally set up so that the ULIDs
			// can simply be numerically compared as two numbers
			return (this.mostSignificantBits < val.mostSignificantBits ? -1 :
					(this.mostSignificantBits > val.mostSignificantBits ? 1 :
							(this.leastSignificantBits < val.leastSignificantBits ? -1 :
									(this.leastSignificantBits > val.leastSignificantBits ? 1 :
											0))));
		}

		public String toString()
		{
			char[] buffer = new char[26];

			internalWriteCrockford(buffer, timestamp(), 10, 0);
			long value = ((mostSignificantBits & 0xFFFFL) << 24);
			long interim = (leastSignificantBits >>> 40);
			value = value | interim;
			internalWriteCrockford(buffer, value, 8, 10);
			internalWriteCrockford(buffer, leastSignificantBits, 8, 18);

			return new String(buffer);
		}
	}
}
