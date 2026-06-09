package edu.unac.service.impl;

import edu.unac.model.card.*;
import edu.unac.model.enums.CardType;
import edu.unac.model.enums.ModifierType;
import edu.unac.service.DeckService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DeckServiceImpl implements DeckService {

    @Override
    public List<Card> buildDeck() {

        List<Card> deck = new ArrayList<>();

        addNumericCards(deck);

        addActionCards(deck);

        addModifierCards(deck);

        Collections.shuffle(deck);

        return deck;
    }

    private void addNumericCards(List<Card> deck) {

        deck.add(new NumericCard(0));
        deck.add(new NumericCard(1));

        for (int value = 2; value <= 12; value++) {

            for (int count = 0; count < value; count++) {

                deck.add(new NumericCard(value));
            }
        }
    }

    private void addActionCards(List<Card> deck) {

        for (int i = 0; i < 3; i++) {
            deck.add(new ActionCard(CardType.FREEZE));
        }

        for (int i = 0; i < 3; i++) {
            deck.add(new ActionCard(CardType.FLIP_THREE));
        }

        for (int i = 0; i < 3; i++) {
            deck.add(new ActionCard(CardType.SECOND_CHANCE));
        }
    }

    private void addModifierCards(List<Card> deck) {

        deck.add(new ModifierCard(ModifierType.PLUS_2));
        deck.add(new ModifierCard(ModifierType.PLUS_4));
        deck.add(new ModifierCard(ModifierType.PLUS_6));
        deck.add(new ModifierCard(ModifierType.PLUS_8));
        deck.add(new ModifierCard(ModifierType.PLUS_10));
        deck.add(new ModifierCard(ModifierType.TIMES_2));
    }


}