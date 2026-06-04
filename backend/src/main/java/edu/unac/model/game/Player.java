package edu.unac.model.game;

import edu.unac.model.card.Card;
import edu.unac.model.enums.PlayerStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "players")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Builder.Default
    private Integer totalScore = 0;

    @Builder.Default
    private Integer roundScore = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PlayerStatus status = PlayerStatus.ACTIVE;

    @Builder.Default
    private boolean secondChance = false;

    @Builder.Default
    private boolean flippedSeven = false;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "player_id")
    @Builder.Default
    private List<Card> hand = new ArrayList<>();
}