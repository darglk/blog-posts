package com.darglk.blogposts.repository;

import com.darglk.blogposts.repository.entity.PostAttachmentEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PostAttachmentsRepository {
    List<PostAttachmentEntity> select(String postId);
    void insert(PostAttachmentEntity entity);
}
