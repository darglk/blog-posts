package com.darglk.blogposts.rest;

import com.darglk.blogposts.BlogPostsApplication;
import com.darglk.blogposts.repository.TagsBlacklistRepository;
import com.darglk.blogposts.repository.TagsFavoritesRepository;
import com.darglk.blogposts.repository.TagsRepository;
import com.darglk.blogposts.repository.UsersRepository;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
public class TagsControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private TagsRepository tagsRepository;
    @Autowired
    private TagsBlacklistRepository tagsBlacklistRepository;
    @Autowired
    private TagsFavoritesRepository tagsFavoritesRepository;

    private final String accessToken = "4a42f24d-208e-4e08-8f1f-51db0b960a4f:ROLE_USER,ROLE_ADMIN";

    @BeforeEach
    public void setup() {
        usersRepository.insert("4a42f24d-208e-4e08-8f1f-51db0b960a4f", "juser");
        tagsRepository.insert("asdftag");
    }

    @AfterEach
    public void teardown() {
        tagsBlacklistRepository.deleteAll();
        tagsFavoritesRepository.deleteAll();
        usersRepository.delete("4a42f24d-208e-4e08-8f1f-51db0b960a4f");
    }

    @Test
    public void testToggleBlacklistTag() throws Exception {
        tagsFavoritesRepository.insert("asdftag", "4a42f24d-208e-4e08-8f1f-51db0b960a4f");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/tags/blacklist/asdftag")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertTrue(tagsBlacklistRepository.exists("asdftag", "4a42f24d-208e-4e08-8f1f-51db0b960a4f"));
        assertFalse(tagsFavoritesRepository.exists("asdftag", "4a42f24d-208e-4e08-8f1f-51db0b960a4f"));

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/tags/blacklist/asdftag")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertFalse(tagsBlacklistRepository.exists("asdftag", "4a42f24d-208e-4e08-8f1f-51db0b960a4f"));
    }

    @Test
    public void testToggleBlacklistTag_notFound() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/tags/blacklist/nope")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[*].message").value("Not found tag with name: nope"));
    }

    @Test
    public void testToggleFavoriteTag() throws Exception {
        tagsBlacklistRepository.insert("asdftag", "4a42f24d-208e-4e08-8f1f-51db0b960a4f");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/tags/favorite/asdftag")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertTrue(tagsFavoritesRepository.exists("asdftag", "4a42f24d-208e-4e08-8f1f-51db0b960a4f"));
        assertFalse(tagsBlacklistRepository.exists("asdftag", "4a42f24d-208e-4e08-8f1f-51db0b960a4f"));

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/tags/favorite/asdftag")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertFalse(tagsFavoritesRepository.exists("asdftag", "4a42f24d-208e-4e08-8f1f-51db0b960a4f"));
    }

    @Test
    public void testToggleFavoriteTag_notFound() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/tags/favorite/nope")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[*].message").value("Not found tag with name: nope"));
    }
}
