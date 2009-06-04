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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

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
public class LoggingTestBaseExampleTest
	extends LoggingTestBase
{
	private final Logger logger = LoggerFactory.getLogger(LoggingTestBaseExampleTest.class);

	public LoggingTestBaseExampleTest(Boolean logging)
	{
		super(logging);
	}

	@Test
	public void someTest()
	{
		if(logger.isDebugEnabled()) logger.debug("Shouldn't show up in default mode.");
		if(logger.isInfoEnabled()) logger.info("Just some logging message to see if everything works.");

	}

	@Test
	public void someOtherTest()
	{
		if(logger.isDebugEnabled()) logger.debug("Other - Shouldn't show up in default mode.");
		if(logger.isInfoEnabled()) logger.info("Other - Just some logging message to see if everything works.");
	}
}
