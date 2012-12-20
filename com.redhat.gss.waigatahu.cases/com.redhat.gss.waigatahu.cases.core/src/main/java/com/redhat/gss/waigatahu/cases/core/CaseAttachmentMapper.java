package com.redhat.gss.waigatahu.cases.core;

import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttachmentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import com.redhat.gss.strata.model.Attachment;
import com.redhat.gss.waigatahu.cases.data.CaseAttribute;

public class CaseAttachmentMapper extends TaskAttachmentMapper {
	public static CaseAttachmentMapper createFrom(TaskRepository repository, Attachment a) {
		CaseAttachmentMapper mapper = new CaseAttachmentMapper();

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
		mapper.setPrivate(a.getPrivate());
/*
		a.getActive()
		a.getCaseId()
		a.getCaseNumber()
		a.getFileDate
		a.getEtag()
		a.getLastModifiedBy()
		a.getLastModifiedDate()
		*/
		return mapper;
	}

	public void applyTo(ITaskAttachment taskAttachment) {
		super.applyTo(taskAttachment);

		/*if (isPrivate() != null) {
			taskAttachment.setPrivate(isPrivate());
		}*/
		
		//FIXME: add more
	}

	public void applyTo(TaskAttribute taskAttribute) {
		super.applyTo(taskAttribute);
		TaskData taskData = taskAttribute.getTaskData();
		TaskAttributeMapper mapper = taskData.getAttributeMapper();

		if (isDeprecated() != null) {
			TaskAttribute child = CaseTaskSchema.getField(CaseAttribute.ATTACHMENT_PRIVATE).createAttribute(taskAttribute);
			mapper.setBooleanValue(child, isDeprecated());
		}
		
		//FIXME: add more
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof CaseAttachmentMapper)) {
			return false;
		}
		if (!super.equals(obj))
			return false;

		CaseAttachmentMapper other = (CaseAttachmentMapper) obj;
		if ((other.private_ != null && this.private_ != null) && !(other.private_ == this.private_)) {
			return false;
		}

		//FIXME: check fields
		return true;
	}



	private Boolean private_;

	public Boolean isPrivate() {
		return private_;
	}
	public void setPrivate(Boolean private_) {
		this.private_ = private_;
	}
}
