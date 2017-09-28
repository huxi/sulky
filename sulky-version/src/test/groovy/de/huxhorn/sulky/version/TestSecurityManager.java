/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2017 Joern Huxhorn
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
 * Copyright 2007-2017 Joern Huxhorn
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

package de.huxhorn.sulky.version;

import java.security.AccessControlException;
import java.security.Permission;
import java.util.PropertyPermission;
import java.util.Set;

public class TestSecurityManager extends SecurityManager
{
	private static final java.lang.String PROPERTY_RW_ACTION = "read,write";
	private static final java.lang.String PROPERTY_READ_ACTION = "read";
	private static final java.lang.String PROPERTY_WRITE_ACTION = "write";

	private Set<String> deniedProperties;
	private Set<String> unreadableProperties;
	private Set<String> unwritableProperties;

	public Set<String> getDeniedProperties() {
		return deniedProperties;
	}

	public void setDeniedProperties(Set<String> deniedProperties) {
		this.deniedProperties = deniedProperties;
	}

	public Set<String> getUnreadableProperties() {
		return unreadableProperties;
	}

	public void setUnreadableProperties(Set<String> unreadableProperties) {
		this.unreadableProperties = unreadableProperties;
	}

	public Set<String> getUnwritableProperties() {
		return unwritableProperties;
	}

	public void setUnwritableProperties(Set<String> unwritableProperties) {
		this.unwritableProperties = unwritableProperties;
	}

	@Override
	public void checkPermission(Permission perm) {
		if(perm instanceof PropertyPermission)
		{
			PropertyPermission p = (PropertyPermission) perm;
			String permissionName = p.getName();
			if(deniedProperties != null && deniedProperties.contains(permissionName)) {
				throw new AccessControlException("access denied " + perm, perm);
			}

			String actions = p.getActions();

			if(PROPERTY_READ_ACTION.equals(actions) && unreadableProperties != null && unreadableProperties.contains(permissionName))
			{
				throw new AccessControlException("access denied " + perm, perm);
			}

			if(PROPERTY_WRITE_ACTION.equals(actions) && unwritableProperties != null && unwritableProperties.contains(permissionName))
			{
				throw new AccessControlException("access denied " + perm, perm);
			}

			if(PROPERTY_RW_ACTION.equals(actions) && (
					(unreadableProperties != null && unreadableProperties.contains(permissionName)) ||
					(unwritableProperties != null && unwritableProperties.contains(permissionName))
					))
			{
				throw new AccessControlException("access denied " + perm, perm);
			}
		}
	}
}
