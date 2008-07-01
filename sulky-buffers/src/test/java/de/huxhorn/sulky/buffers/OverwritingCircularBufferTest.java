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
package de.huxhorn.sulky.buffers;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverwritingCircularBufferTest
		extends TestCase
{
	private final Logger logger = LoggerFactory.getLogger(OverwritingCircularBufferTest.class);

	private static final int TEST_BUFFER_SIZE = 5;
	private OverwritingCircularBuffer<Long> instance;

	protected void setUp() throws Exception
	{
		instance = new OverwritingCircularBuffer<Long>(TEST_BUFFER_SIZE);
	}

	public void testEmpty()
	{
		assertTrue("Instance is not empty!", instance.isEmpty());
		assertTrue("Instance is full!", !instance.isFull());
		assertEquals("Size doesn't match!", 0, instance.getAvailableElements());
		assertEquals("getBufferSize doesn't match!", TEST_BUFFER_SIZE, instance.getBufferSize());
		assertEquals("overflowCounter doesn't match!", 0, instance.getOverflowCounter());
		Iterator<Long> iterator = instance.iterator();
		assertTrue("iterator has next!", !iterator.hasNext());
	}

	public void testOne()
	{
		instance.add((long) 1);

		assertTrue("Instance is empty!", !instance.isEmpty());
		assertTrue("Instance is full!", !instance.isFull());
		assertEquals("Size doesn't match!", 1, instance.getAvailableElements());
		assertEquals("getBufferSize doesn't match!", TEST_BUFFER_SIZE, instance.getBufferSize());
		assertEquals("overflowCounter doesn't match!", 0, instance.getOverflowCounter());
		Iterator<Long> iterator = instance.iterator();
		assertTrue("iterator doesn't have next!", iterator.hasNext());
		Long element = iterator.next();
		Long getRelativeValue = instance.getRelative(0);
		Long getValue = instance.get(0);
		if (logger.isInfoEnabled())
			logger.info("Element #{}: iterValue={}, getRelativeValue={}, getValue={}", new Object[]{0, element, getRelativeValue, getValue});
		assertEquals("Unexpected value returned by iterator!", (Long) (long) 1, element);
		assertEquals("Iterator and getRelative values differ!", element, getRelativeValue);
		assertEquals("Iterator and get values differ!", element, getValue);
	}

	public void testNearlyFull()
	{
		for (int i = 0; i < TEST_BUFFER_SIZE - 1; i++)
		{
			instance.add((long) i);
		}

		assertTrue("Instance is empty!", !instance.isEmpty());
		assertTrue("Instance is full!", !instance.isFull());
		assertEquals("Size doesn't match!", TEST_BUFFER_SIZE - 1, instance.getAvailableElements());
		assertEquals("getBufferSize doesn't match!", TEST_BUFFER_SIZE, instance.getBufferSize());
		assertEquals("overflowCounter doesn't match!", 0, instance.getOverflowCounter());
		Iterator<Long> iterator = instance.iterator();
		assertTrue("iterator doesn't have next!", iterator.hasNext());
		for (int i = 0; i < TEST_BUFFER_SIZE - 1; i++)
		{
			Long element = iterator.next();
			Long getRelativeValue = instance.getRelative(i);
			Long getValue = instance.get(i);
			if (logger.isInfoEnabled())
				logger.info("Element #{}: iterValue={}, getRelativeValue={}", new Object[]{i, element, getRelativeValue});
			assertEquals("Unexpected value returned by iterator!", (Long) (long) i, element);
			assertEquals("Iterator and getRelative values differ!", element, getRelativeValue);
			assertEquals("Iterator and get values differ!", element, getValue);
		}
	}

	public void testFull()
	{
		for (int i = 0; i < TEST_BUFFER_SIZE; i++)
		{
			instance.add((long) i);
		}

		assertTrue("Instance is empty!", !instance.isEmpty());
		assertTrue("Instance isn't full!", instance.isFull());
		assertEquals("Size doesn't match!", TEST_BUFFER_SIZE, instance.getAvailableElements());
		assertEquals("getBufferSize doesn't match!", TEST_BUFFER_SIZE, instance.getBufferSize());
		assertEquals("overflowCounter doesn't match!", 0, instance.getOverflowCounter());
		Iterator<Long> iterator = instance.iterator();
		assertTrue("iterator doesn't have next!", iterator.hasNext());
		for (int i = 0; i < TEST_BUFFER_SIZE; i++)
		{
			Long element = iterator.next();
			Long getRelativeValue = instance.getRelative(i);
			Long getValue = instance.get(i);
			if (logger.isInfoEnabled())
				logger.info("Element #{}: iterValue={}, getRelativeValue={}", new Object[]{i, element, getRelativeValue});
			assertEquals("Unexpected value returned by iterator!", (Long) (long) i, element);
			assertEquals("Iterator and getRelative values differ!", element, getRelativeValue);
			assertEquals("Iterator and get values differ!", element, getValue);
		}
	}

	public void testOverflowOne()
	{
		for (int i = 0; i < TEST_BUFFER_SIZE; i++)
		{
			instance.add((long) i);
		}
		instance.add((long) TEST_BUFFER_SIZE);

		assertTrue("Instance is empty!", !instance.isEmpty());
		assertTrue("Instance isn't full!", instance.isFull());
		assertEquals("Size doesn't match!", TEST_BUFFER_SIZE, instance.getAvailableElements());
		assertEquals("getBufferSize doesn't match!", TEST_BUFFER_SIZE, instance.getBufferSize());
		long overflowCounter=instance.getOverflowCounter();
		assertEquals("overflowCounter doesn't match!", 1, overflowCounter);
		Iterator<Long> iterator = instance.iterator();
		assertTrue("iterator doesn't have next!", iterator.hasNext());
		for (int i = 0; i < TEST_BUFFER_SIZE; i++)
		{
			Long element = iterator.next();
			Long getRelativeValue = instance.getRelative(i);
			Long getValue = instance.get(i+overflowCounter);
			if (logger.isInfoEnabled())
				logger.info("Element #{}: iterValue={}, getRelativeValue={}", new Object[]{i, element, getRelativeValue});
			assertEquals("Unexpected value returned by iterator!", (Long) (long) (i + 1), element);
			assertEquals("Iterator and getRelative values differ!", element, getRelativeValue);
			assertEquals("Iterator and get values differ!", element, getValue);
		}
	}

	public void testOverflowDouble()
	{
		for (int i = 0; i < TEST_BUFFER_SIZE * 2; i++)
		{
			instance.add((long) i);
		}

		assertTrue("Instance is empty!", !instance.isEmpty());
		assertTrue("Instance isn't full!", instance.isFull());
		assertEquals("Size doesn't match!", TEST_BUFFER_SIZE, instance.getAvailableElements());
		assertEquals("getBufferSize doesn't match!", TEST_BUFFER_SIZE, instance.getBufferSize());
		long overflowCounter=instance.getOverflowCounter();
		assertEquals("overflowCounter doesn't match!", TEST_BUFFER_SIZE, overflowCounter);
		Iterator<Long> iterator = instance.iterator();
		assertTrue("iterator doesn't have next!", iterator.hasNext());
		for (int i = 0; i < TEST_BUFFER_SIZE; i++)
		{
			Long element = iterator.next();
			Long getRelativeValue = instance.getRelative(i);
			Long getValue = instance.get(i+overflowCounter);
			if (logger.isInfoEnabled())
				logger.info("Element #{}: iterValue={}, getRelativeValue={}", new Object[]{i, element, getRelativeValue});
			assertEquals("Unexpected value returned by iterator!", (Long) (long) (i + TEST_BUFFER_SIZE), element);
			assertEquals("Iterator and getRelative values differ!", element, getRelativeValue);
			assertEquals("Iterator and get values differ!", element, getValue);
		}
	}

	public void testAddAllList()
	{
		List<Long> values = new ArrayList<Long>();
		for (int i = 0; i < 4 * TEST_BUFFER_SIZE; i++)
		{
			values.add((long) i);
		}
		if (logger.isInfoEnabled()) logger.info("Adding values: {}", values);
		instance.addAll(values);
		if (logger.isInfoEnabled()) logger.info("Buffer after adding: {}", instance);

		assertTrue("Instance is empty!", !instance.isEmpty());
		assertTrue("Instance isn't full!", instance.isFull());
		assertEquals("Size doesn't match!", TEST_BUFFER_SIZE, instance.getAvailableElements());
		assertEquals("getBufferSize doesn't match!", TEST_BUFFER_SIZE, instance.getBufferSize());
		long overflowCounter=instance.getOverflowCounter();
		assertEquals("overflowCounter doesn't match!", 3 * TEST_BUFFER_SIZE, overflowCounter);
		Iterator<Long> iterator = instance.iterator();
		assertTrue("iterator doesn't have next!", iterator.hasNext());
		for (int i = 0; i < TEST_BUFFER_SIZE; i++)
		{
			Long element = iterator.next();
			Long getRelativeValue = instance.getRelative(i);
			Long getValue = instance.get(i+overflowCounter);
			if (logger.isInfoEnabled())
				logger.info("Element #{}: iterValue={}, getRelativeValue={}", new Object[]{i, element, getRelativeValue});
			assertEquals("Unexpected value returned by iterator!", (Long) (long) (i + 3 * TEST_BUFFER_SIZE), element);
			assertEquals("Iterator and getRelative values differ!", element, getRelativeValue);
			assertEquals("Iterator and get values differ!", element, getValue);
		}
	}

	public void testAddAllArray()
	{
		Long[] values = new Long[4 * TEST_BUFFER_SIZE];
		for (int i = 0; i < 4 * TEST_BUFFER_SIZE; i++)
		{
			values[i] = (long) i;
		}
		if (logger.isInfoEnabled()) logger.info("Adding values: {}", values);
		instance.addAll(values);
		if (logger.isInfoEnabled()) logger.info("Buffer after adding: {}", instance);

		assertTrue("Instance is empty!", !instance.isEmpty());
		assertTrue("Instance isn't full!", instance.isFull());
		assertEquals("Size doesn't match!", TEST_BUFFER_SIZE, instance.getAvailableElements());
		assertEquals("getBufferSize doesn't match!", TEST_BUFFER_SIZE, instance.getBufferSize());
		long overflowCounter=instance.getOverflowCounter();
		assertEquals("overflowCounter doesn't match!", 3 * TEST_BUFFER_SIZE, overflowCounter);
		Iterator<Long> iterator = instance.iterator();
		assertTrue("iterator doesn't have next!", iterator.hasNext());
		for (int i = 0; i < TEST_BUFFER_SIZE; i++)
		{
			Long element = iterator.next();
			Long getRelativeValue = instance.getRelative(i);
			Long getValue = instance.get(i+overflowCounter);
			if (logger.isInfoEnabled())
				logger.info("Element #{}: iterValue={}, getRelativeValue={}", new Object[]{i, element, getRelativeValue});
			assertEquals("Unexpected value returned by iterator!", (Long) (long) (i + 3 * TEST_BUFFER_SIZE), element);
			assertEquals("Iterator and getRelative values differ!", element, getRelativeValue);
			assertEquals("Iterator and get values differ!", element, getValue);
		}
	}

	public void testAddRemove()
	{
		internalTestRemove(instance, 0);
		internalTestRemove(instance, 3);
		internalTestRemove(instance, 7);
		internalTestRemove(instance, 17);
		internalTestRemove(instance, 4 * TEST_BUFFER_SIZE);
		instance = new OverwritingCircularBuffer<Long>(17);
		internalTestRemove(instance, 23);

		// absurd...
		instance = new OverwritingCircularBuffer<Long>(1);
		internalTestRemove(instance, 17);
	}

	public void testAddRemoveAll()
	{
		internalTestRemoveAll(instance, 0);
		internalTestRemoveAll(instance, 3);
		internalTestRemoveAll(instance, 7);
		internalTestRemoveAll(instance, 17);
		internalTestRemoveAll(instance, 4 * TEST_BUFFER_SIZE);
		instance = new OverwritingCircularBuffer<Long>(17);
		internalTestRemoveAll(instance, 23);

		// absurd...
		instance = new OverwritingCircularBuffer<Long>(1);
		internalTestRemoveAll(instance, 17);
	}

	public void internalTestRemove(OverwritingCircularBuffer<Long> impl, int valueCount)
	{
		long bufferSize = impl.getBufferSize();
		if (logger.isInfoEnabled())
			logger.info("Executing add-remove-reset test with valueCount={} and buffer.getBufferSize={}.", valueCount, bufferSize);
		List<Long> values = new ArrayList<Long>(valueCount);
		for (int i = 0; i < valueCount; i++)
		{
			values.add((long) i);
		}
		if (logger.isInfoEnabled()) logger.info("Adding values: {}", values);
		impl.addAll(values);
		if (logger.isInfoEnabled()) logger.info("Buffer after adding: {}", impl);

		if (valueCount == 0)
		{
			assertTrue("Instance isn't empty!", impl.isEmpty());
		}
		else
		{
			assertTrue("Instance is empty!", !impl.isEmpty());
		}
		long expectedElementCount = valueCount;
		long expectedOverflowCount = 0;
		if (valueCount > bufferSize)
		{
			expectedElementCount = bufferSize;
			expectedOverflowCount = valueCount - bufferSize;
			assertTrue("Instance isn't full!", impl.isFull());
		}
		else
		{
			assertTrue("Instance is full!", !impl.isFull());
		}
		assertEquals("Available doesn't match!", expectedElementCount, impl.getAvailableElements());
		long overflowCounter=instance.getOverflowCounter();
		assertEquals("overflowCounter doesn't match!", expectedOverflowCount, overflowCounter);
		assertEquals("Size doesn't match!", valueCount, impl.getSize());
		for (int i = 0; i < expectedElementCount; i++)
		{
			assertTrue("Instance is empty!", !impl.isEmpty());
			assertEquals("Size doesn't match!", expectedElementCount - i, impl.getAvailableElements());
			if (logger.isDebugEnabled()) logger.debug("Size before removal of element #{}: {}", i, impl.getAvailableElements());
			Long removeValue = impl.removeFirst();
			if (logger.isDebugEnabled()) logger.debug("Size after removal of element #{}: {}", i, impl.getAvailableElements());

			if (logger.isInfoEnabled()) logger.info("Element #{}: removeValue={}", new Object[]{i, removeValue});
			assertEquals("Unexpected value returned by remove!", (Long) (expectedOverflowCount + i), removeValue);

			assertTrue("Instance is full!", !impl.isFull());
			assertEquals("Size doesn't match!", expectedElementCount - i - 1, impl.getAvailableElements());
		}
		assertTrue("Instance isn't empty!", impl.isEmpty());
		Long removeValue = impl.removeFirst();
		if (logger.isInfoEnabled())
			logger.info("Element #{}: removeValue={}", new Object[]{expectedElementCount, removeValue});
		assertNull("Remove after last element returned a value: " + removeValue, removeValue);

		assertEquals("overflowCounter doesn't match!", expectedOverflowCount, impl.getOverflowCounter());
		assertEquals("getSize doesn't match!", valueCount, impl.getSize());
		impl.reset();
		assertEquals("overflowCounter doesn't match!", 0, impl.getOverflowCounter());
		assertEquals("getSize doesn't match!", 0, impl.getSize());
		assertTrue("Instance isn't empty!", impl.isEmpty());
		assertTrue("Instance is full!", !impl.isFull());
	}

	public void internalTestRemoveAll(OverwritingCircularBuffer<Long> impl, int valueCount)
	{
		long bufferSize = impl.getBufferSize();
		if (logger.isInfoEnabled())
			logger.info("Executing add-remove-reset test with valueCount={} and buffer.getBufferSize={}.", valueCount, bufferSize);
		List<Long> values = new ArrayList<Long>(valueCount);
		for (int i = 0; i < valueCount; i++)
		{
			values.add((long) i);
		}
		if (logger.isInfoEnabled()) logger.info("Adding values: {}", values);
		impl.addAll(values);
		if (logger.isInfoEnabled()) logger.info("Buffer after adding: {}", impl);

		if (valueCount == 0)
		{
			assertTrue("Instance isn't empty!", impl.isEmpty());
		}
		else
		{
			assertTrue("Instance is empty!", !impl.isEmpty());
		}
		long expectedElementCount = valueCount;
		long expectedOverflowCount = 0;
		if (valueCount > bufferSize)
		{
			expectedElementCount = bufferSize;
			expectedOverflowCount = valueCount - bufferSize;
			assertTrue("Instance isn't full!", impl.isFull());
		}
		else
		{
			assertTrue("Instance is full!", !impl.isFull());
		}
		assertEquals("Size doesn't match!", expectedElementCount, impl.getAvailableElements());
		assertEquals("overflowCounter doesn't match!", expectedOverflowCount, impl.getOverflowCounter());
		assertEquals("getSize doesn't match!", valueCount, impl.getSize());

		List<Long> removedList = impl.removeAll();
		assertTrue("Instance isn't empty!", impl.isEmpty());
		assertEquals("overflowCounter doesn't match!", expectedOverflowCount, impl.getOverflowCounter());
		assertEquals("getSize doesn't match!", valueCount, impl.getSize());


		for (int i = 0; i < expectedElementCount; i++)
		{
			Long removeValue = removedList.get(i);
			if (logger.isInfoEnabled()) logger.info("Element #{}: removeValue={}", new Object[]{i, removeValue});
			assertEquals("Unexpected value returned by remove!", (Long) (expectedOverflowCount + i), removeValue);
		}
		impl.reset();
		assertEquals("overflowCounter doesn't match!", 0, impl.getOverflowCounter());
		assertEquals("getSize doesn't match!", 0, impl.getSize());
		assertTrue("Instance isn't empty!", impl.isEmpty());
		assertTrue("Instance is full!", !impl.isFull());
	}
}
