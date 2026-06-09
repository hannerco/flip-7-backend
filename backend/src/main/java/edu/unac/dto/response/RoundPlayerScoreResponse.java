package edu.unac.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class RoundPlayerScoreResponse {

    private UUID id;

    private UUID playerId;

    private String playerName;

    private Integer score;

    private boolean busted;

    private boolean flippedSeven;
}