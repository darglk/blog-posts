package com.darglk.blogposts.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommentRequest {
    @NotBlank
    @Size(min = 5, max = 2137)
    private String content;
}
