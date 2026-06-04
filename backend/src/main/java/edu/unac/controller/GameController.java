package edu.unac.controller;

import edu.unac.dto.request.CreateGameRequest;
import edu.unac.model.game.Game;
import edu.unac.service.GameService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public Game createGame(
            @RequestBody CreateGameRequest request
    ) {

        return gameService.createGame(
                request.getPlayerNames()
        );
    }

    @GetMapping("/{gameId}")
    public Game getGame(
            @PathVariable UUID gameId
    ) {

        return gameService.getGameById(gameId);
    }
}