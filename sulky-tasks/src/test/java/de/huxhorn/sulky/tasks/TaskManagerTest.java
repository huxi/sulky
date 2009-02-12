package de.huxhorn.sulky.tasks;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TaskManagerTest
{
	private final Logger logger = LoggerFactory.getLogger(TaskManagerTest.class);

	private TaskManager<Integer> instance;
	private String taskName;

	@Before
	public void setUp()
	{
		Toolkit tk = Toolkit.getDefaultToolkit();
		if(logger.isDebugEnabled()) logger.debug("Toolkit: {}", tk);
		instance = new TaskManager<Integer>();
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
		instance.startTask(new Callable<Integer>()
		{
			public Integer call()
				throws Exception
			{
				return null;
			}
		}, "Won't work");
	}

	@Test(expected = IllegalArgumentException.class)
	public void missingName()
	{
		instance.startUp();
		instance.startTask(new Callable<Integer>()
		{
			public Integer call()
				throws Exception
			{
				return null;
			}
		}, null);
	}

	@Test
	public void withDescription()
	{
		instance.startUp();
		String name = "TaskName";
		String description = "Task description";
		Task<Integer> task = instance.startTask(new Callable<Integer>()
		{
			public Integer call()
				throws Exception
			{
				return null;
			}
		}, name, description);
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
		Map<String, String> metaData = new HashMap<String, String>();
		metaData.put("foo", "bar");
		Task<Integer> task = instance.startTask(new Callable<Integer>()
		{
			public Integer call()
				throws Exception
			{
				return null;
			}
		}, name, description, metaData);
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
		assertEquals(false, future.isCancelled());
		assertEquals(false, future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(2500);
		assertEquals(false, future.isCancelled());
		assertEquals(true, future.isDone());
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
		assertEquals(false, future.isCancelled());
		assertEquals(false, future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(2500);
		assertEquals(false, future.isCancelled());
		assertEquals(true, future.isDone());
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
		Callable<Integer> callable = new SleepingProgressingCallable("C1", 200);
		Task<Integer> task = instance.startTask(callable, taskName);
		long taskId = task.getId();
		assertEquals(1, taskId);
		assertSame(task, instance.getTaskById(taskId));
		assertSame(task, instance.getTaskByCallable(callable));
		Future<Integer> future = task.getFuture();
		assertEquals(false, future.isCancelled());
		assertEquals(false, future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(2500);
		assertEquals(false, future.isCancelled());
		assertEquals(true, future.isDone());
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
		assertEquals(false, future.isCancelled());
		assertEquals(false, future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(2500);
		assertEquals(false, future.isCancelled());
		assertEquals(true, future.isDone());
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
		assertEquals(false, future.isCancelled());
		assertEquals(false, future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(2500);
		assertEquals(false, future.isCancelled());
		assertEquals(true, future.isDone());

		List<String> messages = taskListener.getMessages();
		if(logger.isInfoEnabled()) logger.info("Messages: {}", messages);
		List<String> expectedMsgs = new ArrayList<String>();
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
		assertEquals(false, future.isCancelled());
		assertEquals(false, future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(2500);
		assertEquals(false, future.isCancelled());
		assertEquals(true, future.isDone());

		List<String> messages = taskListener.getMessages();
		if(logger.isInfoEnabled()) logger.info("Messages: {}", messages);
		List<String> expectedMsgs = new ArrayList<String>();
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
		assertEquals(false, future.isCancelled());
		assertEquals(false, future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(2500);
		assertEquals(false, future.isCancelled());
		assertEquals(true, future.isDone());

		List<String> messages = taskListener.getMessages();
		if(logger.isInfoEnabled()) logger.info("Messages: {}", messages);
		List<String> expectedMsgs = new ArrayList<String>();
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
		assertEquals(false, future.isCancelled());
		assertEquals(false, future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(2500);
		assertEquals(false, future.isCancelled());
		assertEquals(true, future.isDone());

		List<String> messages = taskListener.getMessages();
		if(logger.isInfoEnabled()) logger.info("Messages: {}", messages);
		List<String> expectedMsgs = new ArrayList<String>();
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
		assertEquals(false, future.isCancelled());
		assertEquals(false, future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		future.cancel(true);
		Thread.sleep(2500);
		assertEquals(true, future.isCancelled());
		assertEquals(true, future.isDone());
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
		assertEquals(false, future.isCancelled());
		assertEquals(false, future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		future.cancel(true);
		Thread.sleep(2500);
		assertEquals(true, future.isCancelled());
		assertEquals(true, future.isDone());
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
		assertEquals(false, future.isCancelled());
		assertEquals(false, future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		future.cancel(true);
		Thread.sleep(2500);
		assertEquals(true, future.isCancelled());
		assertEquals(true, future.isDone());

		List<String> messages = taskListener.getMessages();
		if(logger.isInfoEnabled()) logger.info("Messages: {}", messages);
		List<String> expectedMsgs = new ArrayList<String>();
		expectedMsgs.add(TestTaskListener.CREATED + callable);
		expectedMsgs.add(TestTaskListener.CANCELED + callable);
		assertEquals(expectedMsgs, messages);
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
		assertEquals(false, future.isCancelled());
		assertEquals(false, future.isDone());
		Map<Long, Task<Integer>> tasks = instance.getTasks();
		assertEquals(1, tasks.size());
		assertTrue(tasks.containsValue(task));
		Thread.sleep(500);
		future.cancel(true);
		Thread.sleep(2000);
		assertEquals(true, future.isCancelled());
		assertEquals(true, future.isDone());

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
		final Logger logger = LoggerFactory.getLogger(SleepingCallable.class);
		private int sleepTime;
		private String name;

		public SleepingCallable(String name, int sleepTime)
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
		final Logger logger = LoggerFactory.getLogger(SleepingProgressingCallable.class);
		private int sleepTime;
		private String name;

		public SleepingProgressingCallable(String name, int sleepTime)
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
		final Logger logger = LoggerFactory.getLogger(SleepingCallable.class);
		private int sleepTime;
		private String name;

		public SleepingExceptionCallable(String name, int sleepTime)
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
		public Integer call()
			throws Exception
		{
			if(logger.isInfoEnabled()) logger.info("Sleeping for {}ms.", sleepTime);
			Thread.sleep(sleepTime);
			if(logger.isInfoEnabled()) logger.info("Throwing exception.");
			throw new RuntimeException("Foo.");
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
		final Logger logger = LoggerFactory.getLogger(SleepingProgressingCallable.class);
		private int sleepTime;
		private String name;

		public SleepingExceptionProgressingCallable(String name, int sleepTime)
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
			throw new RuntimeException("Foo.");
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
		public static final String CREATED = "Created: ";
		public static final String FAILED = "Failed: ";
		public static final String FINISHED = "Finished: ";
		public static final String CANCELED = "Canceled: ";
		public static final String PROGRESS = "Progress: ";

		final Logger logger = LoggerFactory.getLogger(TestTaskListener.class);

		List<String> messages;

		public TestTaskListener()
		{
			messages = new ArrayList<String>();
		}

		public void taskCreated(Task<Integer> task)
		{
			if(logger.isInfoEnabled()) logger.info("Task created.");
			messages.add(CREATED + task.getCallable());
		}

		public void executionFailed(Task<Integer> task, ExecutionException exception)
		{
			Throwable cause = exception.getCause();
			if(logger.isInfoEnabled()) logger.info("Execution failed.", cause);
			messages.add(FAILED + task.getCallable() + " " + cause.getClass().getName());
		}

		public void executionFinished(Task<Integer> task, Integer result)
		{
			if(logger.isInfoEnabled()) logger.info("Execution finished. Result={}", result);
			messages.add(FINISHED + task.getCallable());
		}

		public void executionCanceled(Task<Integer> task)
		{
			if(logger.isInfoEnabled()) logger.info("Execution canceled.");
			messages.add(CANCELED + task.getCallable());
		}

		public void progressUpdated(Task<Integer> task, int progress)
		{
			if(logger.isInfoEnabled()) logger.info("Progress update: {}", progress);
			messages.add(PROGRESS + task.getCallable() + " " + progress);
		}

		public List<String> getMessages()
		{
			return messages;
		}
	}

	private static class DispatchTestTaskListener
		implements TaskListener<Integer>
	{
		private final Logger logger = LoggerFactory.getLogger(DispatchTestTaskListener.class);
		boolean failed = false;
		private boolean usingEventQueue;

		public DispatchTestTaskListener(boolean usingEventQueue)
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

		public void taskCreated(Task<Integer> integerTask)
		{
			checkThread();
		}

		public void executionFailed(Task<Integer> task, ExecutionException exception)
		{
			checkThread();
		}

		public void executionFinished(Task<Integer> task, Integer result)
		{
			checkThread();
		}

		public void executionCanceled(Task<Integer> task)
		{
			checkThread();
		}

		public void progressUpdated(Task<Integer> task, int progress)
		{
			checkThread();
		}

		public boolean isFailed()
		{
			return failed;
		}
	}

}
