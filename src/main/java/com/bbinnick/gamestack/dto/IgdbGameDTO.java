package com.bbinnick.gamestack.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IgdbGameDTO {
    private Long id;
    private String name;
    private List<String> platforms;
    private List<String> genres;
    private String coverUrl;
    private Double rating;
    private String summary;
}