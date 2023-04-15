package com.darglk.blogposts.rest;

import com.darglk.blogposts.BlogPostsApplication;
import com.darglk.blogposts.repository.*;
import com.darglk.blogposts.repository.entity.*;
import com.darglk.blogposts.rest.model.CommentRequest;
import com.darglk.blogposts.rest.model.PostRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        classes = { BlogPostsApplication.class, TestConfiguration.class })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class CommentsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private PostsRepository postsRepository;
    @Autowired
    private TagsRepository tagsRepository;
    @Autowired
    private PostTagsRepository postTagsRepository;
    @Autowired
    private PostAttachmentsRepository postAttachmentsRepository;
    @Autowired
    private PostUpvotesRepository postUpvotesRepository;
    @Autowired
    private PostsFavoritesRepository postsFavoritesRepository;
    @Autowired
    private CommentsRepository commentsRepository;
    @Autowired
    private CommentUpvotesRepository commentUpvotesRepository;
    @Autowired
    private CommentAttachmentsRepository commentAttachmentsRepository;
    @Autowired
    private CommentsFavoritesRepository commentsFavoritesRepository;

    private final String accessToken = "4a42f24d-208e-4e08-8f1f-51db0b960a4f:ROLE_USER,ROLE_ADMIN";
    private final String anotherAccessToken = "5a42f24d-208e-4e08-8f1f-51db0b960a4e:ROLE_USER,ROLE_ADMIN";
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        usersRepository.insert("4a42f24d-208e-4e08-8f1f-51db0b960a4f", "juser");
        usersRepository.insert("5a42f24d-208e-4e08-8f1f-51db0b960a4e", "another_user");
        createPost();
    }

    @AfterEach
    public void teardown() {
        postsRepository.deleteAll();
        usersRepository.delete("4a42f24d-208e-4e08-8f1f-51db0b960a4f");
        usersRepository.delete("5a42f24d-208e-4e08-8f1f-51db0b960a4e");
    }

    @Test
    public void testCreateComment_emptyContent() throws Exception {
        var request = new CommentRequest();
        request.setContent("");
        var jsonBody = new MockMultipartFile("commentRequest", "", "application/json", objectMapper.writeValueAsString(request).getBytes());
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts/comments/post/post_id").file(jsonBody)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(2)))
                .andExpect(jsonPath("$.errors.[0].field").value("content"))
                .andExpect(jsonPath("$.errors.[1].field").value("content"))
                .andExpect(jsonPath("$.errors.[*].message").value(Matchers.containsInAnyOrder("must not be blank", "size must be between 5 and 2137")));
    }

    @Test
    public void testCreateComment_noAttachments() throws Exception {
        var request = new CommentRequest();
        request.setContent("Comment 1");
        var jsonBody = new MockMultipartFile("commentRequest", "", "application/json", objectMapper.writeValueAsString(request).getBytes());
        var result = mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts/comments/post/post_id").file(jsonBody)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.content").value("Comment 1"))
                .andReturn();

        var commentId = JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();
        var comment = commentsRepository.selectById(commentId);
        var comments = commentsRepository.select("post_id");
        assertEquals(2, comments.size());
        assertTrue(comment.isPresent());
        assertEquals("Comment 1", comment.get().getContent());
    }

    @Test
    public void testCreateComment_tooManyAttachments() throws Exception {
        var request = new CommentRequest();
        request.setContent("alsdjflasj");
        var jsonBody = new MockMultipartFile("commentRequest", "", "application/json", objectMapper.writeValueAsString(request).getBytes());
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts/comments/post/post_id")
                        .file(jsonBody)
                        .file(new MockMultipartFile("file", "", "image/png", "alsdfjlaksdjf".getBytes()))
                        .file(new MockMultipartFile("file", "", "image/gif", "alsdfjlaksdjf".getBytes()))
                        .file(new MockMultipartFile("file", "", "image/jpeg", "alsdfjlaksdjf".getBytes()))
                        .file(new MockMultipartFile("file", "", "image/png", "alsdfjlaksdjf".getBytes()))
                        .file(new MockMultipartFile("file", "", "image/jpg", "alsdfjlaksdjf".getBytes()))
                        .file(new MockMultipartFile("file", "", "image/png", "alsdfjlaksdjf".getBytes()))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].field").value("file"))
                .andExpect(jsonPath("$.errors.[*].message").value("Too many files"));
    }

    @Test
    public void testCreateComment_emptyAttachment() throws Exception {
        var request = new CommentRequest();
        request.setContent("alsdjflasj");
        var jsonBody = new MockMultipartFile("commentRequest", "", "application/json", objectMapper.writeValueAsString(request).getBytes());
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts/comments/post/post_id")
                        .file(jsonBody)
                        .file(new MockMultipartFile("file", "", "image/png", "alsdfjlaksdjf".getBytes()))
                        .file(new MockMultipartFile("file", "", "image/jpeg", "".getBytes()))
                        .file(new MockMultipartFile("file", "", "image/png", "alsdfjlaksdjf".getBytes()))
                        .file(new MockMultipartFile("file", "", "image/jpg", "alsdfjlaksdjf".getBytes()))
                        .file(new MockMultipartFile("file", "", "image/png", "alsdfjlaksdjf".getBytes()))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].field").value("file"))
                .andExpect(jsonPath("$.errors.[*].message").value("At least one file is empty"));
    }

    @Test
    public void testCreateComment_incorrectExtension() throws Exception {
        var request = new CommentRequest();
        request.setContent("alsdjflasj");
        var jsonBody = new MockMultipartFile("commentRequest", "", "application/json", objectMapper.writeValueAsString(request).getBytes());
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts/comments/post/post_id")
                        .file(jsonBody)
                        .file(new MockMultipartFile("file", "", "application/json", "alsdfjlaksdjf".getBytes()))
                        .file(new MockMultipartFile("file", "", "image/png", "alsdfjlaksdjf".getBytes()))
                        .file(new MockMultipartFile("file", "", "image/jpg", "alsdfjlaksdjf".getBytes()))
                        .file(new MockMultipartFile("file", "", "image/png", "alsdfjlaksdjf".getBytes()))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].field").value("file"))
                .andExpect(jsonPath("$.errors.[*].message").value("At least one file has incorrect extension"));
    }

    @Test
    public void testCreateComment_postNotFound() throws Exception {
        var request = new CommentRequest();
        request.setContent("alsdjflasj");
        var jsonBody = new MockMultipartFile("commentRequest", "", "application/json", objectMapper.writeValueAsString(request).getBytes());
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts/comments/post/post_id1")
                        .file(jsonBody)
                        .file(new MockMultipartFile("file", "", "image/png", "alsdfjlaksdjf".getBytes()))
                        .file(new MockMultipartFile("file", "", "image/jpg", "alsdfjlaksdjf".getBytes()))
                        .file(new MockMultipartFile("file", "", "image/png", "alsdfjlaksdjf".getBytes()))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[*].message").value("Not found post with id: post_id1"));
    }

    @Test
    public void testDeleteComment_notOwn() throws Exception {
        commentsRepository.insert(new CommentEntity("comment_id1", "post_id", "5a42f24d-208e-4e08-8f1f-51db0b960a4e", "asdfj"));
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/posts/comments/comment_id1")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message").value("Cannot delete comment"))
                .andExpect(jsonPath("$.errors.[0].field").value("commentId"));
    }

    @Test
    public void testDeleteComment_postOwner() throws Exception {
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/posts/comments/comment_id")
                        .header("Authorization", "Bearer " + anotherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        var comment = commentsRepository.selectById("comment_id");
        var commentUpvotes = commentUpvotesRepository.select("comment_id");
        var commentAttachments = commentAttachmentsRepository.select("comment_id");

        assertFalse(commentsFavoritesRepository.exists("comment_id", "5a42f24d-208e-4e08-8f1f-51db0b960a4e"));
        assertTrue(comment.isEmpty());
        assertTrue(commentUpvotes.isEmpty());
        assertTrue(commentAttachments.isEmpty());
    }

    @Test
    public void testDeleteComment_commentOwner() throws Exception {
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/posts/comments/comment_id")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        var comment = commentsRepository.selectById("comment_id");
        var commentUpvotes = commentUpvotesRepository.select("comment_id");
        var commentAttachments = commentAttachmentsRepository.select("comment_id");

        assertFalse(commentsFavoritesRepository.exists("comment_id", "5a42f24d-208e-4e08-8f1f-51db0b960a4e"));
        assertTrue(comment.isEmpty());
        assertTrue(commentUpvotes.isEmpty());
        assertTrue(commentAttachments.isEmpty());
    }

    @Test
    public void testToggleFavorite() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/comments/comment_id/favorite")
                        .header("Authorization", "Bearer " + anotherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertFalse(commentsFavoritesRepository.exists("comment_id", "5a42f24d-208e-4e08-8f1f-51db0b960a4e"));

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/comments/comment_id/favorite")
                        .header("Authorization", "Bearer " + anotherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertTrue(commentsFavoritesRepository.exists("comment_id", "5a42f24d-208e-4e08-8f1f-51db0b960a4e"));
    }

    @Test
    public void testToggleFavorite_notFound() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/comments/comment_id1/favorite")
                        .header("Authorization", "Bearer " + anotherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message").value("Not found comment with id: comment_id1"));
    }

    @Test
    public void testToggleUpvote() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/comments/comment_id/upvote")
                        .header("Authorization", "Bearer " + anotherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertFalse(commentUpvotesRepository.exists("comment_id", "5a42f24d-208e-4e08-8f1f-51db0b960a4e"));

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/comments/comment_id/upvote")
                        .header("Authorization", "Bearer " + anotherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertTrue(commentUpvotesRepository.exists("comment_id", "5a42f24d-208e-4e08-8f1f-51db0b960a4e"));
    }

    @Test
    public void testToggleUpvote_ownComment() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/comments/comment_id/upvote")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message").value("You cannot upvote your own comment"))
                .andExpect(jsonPath("$.errors.[0].field").value("commentId"));
    }

    @Test
    public void testToggleUpvote_notFound() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/comments/comment_id2/upvote")
                        .header("Authorization", "Bearer " + anotherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message").value("Not found comment with id: comment_id2"));
    }

    @Test
    public void testAddAttachment_incorrectExtension() throws Exception {
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts/comments/comment_id/attachment")
                        .file(new MockMultipartFile("file", "", "application/json", "alsdfjlaksdjf".getBytes()))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].field").value("file"))
                .andExpect(jsonPath("$.errors.[*].message").value("Incorrect content type"));
    }

    @Test
    public void testAddAttachment_emptyFile() throws Exception {
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts/comments/comment_id/attachment")
                        .file(new MockMultipartFile("file", "", "image/png", "".getBytes()))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].field").value("file"))
                .andExpect(jsonPath("$.errors.[*].message").value("File cannot be null or empty"));
    }

    @Test
    public void testAddAttachment_notFound() throws Exception {
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts/comments/comment_id1/attachment")
                        .file(new MockMultipartFile("file", "", "image/png", "asdfasdf".getBytes()))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[*].message").value("Not found comment with id: comment_id1"));
    }

    @Test
    public void testAddAttachment_notOwnComment() throws Exception {
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts/comments/comment_id/attachment")
                        .file(new MockMultipartFile("file", "", "image/png", "adsfaas".getBytes()))
                        .header("Authorization", "Bearer " + anotherAccessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].field").value("commentId"))
                .andExpect(jsonPath("$.errors.[*].message").value("Cannot modify not own comment"));
    }

    @Test
    public void testAddAttachment_tooMany() throws Exception {
        commentAttachmentsRepository.insert(new CommentAttachmentEntity("comment_id", "asldkfj1"));
        commentAttachmentsRepository.insert(new CommentAttachmentEntity("comment_id", "asldkfj2"));
        commentAttachmentsRepository.insert(new CommentAttachmentEntity("comment_id", "asldkfj3"));
        commentAttachmentsRepository.insert(new CommentAttachmentEntity("comment_id", "asldkfj4"));
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts/comments/comment_id/attachment")
                        .file(new MockMultipartFile("file", "", "image/png", "adsfaas".getBytes()))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].field").value("file"))
                .andExpect(jsonPath("$.errors.[*].message").value("Too many files"));
    }

    @Test
    public void testAddAttachment() throws Exception {
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts/comments/comment_id/attachment")
                        .file(new MockMultipartFile("file", "", "image/png", "adsfaas".getBytes()))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk());
        var attachments = commentAttachmentsRepository.select("comment_id");
        assertEquals(2, attachments.size());
    }

    @Test
    public void testRemoveAttachment_commentNotFound() throws Exception {
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/posts/comments/comment_id/attachment/asdf")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[*].message").value("Not found comment with id: comment_id"));
    }

    @Test
    public void testRemoveAttachment_notOwnComment() throws Exception {
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/posts/comments/comment_id/attachment/alsdjkf")
                        .header("Authorization", "Bearer " + anotherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[*].field").value("commentId"))
                .andExpect(jsonPath("$.errors.[*].message").value("Cannot modify not own comment"));
    }

    @Test
    public void testRemoveAttachment_attachmentNotFound() throws Exception {
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/posts/comments/comment_id/attachment/111")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[*].message").value("Not found attachment with id: 111"));
    }

    @Test
    public void testRemoveAttachment() throws Exception {
        mockMvc.perform(request(HttpMethod.DELETE, "/api/v1/posts/comments/comment_id/attachment/alsdjkf")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertFalse(commentAttachmentsRepository.selectByUrl("comment_id", "alsdjkf").isPresent());
    }

    @Test
    public void testUpdateComment_notFound() throws Exception {
        var request = new CommentRequest();
        request.setContent("asdfa1");
        mockMvc.perform(request(HttpMethod.PUT, "/api/v1/posts/comments/comment_id1")
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message").value("Not found comment with id: comment_id1"));
    }

    @Test
    public void testUpdateComment_notOwnComment() throws Exception {
        var request = new CommentRequest();
        request.setContent("asdfa1");
        mockMvc.perform(request(HttpMethod.PUT, "/api/v1/posts/comments/comment_id")
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + anotherAccessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message").value("Cannot modify not own comment"))
                .andExpect(jsonPath("$.errors.[0].field").value("commentId"));
    }

    @Test
    public void testUpdateComment_blankContent() throws Exception {
        var request = new PostRequest();
        request.setContent("");
        mockMvc.perform(request(HttpMethod.PUT, "/api/v1/posts/comments/comment_id")
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(2)))
                .andExpect(jsonPath("$.errors.[0].field").value("content"))
                .andExpect(jsonPath("$.errors.[1].field").value("content"))
                .andExpect(jsonPath("$.errors.[*].message").value(Matchers.containsInAnyOrder("must not be blank", "size must be between 5 and 2137")));
    }

    @Test
    public void testUpdateComment() throws Exception {
        var request = new CommentRequest();
        request.setContent("new content");
        mockMvc.perform(request(HttpMethod.PUT, "/api/v1/posts/comments/comment_id")
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("comment_id"))
                .andExpect(jsonPath("$.content").value("new content"));

        var comment = commentsRepository.selectById("comment_id");
        assertTrue(comment.isPresent());
        assertEquals("new content", comment.get().getContent());
    }

    private void createPost() {
        postsRepository.insert("post_id", "5a42f24d-208e-4e08-8f1f-51db0b960a4e", "asldfkj");
        tagsRepository.insert("jklee");
        postTagsRepository.insert(new PostTagEntity("jklee", "post_id"));
        postAttachmentsRepository.insert(new PostAttachmentEntity("post_id", "asldkfj"));
        postUpvotesRepository.insert(new PostUpvoteEntity("post_id", "4a42f24d-208e-4e08-8f1f-51db0b960a4f"));
        postsFavoritesRepository.insert("post_id", "4a42f24d-208e-4e08-8f1f-51db0b960a4f");

        commentsRepository.insert(new CommentEntity("comment_id", "post_id", "4a42f24d-208e-4e08-8f1f-51db0b960a4f", "asdfj"));
        commentsFavoritesRepository.insert("comment_id", "5a42f24d-208e-4e08-8f1f-51db0b960a4e");
        commentUpvotesRepository.insert(new CommentUpvoteEntity("comment_id", "5a42f24d-208e-4e08-8f1f-51db0b960a4e"));
        commentAttachmentsRepository.insert(new CommentAttachmentEntity("comment_id", "alsdjkf"));
    }
}
