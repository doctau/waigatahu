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

import org.eclipse.mylyn.tasks.core.data.AbstractTaskSchema;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

import com.redhat.gss.waigatahu.cases.data.CaseAttribute;

public final class CaseTaskSchema extends AbstractTaskSchema {
	private static final CaseTaskSchema instance = new CaseTaskSchema();

	public static Field getField(String taskKey) {
		return instance.getFieldByKey(taskKey);
	}

	public static CaseTaskSchema getInstance() {
		return instance;
	}
	
	public final Field ATTACHMENT_PRIVATE = createField(CaseAttribute.ATTACHMENT_PRIVATE,
			/*org.eclipse.mylyn.tasks.core.data.Messages.DefaultTaskSchema_Private_Label*/ "Private", TaskAttribute.TYPE_BOOLEAN);
}
