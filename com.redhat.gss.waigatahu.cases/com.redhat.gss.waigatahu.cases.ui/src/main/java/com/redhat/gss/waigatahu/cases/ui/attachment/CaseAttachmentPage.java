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
