package com.darglk.blogposts.rest;

import com.darglk.blogposts.BlogPostsApplication;
import com.darglk.blogposts.repository.PostTagsRepository;
import com.darglk.blogposts.repository.PostsRepository;
import com.darglk.blogposts.repository.TagsRepository;
import com.darglk.blogposts.repository.UsersRepository;
import com.darglk.blogposts.rest.model.PostRequest;
import com.darglk.blogposts.rest.model.TagRequest;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        classes = { BlogPostsApplication.class, TestConfiguration.class })
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class PostControllerTest {

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

    private final String accessToken = "4a42f24d-208e-4e08-8f1f-51db0b960a4f:ROLE_USER,ROLE_ADMIN";
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        usersRepository.insert("4a42f24d-208e-4e08-8f1f-51db0b960a4f", "juser");
    }

    @AfterEach
    public void teardown() {
        postsRepository.deleteAll();
        usersRepository.delete("4a42f24d-208e-4e08-8f1f-51db0b960a4f");

    }

    @Test
    public void testCreatePost_emptyContent() throws Exception {
        var request = new PostRequest();
        request.setContent("");
        request.setTags(List.of(new TagRequest("asdf")));
        var jsonBody = new MockMultipartFile("postRequest", "", "application/json", objectMapper.writeValueAsString(request).getBytes());
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts").file(jsonBody)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(2)))
                .andExpect(jsonPath("$.errors.[0].field").value("content"))
                .andExpect(jsonPath("$.errors.[1].field").value("content"))
                .andExpect(jsonPath("$.errors.[*].message").value(Matchers.containsInAnyOrder("must not be blank", "size must be between 5 and 2137")));
    }

    @Test
    public void testCreatePost_emptyTag() throws Exception {
        var request = new PostRequest();
        request.setContent("alsdjflasj");
        request.setTags(List.of(new TagRequest("")));
        var jsonBody = new MockMultipartFile("postRequest", "", "application/json", objectMapper.writeValueAsString(request).getBytes());
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts").file(jsonBody)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(2)))
                .andExpect(jsonPath("$.errors.[0].field").value("tags[0].tag"))
                .andExpect(jsonPath("$.errors.[1].field").value("tags[0].tag"))
                .andExpect(jsonPath("$.errors.[*].message").value(Matchers.containsInAnyOrder("must not be blank", "size must be between 2 and 100")));
    }

    @Test
    public void testCreatePost_emptyTagList() throws Exception {
        var request = new PostRequest();
        request.setContent("alsdjflasj");
        request.setTags(List.of());
        var jsonBody = new MockMultipartFile("postRequest", "", "application/json", objectMapper.writeValueAsString(request).getBytes());
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts").file(jsonBody)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.content").value("alsdjflasj"));
    }

    @Test
    public void testCreatePost_noAttachments() throws Exception {
        var request = new PostRequest();
        request.setContent("alsdjflasj");
        request.setTags(List.of(new TagRequest("asdf")));
        var jsonBody = new MockMultipartFile("postRequest", "", "application/json", objectMapper.writeValueAsString(request).getBytes());
        var result = mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts").file(jsonBody)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.content").value("alsdjflasj"))
                .andReturn();

        var postId = JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();
        var postTags = postTagsRepository.select(postId);
        var post = postsRepository.selectById(postId);
        assertTrue(tagsRepository.exists("asdf"));
        assertEquals(1, postTags.size());
        assertEquals("asdf", postTags.get(0).getTagName());
        assertTrue(post.isPresent());
        assertEquals("alsdjflasj", post.get().getContent());
    }

    @Test
    public void testCreatePost_tooManyAttachments() throws Exception {
        var request = new PostRequest();
        request.setContent("alsdjflasj");
        request.setTags(List.of());
        var jsonBody = new MockMultipartFile("postRequest", "", "application/json", objectMapper.writeValueAsString(request).getBytes());
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts")
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
    public void testCreatePost_emptyAttachment() throws Exception {
        var request = new PostRequest();
        request.setContent("alsdjflasj");
        request.setTags(List.of());
        var jsonBody = new MockMultipartFile("postRequest", "", "application/json", objectMapper.writeValueAsString(request).getBytes());
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts")
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
    public void testCreatePost_incorrectExtension() throws Exception {
        var request = new PostRequest();
        request.setContent("alsdjflasj");
        request.setTags(List.of());
        var jsonBody = new MockMultipartFile("postRequest", "", "application/json", objectMapper.writeValueAsString(request).getBytes());
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/posts")
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
}
