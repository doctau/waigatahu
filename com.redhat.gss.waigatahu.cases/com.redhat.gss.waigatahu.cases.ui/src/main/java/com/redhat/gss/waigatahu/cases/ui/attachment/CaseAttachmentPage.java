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

package com.redhat.gss.waigatahu.cases.ui.attachment;

import org.eclipse.mylyn.tasks.core.data.TaskAttachmentModel;
import org.eclipse.mylyn.tasks.ui.wizards.TaskAttachmentPage;

/*
 * Last page of the new attachment wizard.
 */
public class CaseAttachmentPage extends TaskAttachmentPage {
	public CaseAttachmentPage(TaskAttachmentModel model) {
		super(model);
		
		setNeedsReplaceExisting(false);
		
		//FIXME: don't need "patch" button
	}
}
