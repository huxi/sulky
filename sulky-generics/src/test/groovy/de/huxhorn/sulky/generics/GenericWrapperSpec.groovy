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

package de.huxhorn.sulky.generics;

import spock.lang.Specification;

class GenericWrapperSpec extends Specification {

    def "check toString"() {
        expect:
        new GenericWrapper<Integer>(1).toString() == 'wrapper-Integer[wrapped=1]'
        new GenericWrapper<Integer>(null).toString() == 'wrapper-null[wrapped=null]'
    }

    def "check equals"() {
        when:
        def nullWrapper = new GenericWrapper<Number>(null)
        def oneIntWrapper = new GenericWrapper<Number>(1)
        def oneLongWrapper = new GenericWrapper<Number>(1L)

        then:
        !oneIntWrapper.equals(null)
        !oneIntWrapper.equals(1)
        oneIntWrapper.equals(oneIntWrapper)
        oneIntWrapper.equals(new GenericWrapper<Integer>(1))
        !oneIntWrapper.equals(oneLongWrapper)
        !oneLongWrapper.equals(oneIntWrapper)
        !oneIntWrapper.equals(nullWrapper)
        !nullWrapper.equals(oneIntWrapper)
        nullWrapper.equals(new GenericWrapper<Number>(null))
    }

    def "check hashCode"() {
        expect:
        new GenericWrapper<>(null).hashCode() == 0
        new GenericWrapper<>(1).hashCode() == 1.hashCode()
        new GenericWrapper<>(1L).hashCode() == 1L.hashCode()
    }

    def "wrapping null"() {
        when:
        GenericWrapper<Number> wrapper = new GenericWrapper<>(null)

        then:
        !wrapper.isWrapperFor(Integer.class)
        !wrapper.isWrapperFor(Number.class)
        !wrapper.isWrapperFor(String.class)
    }

    def "unwrapping null"() {
        when:
        GenericWrapper<Number> wrapper = new GenericWrapper<>(null)
        wrapper.unwrap(Integer.class)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'This Wrapper wraps null!'

    }

    def "wrapping an Integer"() {
        when:
        GenericWrapper<Number> wrapper = new GenericWrapper<>(1)

        then:
        wrapper.isWrapperFor(Integer.class)
        wrapper.isWrapperFor(Number.class)
        !wrapper.isWrapperFor(String.class)
        wrapper.wrapped == 1
    }

    def "wrapping an Integer Wrapper"() {
        when:
        GenericWrapper<Wrapper> wrapper = new GenericWrapper<>(new GenericWrapper(1))

        then:
        wrapper.isWrapperFor(Integer.class)
        wrapper.isWrapperFor(Number.class)
        !wrapper.isWrapperFor(String.class)
        wrapper.wrapped != 1
    }

    def "unwrapping an Integer as Integer"() {
        when:
        GenericWrapper<Number> wrapper = new GenericWrapper<>(1)
        Integer unwrapped = wrapper.unwrap(Integer.class)

        then:
        unwrapped == 1
    }

    def "unwrapping an Integer as Number"() {
        when:
        GenericWrapper<Number> wrapper = new GenericWrapper<>(1)
        Number unwrapped = wrapper.unwrap(Number.class)

        then:
        unwrapped == 1
    }

    def "unwrapping an Integer as String"() {
        when:
        GenericWrapper<Number> wrapper = new GenericWrapper<>(1)
        wrapper.unwrap(String.class)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'This Wrapper does not wrap an instance of the given interface!'
    }

    def "unwrapping an Integer-Wrapper as Number"() {
        when:
        GenericWrapper<Wrapper> wrapper = new GenericWrapper<>(new GenericWrapper(1))
        Number unwrapped = wrapper.unwrap(Number.class)

        then:
        unwrapped == 1
    }

    def "unwrapping an Integer-Wrapper as String"() {
        when:
        GenericWrapper<Wrapper> wrapper = new GenericWrapper<>(new GenericWrapper(1))
        wrapper.unwrap(String.class)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'This Wrapper does not wrap an instance of the given interface!'
    }
}
