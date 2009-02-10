/**
 * This package contains the sulky Task API, a wrapper for Executor, Callable and Future,
 * that also supports handling on the event dispatch thread. It's an alternative to
 * the SwingWorker that has been added to Java in the 1.6 release.
 *
 * <p>In contrast to SwingWorker, it contains the definition of a generified
 * TaskManager, Task and TaskListener.</p>
 *
 * <p>The TaskManager is used to keep track of all running (or scheduled) Tasks and sends
 * events to registered TaskListeners, optionally using the event dispatch thread for event
 * delivery.</p>
 *
 * <p>A Task contains, in addition to a Callable and the related Future, additional information
 * like a TaskManager-related unique Task ID, a name, a description and a map of Strings that can
 * be used for arbitrary additional meta data.</p>  
 * @see java.util.concurrent
 * @see javax.swing.SwingWorker
 */
package de.huxhorn.sulky.tasks;
