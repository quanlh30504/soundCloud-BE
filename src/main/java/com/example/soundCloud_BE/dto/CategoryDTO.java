package com.example.soundCloud_BE.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.michaelthelin.spotify.model_objects.specification.Category;
import se.michaelthelin.spotify.model_objects.specification.Image;
import java.util.Arrays;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private String id;
    private String name;
    private String href;
    private String iconUrl;
    private String[] icons;

    public static CategoryDTO fromEntity(Category category) {
        return CategoryDTO.builder()
            .id(category.getId())
            .name(category.getName())
            .href(category.getHref())
            .iconUrl(category.getIcons()[0].getUrl())
            .icons(Arrays.stream(category.getIcons())
                .map(Image::getUrl)
                .toArray(String[]::new))
            .build();
    }
} 