package edu.unac.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class StayRequest {
    private UUID playerId;
}
