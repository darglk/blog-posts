package com.darglk.blogposts.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PostUpvoteEntity {
    private String postId;
    private String userId;
}
