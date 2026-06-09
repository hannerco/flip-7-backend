package edu.unac.repository;

import edu.unac.model.game.Game;
import edu.unac.model.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID> {

	List<Game> findAllByStatus(GameStatus status);
}