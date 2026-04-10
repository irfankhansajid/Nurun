package com.nurun.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateConversationTitleRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title can be up to 120 characters")
    private String title;
}
