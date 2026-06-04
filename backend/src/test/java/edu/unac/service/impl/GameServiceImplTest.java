package edu.unac.service.impl;

import edu.unac.model.enums.GameStatus;
import edu.unac.model.game.Game;
import edu.unac.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameServiceImpl gameService;

    @Test
    void shouldCreateGameWithTwoPlayers() {

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game game = gameService.createGame(
                List.of("Alice", "Bob")
        );

        assertNotNull(game);

        assertEquals(2, game.getPlayers().size());

        assertEquals(
                GameStatus.WAITING,
                game.getStatus()
        );

        verify(gameRepository, times(1))
                .save(any(Game.class));
    }

    @Test
    void shouldThrowExceptionWhenLessThanTwoPlayers() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gameService.createGame(
                                List.of("Alice")
                        )
                );

        assertEquals(
                "Need at least 2 players",
                exception.getMessage()
        );

        verify(gameRepository, never())
                .save(any());
    }

    @Test
    void shouldReturnGameWhenGameExists() {

        Game game = Game.builder().build();

        when(gameRepository.findById(any()))
                .thenReturn(java.util.Optional.of(game));

        Game result =
                gameService.getGameById(
                        java.util.UUID.randomUUID()
                );

        assertNotNull(result);

        verify(gameRepository, times(1))
                .findById(any());
    }

    @Test
    void shouldThrowExceptionWhenGameDoesNotExist() {

        when(gameRepository.findById(any()))
                .thenReturn(java.util.Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gameService.getGameById(
                                java.util.UUID.randomUUID()
                        )
                );

        assertEquals(
                "Game not found",
                exception.getMessage()
        );

        verify(gameRepository, times(1))
                .findById(any());
    }
}