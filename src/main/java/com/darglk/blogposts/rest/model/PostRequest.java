package com.darglk.blogposts.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PostRequest {
    @NotBlank
    @Size(min = 5, max = 2137)
    private String content;
    private List<@Valid TagRequest> tags;
}
