package edu.unac.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.unac.dto.response.FinishedGameResponse;
import edu.unac.dto.response.GameResponse;
import edu.unac.dto.response.RoundPlayerScoreResponse;
import edu.unac.dto.response.RoundResultResponse;
import edu.unac.dto.response.WinnerResponse;
import edu.unac.mapper.GameMapper;
import edu.unac.model.enums.GameStatus;
import edu.unac.model.game.Game;
import edu.unac.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GameService gameService;

    @MockBean
    private GameMapper gameMapper;

    @Test
    void shouldApplyActionSuccessfully() throws Exception {

        UUID gameId = UUID.randomUUID();
        UUID targetPlayerId = UUID.randomUUID();

        Game game = Game.builder().id(gameId).build();

        GameResponse response = new GameResponse();
        response.setId(gameId);
        response.setStatus(GameStatus.IN_ROUND);

        when(gameService.applyAction(eq(gameId), eq(targetPlayerId)))
                .thenReturn(game);

        when(gameMapper.toResponse(game)).thenReturn(response);

        String payload = objectMapper.writeValueAsString(
                new ApplyActionBody(targetPlayerId)
        );

        mockMvc.perform(post("/games/{gameId}/actions", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId.toString()))
                .andExpect(jsonPath("$.status").value("IN_ROUND"));

        verify(gameService).applyAction(gameId, targetPlayerId);
        verify(gameMapper).toResponse(game);
    }

    @Test
    void shouldForwardNullTargetPlayerIdWhenMissing() throws Exception {

        UUID gameId = UUID.randomUUID();

        Game game = Game.builder().id(gameId).build();

        GameResponse response = new GameResponse();
        response.setId(gameId);
        response.setStatus(GameStatus.IN_ROUND);

        when(gameService.applyAction(eq(gameId), any()))
                .thenReturn(game);

        when(gameMapper.toResponse(game)).thenReturn(response);

        String payload = "{}";

        mockMvc.perform(post("/games/{gameId}/actions", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId.toString()));

        verify(gameService).applyAction(eq(gameId), any());
    }

        @Test
        void shouldReturnRoundHistoryWhenGettingGame() throws Exception {

                UUID gameId = UUID.randomUUID();
                UUID playerId = UUID.randomUUID();

                Game game = Game.builder().id(gameId).build();

                RoundPlayerScoreResponse scoreResponse = new RoundPlayerScoreResponse();
                scoreResponse.setId(UUID.randomUUID());
                scoreResponse.setPlayerId(playerId);
                scoreResponse.setPlayerName("Alice");
                scoreResponse.setScore(21);
                scoreResponse.setBusted(false);
                scoreResponse.setFlippedSeven(true);

                RoundResultResponse roundResponse = new RoundResultResponse();
                roundResponse.setId(UUID.randomUUID());
                roundResponse.setRoundNumber(2);
                roundResponse.setScores(List.of(scoreResponse));

                GameResponse response = new GameResponse();
                response.setId(gameId);
                response.setStatus(GameStatus.IN_ROUND);
                response.setRoundHistory(List.of(roundResponse));

                when(gameService.getGameById(eq(gameId))).thenReturn(game);
                when(gameMapper.toResponse(game)).thenReturn(response);

                mockMvc.perform(get("/games/{gameId}", gameId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(gameId.toString()))
                                .andExpect(jsonPath("$.roundHistory[0].roundNumber").value(2))
                                .andExpect(jsonPath("$.roundHistory[0].scores[0].playerName").value("Alice"))
                                .andExpect(jsonPath("$.roundHistory[0].scores[0].flippedSeven").value(true));

                verify(gameService).getGameById(gameId);
                verify(gameMapper).toResponse(game);
        }

        @Test
        void shouldListFinishedGames() throws Exception {

                UUID gameId = UUID.randomUUID();

                Game finishedGame = Game.builder()
                                .id(gameId)
                                .status(GameStatus.GAME_OVER)
                                .build();

                WinnerResponse winnerResponse = new WinnerResponse();
                winnerResponse.setId(UUID.randomUUID());
                winnerResponse.setName("Bob");
                winnerResponse.setTotalScore(205);

                FinishedGameResponse finishedGameResponse = new FinishedGameResponse();
                finishedGameResponse.setId(gameId);
                finishedGameResponse.setCurrentRound(4);
                finishedGameResponse.setPlayersCount(2);
                finishedGameResponse.setWinner(winnerResponse);

                when(gameService.getFinishedGames()).thenReturn(List.of(finishedGame));
                when(gameMapper.toFinishedGameResponse(finishedGame)).thenReturn(finishedGameResponse);

                mockMvc.perform(get("/games/finished"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(gameId.toString()))
                                .andExpect(jsonPath("$[0].winner.name").value("Bob"))
                                .andExpect(jsonPath("$[0].playersCount").value(2));

                verify(gameService).getFinishedGames();
                verify(gameMapper).toFinishedGameResponse(finishedGame);
        }

        @Test
        void shouldReturnFinishedGameDetail() throws Exception {

                UUID gameId = UUID.randomUUID();
                Game finishedGame = Game.builder().id(gameId).status(GameStatus.GAME_OVER).build();

                GameResponse response = new GameResponse();
                response.setId(gameId);
                response.setStatus(GameStatus.GAME_OVER);

                when(gameService.getFinishedGameById(eq(gameId))).thenReturn(finishedGame);
                when(gameMapper.toResponse(finishedGame)).thenReturn(response);

                mockMvc.perform(get("/games/finished/{gameId}", gameId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(gameId.toString()))
                                .andExpect(jsonPath("$.status").value("GAME_OVER"));

                verify(gameService).getFinishedGameById(gameId);
                verify(gameMapper).toResponse(finishedGame);
        }

        @Test
        void shouldReturnBadRequestWhenFinishedGameDoesNotExistForDetail() throws Exception {

                UUID gameId = UUID.randomUUID();

                when(gameService.getFinishedGameById(eq(gameId)))
                                .thenThrow(new IllegalArgumentException("Game is not finished"));

                mockMvc.perform(get("/games/finished/{gameId}", gameId))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                                .andExpect(jsonPath("$.message").value("Game is not finished"));

                verify(gameService).getFinishedGameById(gameId);
        }

    private record ApplyActionBody(UUID targetPlayerId) {
    }
}
