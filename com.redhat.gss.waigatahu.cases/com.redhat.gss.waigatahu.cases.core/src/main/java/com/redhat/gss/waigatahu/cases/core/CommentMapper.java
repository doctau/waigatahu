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

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;

import com.redhat.gss.strata.model.Comment;

public class CommentMapper extends TaskCommentMapper {
	public static CommentMapper createFrom(TaskRepository repository, Comment c) {
		CommentMapper mapper = new CommentMapper();
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
