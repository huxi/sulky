/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2009 Joern Huxhorn
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
package de.huxhorn.sulky.junit;

//import ch.qos.logback.classic.Level;
//import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
//import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.joran.JoranConfigurator;
//import ch.qos.logback.classic.spi.LoggingEvent;
//import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

/**
 */
@RunWith(Parameterized.class)
public class LoggingTestBase
{
	protected boolean verbose = true;
	protected boolean deleteLogFiles = false;
	private Boolean logging;
	private File loggingFile;

	public LoggingTestBase(Boolean logging)
	{
		this.logging = logging;
	}

	@Parameters
	public static Collection<Object[]> configs()
	{
		return Arrays.asList(new Object[][]{
			{null},
			{Boolean.TRUE},
			{Boolean.FALSE},
		});
	}

	@Before
	public void setUpLogging()
		throws IOException
	{
		loggingFile = null;
		if(this.logging != null)
		{
			if(this.logging)
			{
				loggingFile = File.createTempFile("logging", "log");
				enableAllLogging(loggingFile, verbose);
			}
			else
			{
				disableAllLogging(verbose);
			}
		}
		else
		{
			resetLogging(verbose);
		}
	}

	@After
	public void tearDownLogging()
	{
		if(logging != null)
		{
			resetLogging(verbose);
		}
		if(loggingFile != null && deleteLogFiles)
		{
			loggingFile.delete();
		}
	}

	public static void resetLogging(boolean verbose)
	{
		if(verbose)
		{
			System.out.println("### Resetting logging configuration.");
		}
		ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
		if(loggerFactory instanceof LoggerContext)
		{
			LoggerContext loggerContext = (LoggerContext) loggerFactory;
			// reset previous configuration initially loaded from logback.xml
			if(verbose)
			{
				System.out.println("\nAbout to reset logging system.");
			}
			loggerContext.reset();
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			URL configUrl;
			configUrl = LoggingTestBase.class.getResource("/logback-test.xml");
			if(configUrl == null)
			{
				configUrl = LoggingTestBase.class.getResource("/logback.xml");
			}
			try
			{
				configurator.doConfigure(configUrl);
				if(verbose)
				{
					System.out.println("\nPrinting status of logging system:");
					StatusPrinter.print(loggerContext);
				}
			}
			catch(JoranException ex)
			{
				System.err.println("!!! Error configuring logging framework with '" + configUrl + "'!");
				ex.printStackTrace();
				StatusPrinter.print(loggerContext);
			}
		}
	}

	private static void configureLoggingFromString(String loggingConfig, boolean verbose)
	{
		ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
		if(loggerFactory instanceof LoggerContext)
		{
			LoggerContext loggerContext = (LoggerContext) loggerFactory;
			// reset previous configuration initially loaded from logback.xml
			if(verbose)
			{
				System.out.println("\nAbout to reset logging system.");
			}
			loggerContext.reset();

			InputStream is = null;
			try
			{
				byte[] stringBytes = loggingConfig.getBytes("UTF-8");
				is = new ByteArrayInputStream(stringBytes);
			}
			catch(UnsupportedEncodingException e)
			{
				//this won't happen... famous last words :p
			}

			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			try
			{
				configurator.doConfigure(is);
				if(verbose)
				{
					System.out.println("\nPrinting status of logging system:");
					StatusPrinter.print(loggerContext);
				}
			}
			catch(JoranException ex)
			{
				System.err.println("!!! Error configuring logging framework with '" + loggingConfig + "'!");
				ex.printStackTrace();
				StatusPrinter.print(loggerContext);
			}
		}

	}

	/**
	 * This method disables all log events
	 *
	 * @param verbose if info about logging configuration should be written to System.out
	 */
	public static void disableAllLogging(boolean verbose)
	{
		if(verbose)
		{
			System.out.println("### Disabling all logging.");
		}

		/*
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.reset();
		Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.OFF);
		*/

		String configString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<configuration>\n" +
			"    <root>\n" +
			"        <level value=\"OFF\"/>\n" +
			"    </root>\n" +
			"</configuration>";

		configureLoggingFromString(configString, verbose);
	}

	/**
	 * This method enables all log events and writes the logs to the given file. This is done to force
	 * evaluation of the log message and keep them off the console.
	 *
	 * @param file    the file in which log messages will be written.
	 * @param verbose if info about logging configuration should be written to System.out
	 */
	public static void enableAllLogging(File file, boolean verbose)
	{
		if(verbose)
		{
			System.out.println("### Enabling all logging.\n### Logs are written to '" + file.getAbsolutePath() + "'.");
		}

		/*
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		if(verbose)
		{
			StatusPrinter.print(loggerContext);
		}
		loggerContext.reset();
		Logger root = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.ALL);
		PatternLayout patternLayout = new PatternLayout();
		patternLayout.setPattern("%-4relative [%thread] %-5level %logger{35} - %msg%n");
		patternLayout.setContext(loggerContext);
		FileAppender<LoggingEvent> appender = new FileAppender<LoggingEvent>();
		appender.setLayout(patternLayout);
		appender.setFile(file.getAbsolutePath());
		appender.setName("FILE");
		appender.setContext(loggerContext);
		root.addAppender(appender);
		appender.start();

		if(verbose)
		{
			System.out.println("### Status after enabling all logging.\n### Logs are written to '" + file.getAbsolutePath() + "'.");
			StatusPrinter.print(loggerContext);
		}
		*/

		String configString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<configuration>\n" +
			"    <appender name=\"FILE\" class=\"ch.qos.logback.core.FileAppender\">\n" +
			"        <File>" + file.getAbsolutePath() + "</File>\n" +
			"        <Append>true</Append>\n" +
			"        <layout class=\"ch.qos.logback.classic.PatternLayout\">\n" +
			"            <Pattern>%-4relative [%thread] %-5level %logger{35} - %msg%n</Pattern>\n" +
			"        </layout>\n" +
			"    </appender>\n" +
			"    <root>\n" +
			"        <level value=\"ALL\"/>\n" +
			"        <appender-ref ref=\"FILE\"/>\n" +
			"    </root>\n" +
			"</configuration>";

		configureLoggingFromString(configString, verbose);
	}
}
