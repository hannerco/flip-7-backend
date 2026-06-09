package edu.unac.model.game;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "round_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoundResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Integer roundNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    @lombok.ToString.Exclude
    @lombok.EqualsAndHashCode.Exclude
    private Game game;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "round_result_id")
    @Builder.Default
    private List<RoundPlayerScore> scores = new ArrayList<>();

}
