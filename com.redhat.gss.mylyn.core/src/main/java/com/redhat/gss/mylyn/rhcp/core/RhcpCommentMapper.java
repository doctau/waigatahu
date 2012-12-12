package com.redhat.gss.mylyn.rhcp.core;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;

import com.redhat.gss.strata.model.Comment;

public class RhcpCommentMapper extends TaskCommentMapper {

	public static RhcpCommentMapper createFrom(TaskRepository repository, Comment c) {
		RhcpCommentMapper mapper = new RhcpCommentMapper();
        // Set properties and text associated with this comment.
        mapper.setAuthor(repository.createPerson(c.getCreatedBy()));
        mapper.setCreationDate(c.getCreatedDate().getTime());
        mapper.setText(c.getText());
        mapper.setCommentId(c.getId());
        mapper.setIsPrivate(!c.getPublic());
        mapper.setUrl(c.getUri());

        //FIXME:
        //c.getDraft()
        //c.getLastModifiedBy()
        //c.getLastModifiedDate()
		return mapper;
	}
}
