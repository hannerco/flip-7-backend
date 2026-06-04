package edu.unac.model.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Winner {

    private UUID id;

    private String name;

    private Integer totalScore;
}