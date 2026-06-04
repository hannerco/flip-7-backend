package edu.unac.service.impl;

import edu.unac.model.card.*;
import edu.unac.model.enums.ModifierType;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeckServiceImplTest {

    private final DeckServiceImpl deckService = new DeckServiceImpl();

    @Test
    void shouldBuildDeckWith94Cards() {

        List<Card> deck = deckService.buildDeck();

        assertEquals(94, deck.size());
    }

    @Test
    void shouldContainCorrectAmountOfNumericCards() {

        List<Card> deck = deckService.buildDeck();

        Map<Integer, Long> frequencies =
                deck.stream()
                        .filter(card -> card instanceof NumericCard)
                        .map(card -> ((NumericCard) card).getValue())
                        .collect(Collectors.groupingBy(
                                value -> value,
                                Collectors.counting()));

        assertEquals(1L, frequencies.get(0));
        assertEquals(1L, frequencies.get(1));
        assertEquals(2L, frequencies.get(2));
        assertEquals(3L, frequencies.get(3));
        assertEquals(4L, frequencies.get(4));
        assertEquals(5L, frequencies.get(5));
        assertEquals(6L, frequencies.get(6));
        assertEquals(7L, frequencies.get(7));
        assertEquals(8L, frequencies.get(8));
        assertEquals(9L, frequencies.get(9));
        assertEquals(10L, frequencies.get(10));
        assertEquals(11L, frequencies.get(11));
        assertEquals(12L, frequencies.get(12));
    }

    @Test
    void shouldContainThreeFreezeCards() {

        List<Card> deck = deckService.buildDeck();

        long freezeCards =
                deck.stream()
                        .filter(card -> card instanceof FreezeCard)
                        .count();

        assertEquals(3, freezeCards);
    }

    @Test
    void shouldContainThreeFlipThreeCards() {

        List<Card> deck = deckService.buildDeck();

        long cards =
                deck.stream()
                        .filter(card -> card instanceof FlipThreeCard)
                        .count();

        assertEquals(3, cards);
    }

    @Test
    void shouldContainThreeSecondChanceCards() {

        List<Card> deck = deckService.buildDeck();

        long cards =
                deck.stream()
                        .filter(card -> card instanceof SecondChanceCard)
                        .count();

        assertEquals(3, cards);
    }

    @Test
    void shouldContainAllModifierCards() {

        List<Card> deck = deckService.buildDeck();

        Map<ModifierType, Long> modifiers =
                deck.stream()
                        .filter(card -> card instanceof ModifierCard)
                        .map(card -> ((ModifierCard) card).getModifier())
                        .collect(Collectors.groupingBy(
                                modifier -> modifier,
                                Collectors.counting()));

        assertEquals(1L, modifiers.get(ModifierType.PLUS_2));
        assertEquals(1L, modifiers.get(ModifierType.PLUS_4));
        assertEquals(1L, modifiers.get(ModifierType.PLUS_6));
        assertEquals(1L, modifiers.get(ModifierType.PLUS_8));
        assertEquals(1L, modifiers.get(ModifierType.PLUS_10));
        assertEquals(1L, modifiers.get(ModifierType.TIMES_2));
    }
}