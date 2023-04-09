package com.darglk.blogposts.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommentAttachmentEntity {
    private String commentId;
    private String url;
}
