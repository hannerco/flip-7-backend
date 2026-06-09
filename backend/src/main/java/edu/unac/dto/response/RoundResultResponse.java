package edu.unac.dto.response;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class RoundResultResponse {

    private UUID id;

    private Integer roundNumber;

    private List<RoundPlayerScoreResponse> scores;
}