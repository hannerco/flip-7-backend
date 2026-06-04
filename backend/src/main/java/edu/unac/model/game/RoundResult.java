package edu.unac.model.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoundResult {

    private Integer round;

    private UUID dealerId;

    private List<RoundScore> scores;
}