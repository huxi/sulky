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
package de.huxhorn.sulky.stax;

import junit.framework.TestCase;

public class StaxUtilitiesTest
	extends TestCase
{
	public void testNormalizeNewlines()
	{
		String input;
		String result;
		String expectedResult;

		input = null;
		expectedResult = null;
		result = StaxUtilities.normalizeNewlines(input);
		assertEquals(expectedResult, result);

		input = "";
		expectedResult = "";
		result = StaxUtilities.normalizeNewlines(input);
		assertEquals(expectedResult, result);

		input = "foobar";
		expectedResult = "foobar";
		result = StaxUtilities.normalizeNewlines(input);
		assertEquals(expectedResult, result);

		input = "foo\nbar\nfoobar";
		expectedResult = "foo\nbar\nfoobar";
		result = StaxUtilities.normalizeNewlines(input);
		assertEquals(expectedResult, result);

		input = "foo\r\nbar\r\nfoobar";
		expectedResult = "foo\nbar\nfoobar";
		result = StaxUtilities.normalizeNewlines(input);
		assertEquals(expectedResult, result);

		input = "foo\r\n\r\nbar\r\nfoobar";
		expectedResult = "foo\n\nbar\nfoobar";
		result = StaxUtilities.normalizeNewlines(input);
		assertEquals(expectedResult, result);

		input = "foo\n\rbar\n\rfoobar";
		expectedResult = "foo\nbar\nfoobar";
		result = StaxUtilities.normalizeNewlines(input);
		assertEquals(expectedResult, result);

		input = "foo\rbar\rfoobar";
		expectedResult = "foo\nbar\nfoobar";
		result = StaxUtilities.normalizeNewlines(input);
		assertEquals(expectedResult, result);

		input = "foo\r\rbar\rfoobar";
		expectedResult = "foo\n\nbar\nfoobar";
		result = StaxUtilities.normalizeNewlines(input);
		assertEquals(expectedResult, result);

		input = "foo\r\n\r\rbar\rfoobar\r\r\r";
		expectedResult = "foo\n\n\nbar\nfoobar\n\n\n";
		result = StaxUtilities.normalizeNewlines(input);
		assertEquals(expectedResult, result);

		input = "  foo\r\nb a r\r\nfoo  bar  \r\n  ";
		expectedResult = "  foo\nb a r\nfoo  bar  \n  ";
		result = StaxUtilities.normalizeNewlines(input);
		assertEquals(expectedResult, result);
	}

	public void testReplaceWhiteSpace()
	{
		String input;
		String result;
		String expectedResult;

		input = null;
		expectedResult = null;
		result = StaxUtilities.replaceWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "";
		expectedResult = "";
		result = StaxUtilities.replaceWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foobar";
		expectedResult = "foobar";
		result = StaxUtilities.replaceWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foo\nbar\nfoobar";
		expectedResult = "foo bar foobar";
		result = StaxUtilities.replaceWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foo\r\nbar\r\nfoobar";
		expectedResult = "foo  bar  foobar";
		result = StaxUtilities.replaceWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foo\r\n\r\nbar\r\nfoobar";
		expectedResult = "foo    bar  foobar";
		result = StaxUtilities.replaceWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foo\n\rbar\n\rfoobar";
		expectedResult = "foo  bar  foobar";
		result = StaxUtilities.replaceWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foo\rbar\rfoobar";
		expectedResult = "foo bar foobar";
		result = StaxUtilities.replaceWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foo\r\rbar\rfoobar";
		expectedResult = "foo  bar foobar";
		result = StaxUtilities.replaceWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foo\r\n\r\rbar\rfoobar\r\r\r";
		expectedResult = "foo    bar foobar   ";
		result = StaxUtilities.replaceWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "  foo\r\nb a r\r\nfoo  bar  \r\n  ";
		expectedResult = "  foo  b a r  foo  bar      ";
		result = StaxUtilities.replaceWhiteSpace(input);
		assertEquals(expectedResult, result);
	}

	public void testCollapseWhiteSpace()
	{
		String input;
		String result;
		String expectedResult;

		input = null;
		expectedResult = null;
		result = StaxUtilities.collapseWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "";
		expectedResult = "";
		result = StaxUtilities.collapseWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foobar";
		expectedResult = "foobar";
		result = StaxUtilities.collapseWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foo\nbar\nfoobar";
		expectedResult = "foo bar foobar";
		result = StaxUtilities.collapseWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foo\r\nbar\r\nfoobar";
		expectedResult = "foo bar foobar";
		result = StaxUtilities.collapseWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foo\r\n\r\nbar\r\nfoobar";
		expectedResult = "foo bar foobar";
		result = StaxUtilities.collapseWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foo\n\rbar\n\rfoobar";
		expectedResult = "foo bar foobar";
		result = StaxUtilities.collapseWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foo\rbar\rfoobar";
		expectedResult = "foo bar foobar";
		result = StaxUtilities.collapseWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foo\r\rbar\rfoobar";
		expectedResult = "foo bar foobar";
		result = StaxUtilities.collapseWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "foo\r\n\r\rbar\rfoobar\r\r\r";
		expectedResult = "foo bar foobar";
		result = StaxUtilities.collapseWhiteSpace(input);
		assertEquals(expectedResult, result);

		input = "  foo\r\nb a r\r\nfoo  bar  \r\n  ";
		expectedResult = "foo b a r foo bar";
		result = StaxUtilities.collapseWhiteSpace(input);
		assertEquals(expectedResult, result);
	}
}
