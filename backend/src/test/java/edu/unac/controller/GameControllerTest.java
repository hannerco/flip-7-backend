package edu.unac.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.unac.model.card.ActionCard;
import edu.unac.model.card.NumericCard;
import edu.unac.model.enums.CardType;
import edu.unac.model.enums.GameStatus;
import edu.unac.model.enums.PlayerStatus;
import edu.unac.model.game.Game;
import edu.unac.model.game.PendingAction;
import edu.unac.model.game.Player;
import edu.unac.model.game.Winner;
import edu.unac.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Transactional
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameRepository gameRepository;

    @BeforeEach
    void cleanDatabase() {
        gameRepository.deleteAll();
    }

    @Test
    void shouldCreateGameAndReturnGameId() throws Exception {

        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerNames\":[\"Alice\",\"Bob\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").isNotEmpty());
    }

    @Test
    void shouldStartRoundAndExposePlayersState() throws Exception {

        UUID gameId = createGame();
        seedDeck(gameId, List.of(
                new NumericCard(1),
                new NumericCard(2),
                new NumericCard(3),
                new NumericCard(4)
        ));

        mockMvc.perform(post("/games/{gameId}/rounds/start", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId.toString()))
                .andExpect(jsonPath("$.status").value("IN_ROUND"))
                .andExpect(jsonPath("$.players.length()").value(2))
                .andExpect(jsonPath("$.players[0].cards.length()").value(1))
                .andExpect(jsonPath("$.players[1].cards.length()").value(1));
    }

    @Test
    void shouldDrawCardAndAdvanceTurnUsingRealServices() throws Exception {

        UUID gameId = createGame();
        seedDeck(gameId, List.of(
                new NumericCard(1),
                new NumericCard(2),
                new NumericCard(5),
                new NumericCard(6)
        ));

        JsonNode startResponse = objectMapper.readTree(
                mockMvc.perform(post("/games/{gameId}/rounds/start", gameId))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        );

        UUID currentPlayerId = UUID.fromString(startResponse.get("currentPlayerId").asText());

        mockMvc.perform(post("/games/{gameId}/draw", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"" + currentPlayerId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPlayerId").isNotEmpty())
                .andExpect(jsonPath("$.players.length()").value(2));
    }

    @Test
    void shouldExposePendingActionAndResolveIt() throws Exception {

        UUID gameId = createGame();
        Game game = gameRepository.findById(gameId).orElseThrow();

        game.setDeck(new ArrayList<>(List.of(
                new NumericCard(1),
                new NumericCard(2),
                new ActionCard(CardType.FREEZE)
        )));
        gameRepository.saveAndFlush(game);

        JsonNode startResponse = objectMapper.readTree(
                mockMvc.perform(post("/games/{gameId}/rounds/start", gameId))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        );

        UUID sourcePlayerId = UUID.fromString(startResponse.get("currentPlayerId").asText());
        UUID targetPlayerId = UUID.fromString(startResponse.get("players").get(1).get("id").asText());

        JsonNode drawResponse = objectMapper.readTree(
                mockMvc.perform(post("/games/{gameId}/draw", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerId\":\"" + sourcePlayerId + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.pendingAction.type").value("FREEZE"))
                        .andExpect(jsonPath("$.pendingAction.targetOptions.length()").value(1))
                        .andReturn()
                        .getResponse()
                        .getContentAsString()
        );

        mockMvc.perform(post("/games/{gameId}/actions", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetPlayerId\":\"" + targetPlayerId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingAction").doesNotExist())
                .andExpect(jsonPath("$.players[1].status").value("STAYED"));

        org.junit.jupiter.api.Assertions.assertTrue(drawResponse.has("pendingAction"));
    }

    @Test
    void shouldListFinishedGamesAndReturnFinishedDetail() throws Exception {

        UUID winnerId = UUID.randomUUID();

        Game game = Game.builder()
                .status(GameStatus.GAME_OVER)
                .currentRound(5)
                .players(new ArrayList<>(List.of(
                        Player.builder().name("Alice").totalScore(205).status(PlayerStatus.STAYED).build(),
                        Player.builder().name("Bob").totalScore(180).status(PlayerStatus.BUSTED).build()
                )))
                .winner(new Winner(winnerId, "Alice", 205))
                .build();

        Game savedGame = gameRepository.saveAndFlush(game);
        UUID gameId = savedGame.getId();

        mockMvc.perform(get("/games/finished"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(gameId.toString()))
                .andExpect(jsonPath("$[0].winner.name").value("Alice"));

        mockMvc.perform(get("/games/finished/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId.toString()))
                .andExpect(jsonPath("$.status").value("GAME_OVER"));
    }

    @Test
    void shouldReturnBadRequestWhenGameIsNotFinishedForFinishedDetail() throws Exception {

        UUID gameId = createGame();

        mockMvc.perform(get("/games/finished/{gameId}", gameId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Game is not finished"));
    }

    private UUID createGame() throws Exception {
        String response = mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"playerNames\":[\"Alice\",\"Bob\"]}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("gameId").traverse(objectMapper).readValueAs(UUID.class);
    }

    private void seedDeck(UUID gameId, List<?> cards) {
        Game game = gameRepository.findById(gameId).orElseThrow();
        game.setDeck(new ArrayList<>());

        for (Object card : cards) {
            game.getDeck().add((edu.unac.model.card.Card) card);
        }

                gameRepository.saveAndFlush(game);
    }
}