package com.darglk.blogposts.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommentResponse {
    private String id;
    private String content;
    private String postId;
    private String userId;
}
