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

package com.redhat.gss.waigatahu.cases.ui.editor;

import org.eclipse.mylyn.internal.tasks.core.ITasksCoreConstants;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.IFormPage;

import com.redhat.gss.waigatahu.cases.core.CaseRepositoryConnector;
import com.redhat.gss.waigatahu.cases.core.WaigatahuCaseCorePlugin;

public class CaseEditorPageFactory extends AbstractTaskEditorPageFactory {
	private static final String PAGE_TEXT = "PAGE TEXT";


	public boolean canCreatePageFor(TaskEditorInput input) {
		if (input.getTask().getConnectorKind().equals(WaigatahuCaseCorePlugin.CONNECTOR_KIND)) {
			return true;
		} else if (input.getTask().getAttribute(ITasksCoreConstants.ATTRIBUTE_OUTGOING_NEW_CONNECTOR_KIND).equals(WaigatahuCaseCorePlugin.CONNECTOR_KIND)) {
			return true;
		} else {
			return false;
		}
	}

	public Image getPageImage() {
		//TODO: add
		return null;
	}

	public String getPageText() {
		return PAGE_TEXT;
	}

	public int getPriority() {
		return PRIORITY_TASK;
	}

	public IFormPage createPage(TaskEditor parentEditor) {
		return new CaseEditorPage(parentEditor, WaigatahuCaseCorePlugin.CONNECTOR_KIND, getConnector());
	}


	private CaseRepositoryConnector getConnector() {
		return WaigatahuCaseCorePlugin.getConnector();
	}
}
