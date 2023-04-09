package com.darglk.blogposts.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommentEntity {
    private String id;
    private String postId;
    private String userId;
    private String content;
}
