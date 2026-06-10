package edu.unac.service.impl;

import edu.unac.model.card.ModifierCard;
import edu.unac.model.card.NumericCard;
import edu.unac.model.enums.ModifierType;
import edu.unac.model.game.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScoreServiceImplTest {

    private ScoreServiceImpl scoreService;

    @BeforeEach
    void setUp() {
        scoreService = new ScoreServiceImpl();
    }

    @Test
    void calculateNormalScore() {

        Player player = Player.builder().build();

        player.getHand().add(new NumericCard(1));
        player.getHand().add(new NumericCard(5));
        player.getHand().add(new NumericCard(8));

        int score = scoreService.calculateScore(player);

        assertEquals(14, score);
    }

    @Test
    void calculateScoreWithPlusModifier() {

        Player player = Player.builder().build();

        player.getHand().add(new NumericCard(1));
        player.getHand().add(new NumericCard(5));
        player.getHand().add(new NumericCard(8));

        player.getHand().add(
                new ModifierCard(ModifierType.PLUS_2)
        );

        int score = scoreService.calculateScore(player);

        assertEquals(16, score);
    }

    @Test
    void calculateScoreWithTimesTwo() {

        Player player = Player.builder().build();

        player.getHand().add(new NumericCard(1));
        player.getHand().add(new NumericCard(5));
        player.getHand().add(new NumericCard(8));

        player.getHand().add(
                new ModifierCard(ModifierType.TIMES_2)
        );

        int score = scoreService.calculateScore(player);

        assertEquals(28, score);
    }

    @Test
    void calculateScoreWithTimesTwoAndPlusModifier() {

        Player player = Player.builder().build();

        player.getHand().add(new NumericCard(1));
        player.getHand().add(new NumericCard(5));
        player.getHand().add(new NumericCard(8));

        player.getHand().add(
                new ModifierCard(ModifierType.TIMES_2)
        );

        player.getHand().add(
                new ModifierCard(ModifierType.PLUS_4)
        );

        int score = scoreService.calculateScore(player);

        assertEquals(32, score);
    }

    @Test
    void calculateFlip7Bonus() {

        Player player = Player.builder().build();

        player.getHand().add(new NumericCard(1));
        player.getHand().add(new NumericCard(2));
        player.getHand().add(new NumericCard(3));
        player.getHand().add(new NumericCard(4));
        player.getHand().add(new NumericCard(5));
        player.getHand().add(new NumericCard(6));
        player.getHand().add(new NumericCard(7));

        int score = scoreService.calculateScore(player);

        assertEquals(43, score);
    }

    @Test
    void hasFlip7ShouldReturnTrue() {

        Player player = Player.builder().build();

        player.getHand().add(new NumericCard(1));
        player.getHand().add(new NumericCard(2));
        player.getHand().add(new NumericCard(3));
        player.getHand().add(new NumericCard(4));
        player.getHand().add(new NumericCard(5));
        player.getHand().add(new NumericCard(6));
        player.getHand().add(new NumericCard(7));

        boolean result = scoreService.hasFlip7(player);

        assertTrue(result);
    }

    @Test
    void hasFlip7ShouldReturnFalseWhenRepeatedNumbersExist() {

        Player player = Player.builder().build();

        player.getHand().add(new NumericCard(1));
        player.getHand().add(new NumericCard(2));
        player.getHand().add(new NumericCard(3));
        player.getHand().add(new NumericCard(4));
        player.getHand().add(new NumericCard(5));
        player.getHand().add(new NumericCard(6));
        player.getHand().add(new NumericCard(6));

        boolean result = scoreService.hasFlip7(player);

        assertFalse(result);
    }

    @Test
    void hasFlip7ShouldReturnFalseWhenLessThanSevenDistinctNumbersExist() {

        Player player = Player.builder().build();

        player.getHand().add(new NumericCard(1));
        player.getHand().add(new NumericCard(2));
        player.getHand().add(new NumericCard(3));
        player.getHand().add(new NumericCard(4));
        player.getHand().add(new NumericCard(5));
        player.getHand().add(new NumericCard(6));

        boolean result = scoreService.hasFlip7(player);

        assertFalse(result);
    }


}