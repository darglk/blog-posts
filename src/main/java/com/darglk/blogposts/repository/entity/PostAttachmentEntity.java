package com.darglk.blogposts.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PostAttachmentEntity {
    private String postId;
    private String url;
}
