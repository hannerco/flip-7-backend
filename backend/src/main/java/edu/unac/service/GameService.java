package edu.unac.service;

import edu.unac.model.game.Game;

import java.util.List;

public interface GameService {

    Game createGame(List<String> playerNames);

}