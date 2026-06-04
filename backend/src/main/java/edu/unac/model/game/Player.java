package edu.unac.model.game;

import edu.unac.model.card.Card;
import edu.unac.model.enums.PlayerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    private UUID id;

    private String name;

    @Builder.Default
    private Integer totalScore = 0;

    @Builder.Default
    private Integer roundScore = 0;

    @Builder.Default
    private List<Card> hand = new ArrayList<>();

    @Builder.Default
    private PlayerStatus status = PlayerStatus.ACTIVE;

    @Builder.Default
    private boolean secondChance = false;

    @Builder.Default
    private boolean flippedSeven = false;
}