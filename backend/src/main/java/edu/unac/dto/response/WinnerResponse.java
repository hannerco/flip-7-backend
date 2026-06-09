package edu.unac.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class WinnerResponse {

    private UUID id;

    private String name;

    private Integer totalScore;
}