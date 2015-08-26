/*
 * Copyright 2012 Steve Chaloner
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
package models.security;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.lang.builder.ToStringBuilder;

import play.db.ebean.Model;
import be.objectify.deadbolt.core.models.Role;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class SecurityRole extends Model implements Role {

	public static final String EVENT_ADMIN = "eventAdmin";
	public static final String EVENT_ASSIST = "eventAssistant";
	public static final Finder<Long, SecurityRole> find = new Finder<Long, SecurityRole>(
			Long.class, SecurityRole.class);
	public static final String PFP_ADMIN = "pfpAdmin";
	public static final String ROOT_ADMIN = "rootAdmin";
	public static final String SYS_ADMIN = "sysAdmin";

	public static final String USER = "user";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	public String roleName;

	@Override
	public String getName() {
		return roleName;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public static SecurityRole findByRoleName(String roleName) {
		return find.where().eq("roleName", roleName).findUnique();
	}

	public static List<String> getSecurityRoleValues() {
		return Arrays.asList(SecurityRole.ROOT_ADMIN, SecurityRole.SYS_ADMIN,
				SecurityRole.EVENT_ADMIN, SecurityRole.EVENT_ASSIST,
				SecurityRole.PFP_ADMIN, SecurityRole.USER);

	}

	public static Map<String, String> options() {
		@SuppressWarnings("unchecked")
		final List<SecurityRole> securityRoles = find.findList();
		final LinkedHashMap<String, String> options = new LinkedHashMap<String, String>();
		for (final SecurityRole securityRole : securityRoles) {
			options.put(securityRole.id.toString(), securityRole.roleName);
		}
		return options;
	}
}
