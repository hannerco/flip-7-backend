package edu.unac.dto.response;

import edu.unac.model.enums.PlayerStatus;
import lombok.Data;

import java.util.UUID;
import java.util.List;
import java.util.List;
import edu.unac.dto.response.CardResponse;

@Data
public class PlayerResponse {
    private UUID id;
    private String name;
    private Integer totalScore;
    private Integer roundScore;
    private PlayerStatus status;
    private boolean flippedSeven;
    private List<CardResponse> cards;
}
