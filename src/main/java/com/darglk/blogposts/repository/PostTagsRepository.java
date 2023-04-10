package com.darglk.blogposts.repository;

import com.darglk.blogposts.repository.entity.PostTagEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostTagsRepository {
    void insert(PostTagEntity entity);
    void delete(String postId);
}
