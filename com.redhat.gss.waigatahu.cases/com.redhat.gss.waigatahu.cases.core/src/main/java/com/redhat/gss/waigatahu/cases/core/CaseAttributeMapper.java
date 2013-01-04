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
