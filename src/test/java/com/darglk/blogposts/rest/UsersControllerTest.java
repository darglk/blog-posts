package com.darglk.blogposts.rest;

import com.darglk.blogposts.BlogPostsApplication;
import com.darglk.blogposts.repository.UsersBlacklistRepository;
import com.darglk.blogposts.repository.UsersFavoritesRepository;
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
public class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private UsersBlacklistRepository usersBlacklistRepository;
    @Autowired
    private UsersFavoritesRepository usersFavoritesRepository;

    private final String accessToken = "4a42f24d-208e-4e08-8f1f-51db0b960a4f:ROLE_USER,ROLE_ADMIN";
    private final String anotherAccessToken = "5a42f24d-208e-4e08-8f1f-51db0b960a4e:ROLE_USER,ROLE_ADMIN";

    @BeforeEach
    public void setup() {
        usersRepository.insert("4a42f24d-208e-4e08-8f1f-51db0b960a4f", "juser");
        usersRepository.insert("5a42f24d-208e-4e08-8f1f-51db0b960a4e", "another_user");
    }

    @AfterEach
    public void teardown() {
        usersFavoritesRepository.deleteAll();
        usersBlacklistRepository.deleteAll();
        usersRepository.delete("4a42f24d-208e-4e08-8f1f-51db0b960a4f");
        usersRepository.delete("5a42f24d-208e-4e08-8f1f-51db0b960a4e");
    }

    @Test
    public void testToggleBlacklistUser() throws Exception {
        usersFavoritesRepository.insert("5a42f24d-208e-4e08-8f1f-51db0b960a4e", "4a42f24d-208e-4e08-8f1f-51db0b960a4f");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/users/blacklist/5a42f24d-208e-4e08-8f1f-51db0b960a4e")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertTrue(usersBlacklistRepository.exists("5a42f24d-208e-4e08-8f1f-51db0b960a4e", "4a42f24d-208e-4e08-8f1f-51db0b960a4f"));
        assertFalse(usersFavoritesRepository.exists("5a42f24d-208e-4e08-8f1f-51db0b960a4e", "4a42f24d-208e-4e08-8f1f-51db0b960a4f"));

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/users/blacklist/5a42f24d-208e-4e08-8f1f-51db0b960a4e")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertFalse(usersBlacklistRepository.exists("5a42f24d-208e-4e08-8f1f-51db0b960a4e", "4a42f24d-208e-4e08-8f1f-51db0b960a4f"));
    }

    @Test
    public void testToggleBlacklistUser_self() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/users/blacklist/4a42f24d-208e-4e08-8f1f-51db0b960a4f")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[*].field").value("userId"))
                .andExpect(jsonPath("$.errors.[*].message").value("You cannot blacklist yourself"));
    }

    @Test
    public void testToggleBlacklistUser_notFound() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/users/blacklist/nope")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[*].message").value("Not found user with id: nope"));
    }

    @Test
    public void testToggleFavoriteUser() throws Exception {
        usersBlacklistRepository.insert("5a42f24d-208e-4e08-8f1f-51db0b960a4e", "4a42f24d-208e-4e08-8f1f-51db0b960a4f");
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/users/favorite/5a42f24d-208e-4e08-8f1f-51db0b960a4e")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertTrue(usersFavoritesRepository.exists("5a42f24d-208e-4e08-8f1f-51db0b960a4e", "4a42f24d-208e-4e08-8f1f-51db0b960a4f"));
        assertFalse(usersBlacklistRepository.exists("5a42f24d-208e-4e08-8f1f-51db0b960a4e", "4a42f24d-208e-4e08-8f1f-51db0b960a4f"));

        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/users/favorite/5a42f24d-208e-4e08-8f1f-51db0b960a4e")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertFalse(usersFavoritesRepository.exists("5a42f24d-208e-4e08-8f1f-51db0b960a4e", "4a42f24d-208e-4e08-8f1f-51db0b960a4f"));
    }

    @Test
    public void testToggleFavoriteUser_notFound() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/users/favorite/nope")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[*].message").value("Not found user with id: nope"));
    }

    @Test
    public void testToggleFavoriteUser_self() throws Exception {
        mockMvc.perform(request(HttpMethod.POST, "/api/v1/posts/users/favorite/4a42f24d-208e-4e08-8f1f-51db0b960a4f")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.[*]", hasSize(1)))
                .andExpect(jsonPath("$.errors.[*].field").value("userId"))
                .andExpect(jsonPath("$.errors.[*].message").value("You cannot favorite yourself"));
    }
}
