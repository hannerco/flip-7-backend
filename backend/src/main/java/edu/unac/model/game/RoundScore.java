package edu.unac.model.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoundScore {

    private UUID playerId;

    private String playerName;

    private Integer score;

    private boolean busted;

    private boolean flippedSeven;
}