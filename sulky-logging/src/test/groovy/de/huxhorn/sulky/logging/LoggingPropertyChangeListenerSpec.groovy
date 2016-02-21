/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2014 Joern Huxhorn
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
 * Copyright 2007-2014 Joern Huxhorn
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

package de.huxhorn.sulky.logging

import ch.qos.logback.core.Appender
import org.slf4j.LoggerFactory
import spock.lang.Specification

import java.beans.PropertyChangeEvent;

public class LoggingPropertyChangeListenerSpec extends Specification {
    def "log a change with default logger"() {
        setup:
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

        Appender appenderMock = Mock(Appender)
        root.addAppender(appenderMock);

        when:
        LoggingPropertyChangeListener instance = new LoggingPropertyChangeListener()
        def event = new PropertyChangeEvent(new Object(), 'valueName', 'oldValue', 'newValue')
        instance.propertyChange(event)

        then:
        1 * appenderMock.doAppend({
            assert it.formattedMessage == 'PropertyChangeEvent:\n\tpropertyName=\'valueName\'\n\toldValue=oldValue\n\tnewValue=newValue'
            assert it.loggerName == 'de.huxhorn.sulky.logging.LoggingPropertyChangeListener'
            true
        } )

    }

    def "log a change with custom logger"() {
        setup:
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

        Appender appenderMock = Mock(Appender)
        root.addAppender(appenderMock);

        when:
        LoggingPropertyChangeListener instance = new LoggingPropertyChangeListener(LoggerFactory.getLogger("foo"))
        def event = new PropertyChangeEvent(new Object(), 'valueName', 'oldValue', 'newValue')
        instance.propertyChange(event)

        then:
        1 * appenderMock.doAppend({
            assert it.formattedMessage == 'PropertyChangeEvent:\n\tpropertyName=\'valueName\'\n\toldValue=oldValue\n\tnewValue=newValue'
            assert it.loggerName == 'foo'
            true
        } )

    }

    def "null logger in constructor"() {
        when:
        new LoggingPropertyChangeListener(null);

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'logger must not be null!'
    }
}
