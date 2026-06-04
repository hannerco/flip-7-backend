package edu.unac.model.game;

import edu.unac.model.card.Card;
import edu.unac.model.enums.GameStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Entity
@Table(name = "games")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private GameStatus status = GameStatus.WAITING;

    @Builder.Default
    private Integer currentRound = 0;

    private UUID currentPlayerId;

    private UUID dealerId;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "game_id")
    @Builder.Default
    private List<Player> players = new ArrayList<>();

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "deck_game_id")
    @Builder.Default
    private List<Card> deck = new ArrayList<>();

    /*
     * HU-A2
     * Requiere definir completamente la persistencia de cartas.
     */
    // private List<Card> discardPile;

    /*
     * HU-B2
     */
    // private PendingAction pendingAction;

    /*
     * HU-B5
     */
    // private List<RoundResult> roundHistory;

    /*
     * HU-B6
     */
    // private Winner winner;
}