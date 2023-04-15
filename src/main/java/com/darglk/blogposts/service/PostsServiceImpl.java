package com.darglk.blogposts.service;

import com.darglk.blogcommons.exception.ErrorResponse;
import com.darglk.blogcommons.exception.NotFoundException;
import com.darglk.blogcommons.exception.ValidationException;
import com.darglk.blogcommons.model.UserPrincipal;
import com.darglk.blogposts.repository.*;
import com.darglk.blogposts.repository.entity.PostAttachmentEntity;
import com.darglk.blogposts.repository.entity.PostTagEntity;
import com.darglk.blogposts.repository.entity.PostUpvoteEntity;
import com.darglk.blogposts.rest.model.PostRequest;
import com.darglk.blogposts.rest.model.PostResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostsServiceImpl implements PostsService {

    private final FileService fileService;
    private final PostsRepository postsRepository;
    private final PostAttachmentsRepository postAttachmentsRepository;
    private final PostTagsRepository postTagsRepository;
    private final TagsRepository tagsRepository;
    private final PostUpvotesRepository postUpvotesRepository;
    private final CommentsRepository commentsRepository;
    private final CommentAttachmentsRepository commentAttachmentsRepository;
    private final PostsFavoritesRepository postsFavoritesRepository;

    @Value("${application.files.upload.max}")
    private Integer maxFilesUpload;
    private final List<String> allowedFileExtensions = List.of("image/gif", "image/jpeg", "image/gif", "image/png");

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getPosts() {
        return postsRepository.select().stream()
                .map(p -> new PostResponse(p.getId(), p.getContent()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PostResponse createPost(String userId, PostRequest request, List<MultipartFile> files) {
        if (files.size() > maxFilesUpload) {
            throw new ValidationException(List.of(new ErrorResponse("Too many files", "file")));
        }
        if (files.stream().anyMatch(MultipartFile::isEmpty)) {
            throw new ValidationException(List.of(new ErrorResponse("At least one file is empty", "file")));
        }
        if (files.stream().anyMatch(file -> !allowedFileExtensions.contains(file.getContentType()))) {
            throw new ValidationException(List.of(new ErrorResponse("At least one file has incorrect extension", "file")));
        }
        var postId = UUID.randomUUID().toString();
        // TODO: sanitize content type (allow <b><i><quote><a><code>)
        postsRepository.insert(postId, userId, request.getContent());
        fileService.uploadFiles(files).forEach(fileKey ->
                postAttachmentsRepository.insert(new PostAttachmentEntity(postId, fileKey))
        );
        request.getTags().forEach(tag -> {
            tagsRepository.insert(tag.getTag());
            postTagsRepository.insert(new PostTagEntity(tag.getTag(), postId));
        });
        var postResponse = new PostResponse();
        postResponse.setId(postId);
        postResponse.setContent(request.getContent());
        return postResponse;
    }

    @Transactional
    @Override
    public void deletePost(String postId) {
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        var post = postsRepository.selectById(postId);
        if (post.isEmpty()) {
            throw new NotFoundException("Post with id: " + postId + " was not found");
        }
        var postEntity = post.get();
        if (!postEntity.getUserId().equals(userId)) {
            throw new ValidationException(List.of(new ErrorResponse("Cannot delete not own post", "postId")));
        }
        postAttachmentsRepository.select(postId).forEach(attachment -> {
            fileService.deleteFile(attachment.getUrl());
        });
        commentsRepository.select(postId).forEach(comment -> {
            commentAttachmentsRepository.select(comment.getId()).forEach(attachment -> {
               fileService.deleteFile(attachment.getUrl());
            });
        });
        postsRepository.delete(postId);
    }

    @Transactional
    @Override
    public void upvotePost(String postId) {
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        var post = postsRepository.selectById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        if (post.getUserId().equals(userId)) {
            throw new ValidationException(List.of(new ErrorResponse("You cannot upvote your own post", "postId")));
        }
        if (postUpvotesRepository.exists(postId, userId)) {
            postUpvotesRepository.delete(postId, userId);
        } else {
            postUpvotesRepository.insert(new PostUpvoteEntity(postId, userId));
        }
    }

    @Override
    @Transactional
    public void toggleFavorite(String postId) {
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        postsRepository.selectById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        if (postsFavoritesRepository.exists(postId, userId)) {
            postsFavoritesRepository.delete(postId, userId);
        } else {
            postsFavoritesRepository.insert(postId, userId);
        }
    }

    @Override
    @Transactional
    public void addAttachment(String postId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException(List.of(new ErrorResponse("File cannot be null or empty", "file")));
        }
        if (!allowedFileExtensions.contains(file.getContentType())) {
            throw new ValidationException(List.of(new ErrorResponse("Incorrect content type", "file")));
        }
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        var post = postsRepository.selectById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        if (!post.getUserId().equals(userId)) {
            throw new ValidationException(List.of(new ErrorResponse("Cannot modify not own post", "postId")));
        }

        var attachments = postAttachmentsRepository.select(postId);
        if (attachments.size() > maxFilesUpload) {
            throw new ValidationException(List.of(new ErrorResponse("Too many files", "file")));
        }
        var fileUrl = fileService.uploadFile(file);
        postAttachmentsRepository.insert(new PostAttachmentEntity(postId, fileUrl));
    }

    @Transactional
    @Override
    public void removeAttachment(String postId, String attachmentId) {
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        var post = postsRepository.selectById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        if (!post.getUserId().equals(userId)) {
            throw new ValidationException(List.of(new ErrorResponse("Cannot modify not own post", "postId")));
        }
        postAttachmentsRepository.selectByUrl(postId, attachmentId)
                .orElseThrow(() -> new NotFoundException("Not found attachment with id: " + attachmentId));
        fileService.deleteFile(attachmentId);
        postAttachmentsRepository.delete(postId, attachmentId);
    }

    @Transactional
    @Override
    public PostResponse updatePost(String postId, PostRequest postRequest) {
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        var post = postsRepository.selectById(postId)
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + postId));
        if (!post.getUserId().equals(userId)) {
            throw new ValidationException(List.of(new ErrorResponse("Cannot modify not own post", "postId")));
        }
        postsRepository.update(postId, userId, postRequest.getContent());
        postTagsRepository.delete(postId);
        postRequest.getTags().forEach(tag -> {
            tagsRepository.insert(tag.getTag());
            postTagsRepository.insert(new PostTagEntity(tag.getTag(), postId));
        });
        return new PostResponse(postId, postRequest.getContent());
    }
}
