package edu.unac.controller;

import edu.unac.dto.request.ApplyActionRequest;
import edu.unac.dto.request.CreateGameRequest;
import edu.unac.dto.request.DrawCardRequest;
import edu.unac.dto.request.StayRequest;
import edu.unac.dto.response.FinishedGameResponse;
import edu.unac.dto.response.CreateGameResponse;
import edu.unac.dto.response.GameResponse;
import edu.unac.mapper.GameMapper;
import edu.unac.model.game.Game;
import edu.unac.service.GameService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;
    private final GameMapper gameMapper;

    public GameController(GameService gameService, GameMapper gameMapper) {
        this.gameService = gameService;
        this.gameMapper = gameMapper;
    }

    @PostMapping
    public CreateGameResponse createGame(
            @RequestBody CreateGameRequest request
    ) {

        Game game = gameService.createGame(
                request.getPlayerNames()
        );

        return gameMapper.toCreateResponse(game);
    }

    @GetMapping("/{gameId}")
    public GameResponse getGame(
            @PathVariable UUID gameId
    ) {

        Game game = gameService.getGameById(gameId);

        return gameMapper.toResponse(game);
    }

        @GetMapping("/finished")
        public List<FinishedGameResponse> getFinishedGames() {

                return gameService.getFinishedGames()
                                .stream()
                                .map(gameMapper::toFinishedGameResponse)
                                .toList();
        }

        @GetMapping("/finished/{gameId}")
        public GameResponse getFinishedGame(
                        @PathVariable UUID gameId
        ) {

                Game game = gameService.getFinishedGameById(gameId);

                return gameMapper.toResponse(game);
        }

    @PostMapping("/{gameId}/rounds/start")
    public GameResponse startRound(
            @PathVariable UUID gameId
    ) {

        return gameMapper.toResponse(
                gameService.startRound(gameId)
        );
    }

    @PostMapping("/{gameId}/draw")
    public GameResponse drawCard(
            @PathVariable UUID gameId,
            @RequestBody DrawCardRequest request
    ) {

        return gameMapper.toResponse(
                gameService.drawCard(
                        gameId,
                        request.getPlayerId()
                )
        );
    }

    @PostMapping("/{gameId}/stay")
    public GameResponse stay(
            @PathVariable UUID gameId,
            @RequestBody StayRequest request
    ) {

        return gameMapper.toResponse(
                gameService.stay(
                        gameId,
                        request.getPlayerId()
                )
        );
    }

    @PostMapping("/{gameId}/actions")
    public GameResponse applyAction(
            @PathVariable UUID gameId,
            @RequestBody ApplyActionRequest request
    ) {

        return gameMapper.toResponse(
                gameService.applyAction(
                        gameId,
                        request.getTargetPlayerId()
                )
        );
    }
}