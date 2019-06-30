/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2019 Joern Huxhorn
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
 * Copyright 2007-2019 Joern Huxhorn
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

package de.huxhorn.sulky.junit;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PMD.SystemPrintln"})
@RunWith(Parameterized.class)
public class LoggingTestBase
{
	protected boolean verbose = false;
	protected boolean deleteLogFiles = false;
	private final Boolean logging;
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
		if (this.logging != null)
		{
			if (this.logging)
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
		if (logging != null)
		{
			resetLogging(verbose);
		}
		if (loggingFile != null && deleteLogFiles && !loggingFile.delete())
		{
			System.out.println("Couldn't delete file " + loggingFile.getAbsolutePath() + "!");
		}
	}

	@SuppressWarnings({"PMD.AvoidPrintStackTrace"})
	public static void resetLogging(boolean verbose)
	{
		if (verbose)
		{
			System.out.println("### Resetting logging configuration.");
		}
		ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
		if (loggerFactory instanceof LoggerContext)
		{
			LoggerContext loggerContext = (LoggerContext) loggerFactory;
			// reset previous configuration initially loaded from logback.xml
			if (verbose)
			{
				System.out.println("\nAbout to reset logging system.");
			}
			loggerContext.reset();
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			URL configUrl;
			configUrl = LoggingTestBase.class.getResource("/logback-test.xml");
			if (configUrl == null)
			{
				configUrl = LoggingTestBase.class.getResource("/logback.xml");
			}
			try
			{
				configurator.doConfigure(configUrl);
				if (verbose)
				{
					System.out.println("\nPrinting status of logging system:");
					StatusPrinter.print(loggerContext);
				}
			}
			catch (JoranException ex)
			{
				System.err.println("!!! Error configuring logging framework with '" + configUrl + "'!");
				// this is not a bug! - Avoid Print Stack Trace : Avoid printStackTrace(); use a logger call instead.
				ex.printStackTrace();
				StatusPrinter.print(loggerContext);
			}
		}
	}

	@SuppressWarnings({"PMD.AvoidPrintStackTrace"})
	private static void configureLoggingFromString(String loggingConfig, boolean verbose)
	{
		ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
		if (loggerFactory instanceof LoggerContext)
		{
			LoggerContext loggerContext = (LoggerContext) loggerFactory;
			// reset previous configuration initially loaded from logback.xml
			if (verbose)
			{
				System.out.println("\nAbout to reset logging system.");
			}
			loggerContext.reset();

			byte[] stringBytes = loggingConfig.getBytes(StandardCharsets.UTF_8);
			try(InputStream is = new ByteArrayInputStream(stringBytes))
			{
				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(loggerContext);

				configurator.doConfigure(is);
				if (verbose)
				{
					System.out.println("\nPrinting status of logging system:");
					StatusPrinter.print(loggerContext);
				}
			}
			catch (IOException | JoranException ex)
			{
				System.err.println("!!! Error configuring logging framework with '" + loggingConfig + "'!");
				// this is not a bug! - Avoid Print Stack Trace : Avoid printStackTrace(); use a logger call instead.
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
		if (verbose)
		{
			System.out.println("### Disabling all logging.");
		}

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
		if (verbose)
		{
			System.out.println("### Enabling all logging.\n### Logs are written to '" + file.getAbsolutePath() + "'.");
		}

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
