package edu.unac.model.game;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "round_player_scores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoundPlayerScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID playerId;

    private String playerName;

    private Integer score;

    private boolean busted;

    private boolean flippedSeven;

}
