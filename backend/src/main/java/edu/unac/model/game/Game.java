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

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "discard_game_id")
    @Builder.Default
    private List<Card> discardPile = new ArrayList<>();

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "game"
    )
    @Builder.Default
    private List<RoundResult> roundHistory = new ArrayList<>();

    private Integer initialDealCardsDealtCount;

    @Builder.Default
    private boolean initialDealPaused = false;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "winner_id")),
            @AttributeOverride(name = "name", column = @Column(name = "winner_name")),
            @AttributeOverride(name = "totalScore", column = @Column(name = "winner_total_score"))
    })
    private Winner winner;

    @OneToOne(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private PendingAction pendingAction;

    @Transient
    private AutomaticEvent lastAutomaticEvent;

}