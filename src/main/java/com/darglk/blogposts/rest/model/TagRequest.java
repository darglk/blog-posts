package com.darglk.blogposts.rest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TagRequest {
    @NotBlank
    @Size(min = 2, max = 100)
    private String tag;
}
