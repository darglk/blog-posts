package com.darglk.blogposts.config;

import com.darglk.blogcommons.filter.AuthUserService;
import com.darglk.blogcommons.filter.JwtAuthenticationFilter;
import com.darglk.blogcommons.filter.MockAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${auth-service.url}")
    private String authServiceUrl;
    @Value("${auth-service.port}")
    private String authServicePort;
    @Value("${keycloak.api.key}")
    private String jwtKey;
    @Value("${keycloak.server.url}")
    private String keycloakServerUrl;
    @Value("${keycloak.server.port}")
    private String keycloakServerPort;
    @Value("${keycloak.admin.username}")
    private String keycloakAdminUsername;
    @Value("${keycloak.admin.password}")
    private String keycloakAdminPassword;
    @Value("${keycloak.admin.realm}")
    private String keycloakAdminRealm;
    @Value("${keycloak.admin.client-id}")
    private String keycloakAdminClientId;
    @Value("${keycloak.api.realm}")
    private String keycloakApiRealm;

    @Autowired
    private Environment env;

    @Override
    public void configure(WebSecurity webSecurity) {
        webSecurity.ignoring()
                .antMatchers(HttpMethod.GET, "/api/v1/posts");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/v1/posts")
                .permitAll().and()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .addFilter(
                        !Arrays.asList(env.getActiveProfiles()).contains("test") ?
                                new JwtAuthenticationFilter(
                                        authenticationManager(),
                                        keycloakClient(),
                                        new AuthUserService(authServiceUrl, authServicePort),
                                        jwtKey
                                ) : new MockAuthenticationFilter(authenticationManager())
                );
    }

    @Bean
    public AuthenticationManager authenticationManagerLoad() throws Exception {
        return authenticationManager();
    }

    @Bean
    public RealmResource keycloakClient() {
        var serverUrl = String.format("http://%s:%s", keycloakServerUrl, keycloakServerPort);
        return Keycloak.getInstance(
                serverUrl,
                keycloakAdminRealm,
                keycloakAdminUsername,
                keycloakAdminPassword,
                keycloakAdminClientId).realm(keycloakApiRealm);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
