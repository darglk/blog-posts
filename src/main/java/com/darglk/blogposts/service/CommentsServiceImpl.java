package com.darglk.blogposts.service;

import com.darglk.blogcommons.exception.ErrorResponse;
import com.darglk.blogcommons.exception.NotFoundException;
import com.darglk.blogcommons.exception.ValidationException;
import com.darglk.blogcommons.model.UserPrincipal;
import com.darglk.blogposts.repository.*;
import com.darglk.blogposts.repository.entity.CommentAttachmentEntity;
import com.darglk.blogposts.repository.entity.CommentEntity;
import com.darglk.blogposts.repository.entity.CommentUpvoteEntity;
import com.darglk.blogposts.rest.model.CommentRequest;
import com.darglk.blogposts.rest.model.CommentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentsServiceImpl implements CommentsService {

    private final CommentsRepository commentsRepository;
    private final CommentAttachmentsRepository commentAttachmentsRepository;
    private final CommentUpvotesRepository commentUpvotesRepository;
    private final PostsRepository postsRepository;
    private final CommentsFavoritesRepository commentsFavoritesRepository;
    private final FileService fileService;
    private final List<String> allowedFileExtensions = List.of("image/gif", "image/jpeg", "image/gif", "image/png");
    @Value("${application.files.upload.max}")
    private Integer maxFilesUpload;

    @Override
    @Transactional
    public CommentResponse createComment(String userId, String postId, CommentRequest request, List<MultipartFile> files) {
        if (files.size() > maxFilesUpload) {
            throw new ValidationException(List.of(new ErrorResponse("Too many files", "file")));
        }
        if (files.stream().anyMatch(MultipartFile::isEmpty)) {
            throw new ValidationException(List.of(new ErrorResponse("Files cannot be empty", "file")));
        }
        if (files.stream().anyMatch(file -> !allowedFileExtensions.contains(file.getContentType()))) {
            throw new ValidationException(List.of(new ErrorResponse("Incorrect content type", "file")));
        }
        var commentId = UUID.randomUUID().toString();
        // TODO: sanitize content type (allow <b><i><quote><a><code>)
        commentsRepository.insert(new CommentEntity(commentId, postId, userId, request.getContent()));
        fileService.uploadFiles(files).forEach(fileKey ->
                commentAttachmentsRepository.insert(new CommentAttachmentEntity(commentId, fileKey))
        );
        var commentResponse = new CommentResponse();
        commentResponse.setId(commentId);
        commentResponse.setContent(request.getContent());
        commentResponse.setPostId(postId);
        commentResponse.setUserId(userId);
        return commentResponse;
    }

    @Override
    @Transactional
    public void upvoteComment(String commentId) {
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        var comment = commentsRepository.selectById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found comment with id: " + commentId));
        if (comment.getUserId().equals(userId)) {
            throw new ValidationException(List.of(new ErrorResponse("You cannot upvote your own comment", "commentId")));
        }
        if (commentUpvotesRepository.exists(commentId, userId)) {
            commentUpvotesRepository.delete(commentId, userId);
        } else {
            commentUpvotesRepository.insert(new CommentUpvoteEntity(commentId, userId));
        }
    }

    @Transactional
    @Override
    public void deleteComment(String commentId) {
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        var comment = commentsRepository.selectById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found comment with id: " + commentId));
        var post = postsRepository.selectById(comment.getPostId())
                .orElseThrow(() -> new NotFoundException("Not found post with id: " + comment.getPostId()));

        if (!comment.getUserId().equals(userId) || !post.getUserId().equals(userId)) {
            throw new ValidationException(List.of(new ErrorResponse("Cannot delete comment", "commentId")));
        }

        commentAttachmentsRepository.select(commentId).forEach(attachment -> {
            fileService.deleteFile(attachment.getUrl());
        });
        commentsRepository.delete(commentId);
    }

    @Override
    @Transactional
    public void toggleFavorite(String commentId) {
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        commentsRepository.selectById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found comment with id: " + commentId));
        if (commentsFavoritesRepository.exists(commentId, userId)) {
            commentsFavoritesRepository.delete(commentId, userId);
        } else {
            commentsFavoritesRepository.insert(commentId, userId);
        }
    }

    @Transactional
    @Override
    public void addAttachment(String commentId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException(List.of(new ErrorResponse("File cannot be null or empty", "file")));
        }
        if (!allowedFileExtensions.contains(file.getContentType())) {
            throw new ValidationException(List.of(new ErrorResponse("Incorrect content type", "file")));
        }
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        var comment = commentsRepository.selectById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found comment with id: " + commentId));
        if (!comment.getUserId().equals(userId)) {
            throw new ValidationException(List.of(new ErrorResponse("Cannot modify not own comment", "commentId")));
        }

        var attachments = commentAttachmentsRepository.select(commentId);
        if (attachments.size() > maxFilesUpload) {
            throw new ValidationException(List.of(new ErrorResponse("Too many files", "file")));
        }
        var fileUrl = fileService.uploadFile(file);
        commentAttachmentsRepository.insert(new CommentAttachmentEntity(commentId, fileUrl));
    }

    @Transactional
    @Override
    public void removeAttachment(String commentId, String attachmentId) {
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        var comment = postsRepository.selectById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found comment with id: " + commentId));
        if (!comment.getUserId().equals(userId)) {
            throw new ValidationException(List.of(new ErrorResponse("Cannot modify not own comment", "commentId")));
        }
        commentAttachmentsRepository.selectByUrl(commentId, attachmentId)
                .orElseThrow(() -> new NotFoundException("Not found attachment with id: " + attachmentId));
        fileService.deleteFile(attachmentId);
        commentAttachmentsRepository.delete(commentId, attachmentId);
    }

    @Transactional
    @Override
    public CommentResponse updateComment(String commentId, CommentRequest commentRequest) {
        var userId = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        var post = postsRepository.selectById(commentId)
                .orElseThrow(() -> new NotFoundException("Not found comment with id: " + commentId));
        if (!post.getUserId().equals(userId)) {
            throw new ValidationException(List.of(new ErrorResponse("Cannot modify not own comment", "commentId")));
        }
        commentsRepository.update(commentId, userId, commentRequest.getContent());
        return new CommentResponse(commentId, commentRequest.getContent(), post.getId(), userId);
    }
}
