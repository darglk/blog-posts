package com.darglk.blogposts.service;

import com.darglk.blogposts.repository.PostsRepository;
import com.darglk.blogposts.rest.model.PostRequest;
import com.darglk.blogposts.rest.model.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostsServiceImpl implements PostsService {

    private final PostsRepository postsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getPosts() {
        return postsRepository.select().stream()
                .map(p -> new PostResponse(p.getId(), p.getContent()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createPost(String userId, PostRequest request) {
        postsRepository.insert(UUID.randomUUID().toString(), userId, request.getContent());
    }
}
