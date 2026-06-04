package edu.unac.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateGameRequest {

    private List<String> playerNames;
}