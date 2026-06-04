package edu.unac.model.game;

import edu.unac.model.card.Card;
import edu.unac.model.enums.GameStatus;
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
public class Game {

    private UUID id;

    @Builder.Default
    private GameStatus status = GameStatus.WAITING;

    @Builder.Default
    private Integer currentRound = 0;

    private UUID currentPlayerId;

    private UUID dealerId;

    private Winner winner;

    @Builder.Default
    private List<Player> players = new ArrayList<>();

    @Builder.Default
    private List<Card> deck = new ArrayList<>();

    @Builder.Default
    private List<Card> discardPile = new ArrayList<>();

    private PendingAction pendingAction;

    @Builder.Default
    private List<RoundResult> roundHistory = new ArrayList<>();
}