package com.redhat.gss.mylyn.rhcp.core;

import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

import com.redhat.gss.strata.model.Attachment;

public class RhcpAttachmentMapper extends TaskAttachmentMapper {
	public static RhcpAttachmentMapper createFrom(TaskRepository repository, Attachment a) {
		RhcpAttachmentMapper mapper = new RhcpAttachmentMapper();

		mapper.setAttachmentId(a.getUuid());
		mapper.setAuthor(repository.createPerson(a.getCreatedBy()));
		//mapper.setComment(comment);
		mapper.setContentType(a.getMimeType());
		mapper.setCreationDate(a.getCreatedDate().getTime());
		mapper.setDeprecated(a.getDeprecated());
		mapper.setDescription(a.getDescription());
		mapper.setFileName(a.getFileName());
		mapper.setLength(a.getLength());
		//mapper.setPatch(patch);
		//mapper.setReplaceExisting(replaceExisting);
		mapper.setUrl(a.getUri());

		//ADD fields
/*
		a.getActive()
		a.getCaseId()
		a.getCaseNumber()
		a.getFileDate
		a.getEtag()
		a.getLastModifiedBy()
		a.getLastModifiedDate()
		a.getPrivate()
		*/
		return mapper;
	}

	public void applyTo(ITaskAttachment taskAttachment) {
		super.applyTo(taskAttachment);

		//ADD fields
	}

	public void applyTo(TaskAttribute taskAttribute) {
		super.applyTo(taskAttribute);
		//ADD fields
	}

	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;

		//check fields
		return true;
	}

	
}
