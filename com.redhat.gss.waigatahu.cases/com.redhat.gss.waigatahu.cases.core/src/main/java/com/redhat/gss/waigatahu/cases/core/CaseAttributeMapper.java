/*******************************************************************************
* Copyright (c) 2012 Red Hat, Inc.
* Distributed under license by Red Hat, Inc. All rights reserved.
* This program is made available under the terms of the
* Eclipse Public License v1.0 which accompanies this distribution,
* and is available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* Red Hat, Inc. - initial API and implementation
******************************************************************************/

package com.redhat.gss.waigatahu.cases.core;

import java.util.List;

import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;

public class CaseAttributeMapper extends TaskAttributeMapper {
	public CaseAttributeMapper(TaskRepository taskRepository) {
		super(taskRepository);
	}

	public void setNullableStringValue(TaskAttribute attribute, String value) {
		setValue(attribute, (value == null) ? "" : value);
	}

	public String getNullableStringValue(TaskAttribute attribute) {
		String s = getValue(attribute);
		return (s == null || s.isEmpty()) ? null : s;
	}


	public void addRepositoryPerson(TaskAttribute taskAttribute, IRepositoryPerson person) {
		List<String> values1 = getValues(taskAttribute);
		values1.add(person.getPersonId());
		setValues(taskAttribute, values1);
		if (person.getName() != null) {
			TaskAttribute child = taskAttribute.createAttribute(TaskAttribute.PERSON_NAME);
			List<String> values2 = getValues(child);
			values2.add(person.getPersonId());
			setValues(child, values2);
		}
	}
}
