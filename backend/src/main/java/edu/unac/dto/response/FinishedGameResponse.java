package edu.unac.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class FinishedGameResponse {

    private UUID id;

    private Integer currentRound;

    private WinnerResponse winner;

    private Integer playersCount;
}