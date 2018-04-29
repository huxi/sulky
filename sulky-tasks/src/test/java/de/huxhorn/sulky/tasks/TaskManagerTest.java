/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2018 Joern Huxhorn
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
 * Copyright 2007-2018 Joern Huxhorn
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

package de.huxhorn.sulky.tasks;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TaskManagerTest
{
	private final Logger logger = LoggerFactory.getLogger(TaskManagerTest.class);

	private TaskManager<Integer> instance;
	private String taskName;

	@BeforeClass
	public static void enableHeadless()
	{
		System.setProperty("java.awt.headless", "true");
	}

	@Before
	public void setUp()
	{
		Toolkit tk = Toolkit.getDefaultToolkit();
		if(logger.isDebugEnabled()) logger.debug("Toolkit: {}", tk);
		instance = new TaskManager<>();
		taskName = "TaskName";
	}

	@After
	public void shutDown()
	{
		if(instance.getState() == TaskManager.State.RUNNING)
		{
			instance.shutDown();
		}
	}

	@Test(expected = IllegalStateException.class)
	public void notRunning()
	{
		instance.startTask(() -> null, "Won't work");
	}

	@Test
	public void states()
	{
		assertEquals(TaskManager.State.INITIALIZED, instance.getState());
		instance.startUp();
		assertEquals(TaskManager.State.RUNNING, instance.getState());
		instance.shutDown();
		assertEquals(TaskManager.State.STOPPED, instance.getState());
	}

	/**
	 * No exception, second shutDown should be silently ignored.
	 */
	@Test
	public void shutDownTwice()
	{
		instance.startUp();
		instance.shutDown();
		instance.shutDown();
	}

	@Test(expected = IllegalStateException.class)
	public void startUpTwice()
	{
		instance.startUp();
		instance.startUp();
	}

	@Test(expected = IllegalStateException.class)
	public void restart()
	{
		instance.startUp();
		instance.shutDown();
		instance.startUp();
	}

	@Test(expected = IllegalArgumentException.class)
	public void missingName()
	{
		instance.startUp();
		instance.startTask(() -> null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void missingExecutor()
	{
		//noinspection ConstantConditions
		new TaskManager(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void missingExecutor2()
	{
		//noinspection ConstantConditions
		new TaskManager(null, true);
	}

	@Test
	public void withDescription()
	{
		instance.startUp();
		String name = "TaskName";
		String description = "Task description";
		Task<Integer> task = instance.startTask(() -> null, name, description);
		assertEquals(name, task.getName());
		assertEquals(description, task.getDescription());
		assertNull(task.getMetaData());
	}

	@Test
	public void withDescriptionAndMetaData()
	{
		instance.startUp();
		String name = "TaskName";
		String description = "Task description";
		Map<String, String> metaData = new HashMap<>();
		metaData.put("foo", "bar");
		Task<Integer> task = instance.startTask(() -> null, name, description, metaData);
		assertEquals(name, task.getName());
		assertEquals(description, task.getDescription());
		assertEquals(metaData, task.getMetaData());
	}

	@Test
	public void sleepingCallableNoEDT()
		throws InterruptedException
	{
		instance.startUp();
		DispatchTestTaskListener listener = new DispatchTestTaskListener(false);
		instance.addTaskListener(listener);
		Callable<Integer> callable = new SleepingCallable("C1", 20);
		instance.startTask(callable, taskName);

		Thread.sleep(250);

		assertFalse(listener.isFailed());
	}

	@Test
	public void sleepingCallableEDT()
		throws InterruptedException
	{
		instance.startUp();
		instance.setUsingEventQueue(true);
		DispatchTestTaskListener listener = new DispatchTestTaskListener(true);
		instance.addTaskListener(listener);
		Callable<Integer> callable = new SleepingCallable("C1", 20);
		instance.startTask(callable, taskName);

		Thread.sleep(250);

		assertFalse(listener.isFailed());
	}

	@Test
	public void sleepingCallable()
		throws InterruptedException
	{
		instance.startUp();
		Callable<Integer> callable = new SleepingCallable("C1", 2000);
		Task<Integer> task = instance.startTask(callable, taskName);
		long taskId = task.getId();
		assertEquals(1, taskId);
		assertSame(task, instance.getTaskById(taskId));
		assertSame(task, instance.getTaskByCallable(callable));

		Future<Integer> future = task.getFuture();
		assertFalse(future.isCancelled());
		assertFalse(future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(2500);
		assertFalse(future.isCancelled());
		assertTrue(future.isDone());
		tasks = instance.getTasks();
		assertEquals(0, tasks.size());
	}

	@Test
	public void sleepingExceptionCallableNoEDT()
		throws InterruptedException
	{
		instance.startUp();
		DispatchTestTaskListener listener = new DispatchTestTaskListener(false);
		instance.addTaskListener(listener);
		Callable<Integer> callable = new SleepingExceptionCallable("C1", 20);
		instance.startTask(callable, taskName);

		Thread.sleep(250);

		assertFalse(listener.isFailed());
	}

	@Test
	public void sleepingExceptionCallableEDT()
		throws InterruptedException
	{
		instance.startUp();
		instance.setUsingEventQueue(true);
		DispatchTestTaskListener listener = new DispatchTestTaskListener(true);
		instance.addTaskListener(listener);
		Callable<Integer> callable = new SleepingExceptionCallable("C1", 20);
		instance.startTask(callable, taskName);

		Thread.sleep(250);

		assertFalse(listener.isFailed());
	}


	@Test
	public void sleepingExceptionCallable()
		throws InterruptedException
	{
		instance.startUp();
		Callable<Integer> callable = new SleepingExceptionCallable("C1", 2000);
		Task<Integer> task = instance.startTask(callable, taskName);
		long taskId = task.getId();
		assertEquals(1, taskId);
		assertSame(task, instance.getTaskById(taskId));
		assertSame(task, instance.getTaskByCallable(callable));
		Future<Integer> future = task.getFuture();
		assertFalse(future.isCancelled());
		assertFalse(future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(2500);
		assertFalse(future.isCancelled());
		assertTrue(future.isDone());
		tasks = instance.getTasks();
		assertEquals(0, tasks.size());
	}

	@Test
	public void sleepingProgressingCallableNoEDT()
		throws InterruptedException
	{
		instance.startUp();
		DispatchTestTaskListener listener = new DispatchTestTaskListener(false);
		instance.addTaskListener(listener);
		Callable<Integer> callable = new SleepingProgressingCallable("C1", 20);
		instance.startTask(callable, taskName);

		Thread.sleep(250);

		assertFalse(listener.isFailed());
	}

	@Test
	public void sleepingProgressingCallableEDT()
		throws InterruptedException
	{
		instance.startUp();
		instance.setUsingEventQueue(true);
		DispatchTestTaskListener listener = new DispatchTestTaskListener(true);
		instance.addTaskListener(listener);
		Callable<Integer> callable = new SleepingProgressingCallable("C1", 20);
		instance.startTask(callable, taskName);

		Thread.sleep(250);

		assertFalse(listener.isFailed());
	}

	@Test
	public void sleepingProgressingCallable()
		throws InterruptedException
	{
		instance.startUp();
		Callable<Integer> callable = new SleepingProgressingCallable("C1", 20);
		Task<Integer> task = instance.startTask(callable, taskName);
		long taskId = task.getId();
		assertEquals(1, taskId);
		assertSame(task, instance.getTaskById(taskId));
		assertSame(task, instance.getTaskByCallable(callable));
		Future<Integer> future = task.getFuture();
		assertFalse(future.isCancelled());
		assertFalse(future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(1500);
		assertFalse(future.isCancelled());
		assertTrue(future.isDone());
		tasks = instance.getTasks();
		assertEquals(0, tasks.size());
	}

	@Test
	public void sleepingExceptionProgressingCallableNoEDT()
		throws InterruptedException
	{
		instance.startUp();
		DispatchTestTaskListener listener = new DispatchTestTaskListener(false);
		instance.addTaskListener(listener);
		Callable<Integer> callable = new SleepingExceptionProgressingCallable("C1", 20);
		instance.startTask(callable, taskName);

		Thread.sleep(250);

		assertFalse(listener.isFailed());
	}

	@Test
	public void sleepingExceptionProgressingCallableEDT()
		throws InterruptedException
	{
		instance.startUp();
		instance.setUsingEventQueue(true);
		DispatchTestTaskListener listener = new DispatchTestTaskListener(true);
		instance.addTaskListener(listener);
		Callable<Integer> callable = new SleepingExceptionProgressingCallable("C1", 20);
		instance.startTask(callable, taskName);

		Thread.sleep(250);

		assertFalse(listener.isFailed());
	}

	@Test
	public void sleepingExceptionProgressingCallable()
		throws InterruptedException
	{
		instance.startUp();
		Callable<Integer> callable = new SleepingExceptionProgressingCallable("C1", 200);
		Task<Integer> task = instance.startTask(callable, taskName);
		long taskId = task.getId();
		assertEquals(1, taskId);
		assertSame(task, instance.getTaskById(taskId));
		assertSame(task, instance.getTaskByCallable(callable));
		Future<Integer> future = task.getFuture();
		assertFalse(future.isCancelled());
		assertFalse(future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(2500);
		assertFalse(future.isCancelled());
		assertTrue(future.isDone());
		tasks = instance.getTasks();
		assertEquals(0, tasks.size());
	}

	@Test
	public void sleepingCallableTaskListener()
		throws InterruptedException
	{
		instance.startUp();
		TestTaskListener taskListener = new TestTaskListener();
		instance.addTaskListener(taskListener);
		Callable<Integer> callable = new SleepingCallable("C1", 200);
		Task<Integer> task = instance.startTask(callable, taskName);
		long taskId = task.getId();
		assertEquals(1, taskId);
		assertSame(task, instance.getTaskById(taskId));
		assertSame(task, instance.getTaskByCallable(callable));
		Future<Integer> future = task.getFuture();
		assertFalse(future.isCancelled());
		assertFalse(future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(2500);
		assertFalse(future.isCancelled());
		assertTrue(future.isDone());

		List<String> messages = taskListener.getMessages();
		if(logger.isInfoEnabled()) logger.info("Messages: {}", messages);
		List<String> expectedMsgs = new ArrayList<>();
		expectedMsgs.add(TestTaskListener.CREATED + callable);
		expectedMsgs.add(TestTaskListener.FINISHED + callable);
		assertEquals(expectedMsgs, messages);
		tasks = instance.getTasks();
		assertEquals(0, tasks.size());
	}

	@Test
	public void sleepingProgressingCallableTaskListener()
		throws InterruptedException
	{
		instance.startUp();
		TestTaskListener taskListener = new TestTaskListener();
		instance.addTaskListener(taskListener);
		Callable<Integer> callable = new SleepingProgressingCallable("C1", 200);
		Task<Integer> task = instance.startTask(callable, taskName);
		long taskId = task.getId();
		assertEquals(1, taskId);
		assertSame(task, instance.getTaskById(taskId));
		assertSame(task, instance.getTaskByCallable(callable));
		Future<Integer> future = task.getFuture();
		assertFalse(future.isCancelled());
		assertFalse(future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(2500);
		assertFalse(future.isCancelled());
		assertTrue(future.isDone());

		List<String> messages = taskListener.getMessages();
		if(logger.isInfoEnabled()) logger.info("Messages: {}", messages);
		List<String> expectedMsgs = new ArrayList<>();
		expectedMsgs.add(TestTaskListener.CREATED + callable);
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 0");
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 10");
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 20");
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 30");
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 40");
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 50");
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 60");
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 70");
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 80");
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 90");
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 100");
		expectedMsgs.add(TestTaskListener.FINISHED + callable);
		assertEquals(expectedMsgs, messages);
		tasks = instance.getTasks();
		assertEquals(0, tasks.size());
	}

	@Test
	public void sleepingExceptionCallableTaskListener()
		throws InterruptedException
	{
		instance.startUp();
		TestTaskListener taskListener = new TestTaskListener();
		instance.addTaskListener(taskListener);
		Callable<Integer> callable = new SleepingExceptionCallable("C1", 200);
		Task<Integer> task = instance.startTask(callable, taskName);
		long taskId = task.getId();
		assertEquals(1, taskId);
		assertSame(task, instance.getTaskById(taskId));
		assertSame(task, instance.getTaskByCallable(callable));
		Future<Integer> future = task.getFuture();
		assertFalse(future.isCancelled());
		assertFalse(future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(2500);
		assertFalse(future.isCancelled());
		assertTrue(future.isDone());

		List<String> messages = taskListener.getMessages();
		if(logger.isInfoEnabled()) logger.info("Messages: {}", messages);
		List<String> expectedMsgs = new ArrayList<>();
		expectedMsgs.add(TestTaskListener.CREATED + callable);
		expectedMsgs.add(TestTaskListener.FAILED + callable + " java.lang.RuntimeException");
		assertEquals(expectedMsgs, messages);
		tasks = instance.getTasks();
		assertEquals(0, tasks.size());
	}

	@Test
	public void sleepingExceptionProgressingCallableTaskListener()
		throws InterruptedException
	{
		instance.startUp();
		TestTaskListener taskListener = new TestTaskListener();
		instance.addTaskListener(taskListener);
		Callable<Integer> callable = new SleepingExceptionProgressingCallable("C1", 200);
		Task<Integer> task = instance.startTask(callable, taskName);
		long taskId = task.getId();
		assertEquals(1, taskId);
		assertSame(task, instance.getTaskById(taskId));
		assertSame(task, instance.getTaskByCallable(callable));
		Future<Integer> future = task.getFuture();
		assertFalse(future.isCancelled());
		assertFalse(future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(2500);
		assertFalse(future.isCancelled());
		assertTrue(future.isDone());

		List<String> messages = taskListener.getMessages();
		if(logger.isInfoEnabled()) logger.info("Messages: {}", messages);
		List<String> expectedMsgs = new ArrayList<>();
		expectedMsgs.add(TestTaskListener.CREATED + callable);
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 0");
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 10");
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 20");
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 30");
		expectedMsgs.add(TestTaskListener.PROGRESS + callable + " 40");
		expectedMsgs.add(TestTaskListener.FAILED + callable + " java.lang.RuntimeException");
		assertEquals(expectedMsgs, messages);
		tasks = instance.getTasks();
		assertEquals(0, tasks.size());
	}

	@Test
	public void cancelSleepingCallable()
		throws InterruptedException
	{
		instance.startUp();
		Callable<Integer> callable = new SleepingCallable("C1", 2000);
		Task<Integer> task = instance.startTask(callable, taskName);
		long taskId = task.getId();
		assertEquals(1, taskId);
		assertSame(task, instance.getTaskById(taskId));
		assertSame(task, instance.getTaskByCallable(callable));
		Future<Integer> future = task.getFuture();
		assertFalse(future.isCancelled());
		assertFalse(future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		future.cancel(true);
		Thread.sleep(2500);
		assertTrue(future.isCancelled());
		assertTrue(future.isDone());
		tasks = instance.getTasks();
		assertEquals(0, tasks.size());
	}

	@Test
	public void cancelSleepingProgressingCallable()
		throws InterruptedException
	{
		instance.startUp();
		Callable<Integer> callable = new SleepingProgressingCallable("C1", 200);
		Task<Integer> task = instance.startTask(callable, taskName);
		long taskId = task.getId();
		assertEquals(1, taskId);
		assertSame(task, instance.getTaskById(taskId));
		assertSame(task, instance.getTaskByCallable(callable));
		Future<Integer> future = task.getFuture();
		assertFalse(future.isCancelled());
		assertFalse(future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		future.cancel(true);
		Thread.sleep(2500);
		assertTrue(future.isCancelled());
		assertTrue(future.isDone());
		tasks = instance.getTasks();
		assertEquals(0, tasks.size());
	}

	@Test
	public void cancelSleepingCallableTaskListener()
		throws InterruptedException
	{
		instance.startUp();
		TestTaskListener taskListener = new TestTaskListener();
		instance.addTaskListener(taskListener);
		Callable<Integer> callable = new SleepingProgressingCallable("C1", 200);
		Task<Integer> task = instance.startTask(callable, taskName);
		long taskId = task.getId();
		assertEquals(1, taskId);
		assertSame(task, instance.getTaskById(taskId));
		assertSame(task, instance.getTaskByCallable(callable));
		Future<Integer> future = task.getFuture();
		assertFalse(future.isCancelled());
		assertFalse(future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		future.cancel(true);
		Thread.sleep(2500);
		assertTrue(future.isCancelled());
		assertTrue(future.isDone());

		List<String> messages = taskListener.getMessages();
		if(logger.isInfoEnabled()) logger.info("Messages: {}", messages);
		assertTrue(messages.contains(TestTaskListener.CREATED + callable));
		assertTrue(messages.contains(TestTaskListener.CANCELED + callable));
		tasks = instance.getTasks();
		assertEquals(0, tasks.size());
	}

	@Test
	public void cancelSleepingProgressingCallableTaskListener()
		throws InterruptedException
	{
		instance.startUp();
		TestTaskListener taskListener = new TestTaskListener();
		instance.addTaskListener(taskListener);
		Callable<Integer> callable = new SleepingProgressingCallable("C1", 200);
		Task<Integer> task = instance.startTask(callable, taskName);
		long taskId = task.getId();
		assertEquals(1, taskId);
		assertSame(task, instance.getTaskById(taskId));
		assertSame(task, instance.getTaskByCallable(callable));
		Future<Integer> future = task.getFuture();
		assertFalse(future.isCancelled());
		assertFalse(future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(500);
		future.cancel(true);
		Thread.sleep(2000);
		assertTrue(future.isCancelled());
		assertTrue(future.isDone());

		List<String> messages = taskListener.getMessages();
		if(logger.isInfoEnabled()) logger.info("Messages: {}", messages);
		assertTrue(messages.contains(TestTaskListener.CREATED + callable));
		assertTrue(messages.contains(TestTaskListener.CANCELED + callable));
		tasks = instance.getTasks();
		assertEquals(0, tasks.size());
	}

	@Test
	public void multiTaskListener()
		throws InterruptedException
	{
		instance.startUp();
		TestTaskListener taskListener = new TestTaskListener();
		instance.addTaskListener(taskListener);

		Callable<Integer> prog1 = new SleepingProgressingCallable("Prog1", 200);
		Task<Integer> prog1Task = instance.startTask(prog1, "Prog1");
		assertEquals(1, prog1Task.getId());
		assertSame(prog1Task, instance.getTaskById(1));
		assertSame(prog1Task, instance.getTaskByCallable(prog1));

		Callable<Integer> prog2 = new SleepingProgressingCallable("Prog2", 200);
		Task<Integer> prog2Task = instance.startTask(prog2, "Prog2");
		assertEquals(2, prog2Task.getId());
		assertSame(prog2Task, instance.getTaskById(2));
		assertSame(prog2Task, instance.getTaskByCallable(prog2));

		Callable<Integer> prog3 = new SleepingExceptionProgressingCallable("Prog3", 200);
		Task<Integer> prog3Task = instance.startTask(prog3, "Prog3");
		assertEquals(3, prog3Task.getId());
		assertSame(prog3Task, instance.getTaskById(3));
		assertSame(prog3Task, instance.getTaskByCallable(prog3));

		Callable<Integer> prog4 = new SleepingCallable("Prog4", 2000);
		Task<Integer> prog4Task = instance.startTask(prog4, "Prog4");
		assertEquals(4, prog4Task.getId());
		assertSame(prog4Task, instance.getTaskById(4));
		assertSame(prog4Task, instance.getTaskByCallable(prog4));

		Callable<Integer> prog5 = new SleepingCallable("Prog5", 2000);
		Task<Integer> prog5Task = instance.startTask(prog5, "Prog5");
		assertEquals(5, prog5Task.getId());
		assertSame(prog5Task, instance.getTaskById(5));
		assertSame(prog5Task, instance.getTaskByCallable(prog5));

		Callable<Integer> prog6 = new SleepingExceptionCallable("Prog6", 2000);
		Task<Integer> prog6Task = instance.startTask(prog6, "Prog6");
		assertEquals(6, prog6Task.getId());
		assertSame(prog6Task, instance.getTaskById(6));
		assertSame(prog6Task, instance.getTaskByCallable(prog6));

		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(6, tasks.size());

		Thread.sleep(500);

		prog2Task.getFuture().cancel(true);
		prog5Task.getFuture().cancel(true);

		Thread.sleep(2000);

		List<String> messages = taskListener.getMessages();
		if(logger.isInfoEnabled()) logger.info("Messages: {}", messages);
		assertTrue(messages.contains(TestTaskListener.CREATED + prog1));
		assertTrue(messages.contains(TestTaskListener.CREATED + prog2));
		assertTrue(messages.contains(TestTaskListener.CREATED + prog3));
		assertTrue(messages.contains(TestTaskListener.CREATED + prog4));
		assertTrue(messages.contains(TestTaskListener.CREATED + prog5));
		assertTrue(messages.contains(TestTaskListener.CREATED + prog6));
		assertTrue(messages.contains(TestTaskListener.FINISHED + prog1));
		assertTrue(messages.contains(TestTaskListener.CANCELED + prog2));
		assertTrue(messages.contains(TestTaskListener.FAILED + prog3 + " java.lang.RuntimeException"));
		assertTrue(messages.contains(TestTaskListener.FINISHED + prog4));
		assertTrue(messages.contains(TestTaskListener.CANCELED + prog5));
		assertTrue(messages.contains(TestTaskListener.FAILED + prog6 + " java.lang.RuntimeException"));

		tasks = instance.getTasks();
		assertEquals(0, tasks.size());
	}

	private static class SleepingCallable
		implements Callable<Integer>
	{
		private final Logger logger = LoggerFactory.getLogger(SleepingCallable.class);
		private final int sleepTime;
		private final String name;

		SleepingCallable(String name, int sleepTime)
		{
			this.name = name;
			this.sleepTime = sleepTime;
		}

		/**
		 * Computes a result, or throws an exception if unable to do so.
		 *
		 * @return computed result
		 * @throws Exception if unable to compute a result
		 */
		@Override
		public Integer call()
			throws Exception
		{
			long time = System.currentTimeMillis();
			if(logger.isInfoEnabled()) logger.info("Sleeping for {}ms.", sleepTime);
			Thread.sleep(sleepTime);
			time = System.currentTimeMillis() - time;
			if(logger.isInfoEnabled()) logger.info("Operation took {}ms.", time);
			return (int) time;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	private static class SleepingProgressingCallable
		extends AbstractProgressingCallable<Integer>
	{
		private final Logger logger = LoggerFactory.getLogger(SleepingProgressingCallable.class);
		private final int sleepTime;
		private final String name;

		SleepingProgressingCallable(String name, int sleepTime)
		{
			this.name = name;
			this.sleepTime = sleepTime;
		}

		/**
		 * Computes a result, or throws an exception if unable to do so.
		 *
		 * @return computed result
		 * @throws Exception if unable to compute a result
		 */
		@Override
		public Integer call()
			throws Exception
		{
			long time = System.currentTimeMillis();
			setNumberOfSteps(10);
			setCurrentStep(0);
			for(int i = 0; i < 10; i++)
			{
				if(logger.isInfoEnabled()) logger.info("Sleeping for {}ms.", sleepTime);
				Thread.sleep(sleepTime);
				setCurrentStep(i);
			}
			setCurrentStep(10);
			time = System.currentTimeMillis() - time;
			if(logger.isInfoEnabled()) logger.info("Operation took {}ms.", time);
			return (int) time;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	private static class SleepingExceptionCallable
		implements Callable<Integer>
	{
		private final Logger logger = LoggerFactory.getLogger(SleepingCallable.class);
		private final int sleepTime;
		private final String name;

		SleepingExceptionCallable(String name, int sleepTime)
		{
			this.name = name;
			this.sleepTime = sleepTime;
		}

		/**
		 * Computes a result, or throws an exception if unable to do so.
		 *
		 * @return computed result
		 * @throws Exception if unable to compute a result
		 */
		@Override
		public Integer call()
			throws Exception
		{
			if(logger.isInfoEnabled()) logger.info("Sleeping for {}ms.", sleepTime);
			Thread.sleep(sleepTime);
			if(logger.isInfoEnabled()) logger.info("Throwing exception.");
			throw new RuntimeException("Foo."); // NOPMD
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	private static class SleepingExceptionProgressingCallable
		extends AbstractProgressingCallable<Integer>
	{
		private final Logger logger = LoggerFactory.getLogger(SleepingProgressingCallable.class);
		private final int sleepTime;
		private final String name;

		SleepingExceptionProgressingCallable(String name, int sleepTime)
		{
			this.name = name;
			this.sleepTime = sleepTime;
		}

		/**
		 * Computes a result, or throws an exception if unable to do so.
		 *
		 * @return computed result
		 * @throws Exception if unable to compute a result
		 */
		@Override
		public Integer call()
			throws Exception
		{
			setNumberOfSteps(10);
			setCurrentStep(0);
			for(int i = 0; i < 5; i++)
			{
				if(logger.isInfoEnabled()) logger.info("Sleeping for {}ms.", sleepTime);
				Thread.sleep(sleepTime);
				setCurrentStep(i);
			}
			if(logger.isInfoEnabled()) logger.info("Throwing exception.");
			throw new RuntimeException("Foo."); // NOPMD
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	private static class TestTaskListener
		implements TaskListener<Integer>
	{
		static final String CREATED = "Created: ";
		static final String FAILED = "Failed: ";
		static final String FINISHED = "Finished: ";
		static final String CANCELED = "Canceled: ";
		static final String PROGRESS = "Progress: ";

		private final Logger logger = LoggerFactory.getLogger(TestTaskListener.class);

		private final List<String> messages;

		TestTaskListener()
		{
			messages = new ArrayList<>();
		}

		@Override
		public void taskCreated(Task<Integer> task)
		{
			if(logger.isInfoEnabled()) logger.info("Task created.");
			messages.add(CREATED + task.getCallable());
		}

		@Override
		public void executionFailed(Task<Integer> task, ExecutionException exception)
		{
			Throwable cause = exception.getCause();
			if(logger.isInfoEnabled()) logger.info("Execution failed.", cause);
			messages.add(FAILED + task.getCallable() + " " + cause.getClass().getName());
		}

		@Override
		public void executionFinished(Task<Integer> task, Integer result)
		{
			if(logger.isInfoEnabled()) logger.info("Execution finished. Result={}", result);
			messages.add(FINISHED + task.getCallable());
		}

		@Override
		public void executionCanceled(Task<Integer> task)
		{
			if(logger.isInfoEnabled()) logger.info("Execution canceled.");
			messages.add(CANCELED + task.getCallable());
		}

		@Override
		public void progressUpdated(Task<Integer> task, int progress)
		{
			if(logger.isInfoEnabled()) logger.info("Progress update: {}", progress);
			messages.add(PROGRESS + task.getCallable() + " " + progress);
		}

		List<String> getMessages()
		{
			return messages;
		}
	}

	private static class DispatchTestTaskListener
		implements TaskListener<Integer>
	{
		private final Logger logger = LoggerFactory.getLogger(DispatchTestTaskListener.class);
		boolean failed = false;
		private final boolean usingEventQueue;

		DispatchTestTaskListener(boolean usingEventQueue)
		{
			this.usingEventQueue = usingEventQueue;
		}

		private void checkThread()
		{
			if(usingEventQueue)
			{
				if(!EventQueue.isDispatchThread())
				{
					if(logger.isWarnEnabled()) logger.warn("Execution in wrong thread! {}", Thread.currentThread());
					failed = true;
				}
			}
			else
			{
				if(EventQueue.isDispatchThread())
				{
					if(logger.isWarnEnabled()) logger.warn("Execution in wrong thread! {}", Thread.currentThread());
					failed = true;
				}
			}
		}

		@Override
		public void taskCreated(Task<Integer> integerTask)
		{
			checkThread();
		}

		@Override
		public void executionFailed(Task<Integer> task, ExecutionException exception)
		{
			checkThread();
		}

		@Override
		public void executionFinished(Task<Integer> task, Integer result)
		{
			checkThread();
		}

		@Override
		public void executionCanceled(Task<Integer> task)
		{
			checkThread();
		}

		@Override
		public void progressUpdated(Task<Integer> task, int progress)
		{
			checkThread();
		}

		boolean isFailed()
		{
			return failed;
		}
	}

}
