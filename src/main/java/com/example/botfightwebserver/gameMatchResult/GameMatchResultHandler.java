package com.example.botfightwebserver.gameMatchResult;

import com.example.botfightwebserver.elo.EloCalculator;
import com.example.botfightwebserver.elo.EloChanges;
import com.example.botfightwebserver.gameMatch.GameMatch;
import com.example.botfightwebserver.gameMatch.GameMatchJob;
import com.example.botfightwebserver.gameMatch.GameMatchService;
import com.example.botfightwebserver.gameMatch.MATCH_REASON;
import com.example.botfightwebserver.gameMatch.MATCH_STATUS;
import com.example.botfightwebserver.gameMatchLogs.GameMatchLogService;
import com.example.botfightwebserver.player.Player;
import com.example.botfightwebserver.player.PlayerService;
import com.example.botfightwebserver.rabbitMQ.RabbitMQService;
import com.example.botfightwebserver.submission.Submission;
import com.example.botfightwebserver.submission.SubmissionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
@Slf4j
public class GameMatchResultHandler {

    private final GameMatchService gameMatchService;
    private final PlayerService playerService;
    private final SubmissionService submissionService;
    private final RabbitMQService rabbitMQService;
    private final EloCalculator eloCalculator;
    private final GameMatchLogService gameMatchLogService;


    public void handleGameMatchResult(GameMatchResult result) {
        long gameMatchId = result.matchId();
        if (!gameMatchService.isGameMatchIdExist(gameMatchId)) {
            throw new IllegalArgumentException("Game match id " + gameMatchId + " does not exist");
        }
        if (!gameMatchService.isGameMatchWaiting(gameMatchId)) {
            throw new UnsupportedOperationException("Game match is already played {}" + result);
        }
        MATCH_STATUS status = result.status();
        GameMatch gameMatch = gameMatchService.getReferenceById(gameMatchId);
        Player player1 = gameMatch.getPlayerOne();
        Player player2 = gameMatch.getPlayerTwo();

        log.info("Processing match result for game {}: {} vs {}, status: {}",
            gameMatchId, player1.getName(), player2.getName(), status);

        EloChanges eloChanges = new EloChanges();
        if (gameMatch.getReason() == MATCH_REASON.LADDER) {
            eloChanges = eloCalculator.calculateElo(player1, player2, status);
            log.debug("Handling ladder match: player1 {}, player2 {}", player1.getId(), player2.getId());
            handleLadderResult(player1, player2, status, eloChanges);
            log.info("Ladder match handled");
        } else if (gameMatch.getReason() == MATCH_REASON.VALIDATION) {
            Submission submission = gameMatch.getSubmissionOne();
            log.info("Processing validation match for player {}", player1.getName());
            handleValidationResult(player1, submission);
            log.info("Validation match handled");
        }
        gameMatchService.setGameMatchStatus(gameMatchId, status);
        gameMatchLogService.createGameMatchLog(gameMatchId, result.matchLog(), eloChanges.getPlayer1Change(), eloChanges.getPlayer2Change());
    }

    private void handleLadderResult(Player player1, Player player2, MATCH_STATUS status, EloChanges eloChanges) {
        if (status == MATCH_STATUS.PLAYER_ONE_WIN) {
            playerService.updatePlayerAfterLadderMatch(player1, eloChanges.getPlayer1Change(), eloChanges.getPlayer1PhiChange(),eloChanges.getPlayer1SigmaChange(), true, false);
            playerService.updatePlayerAfterLadderMatch(player2, eloChanges.getPlayer2Change(), eloChanges.getPlayer2PhiChange(), eloChanges.getPlayer2SigmaChange(), false, false);
        } else if (status == MATCH_STATUS.PLAYER_TWO_WIN) {
            playerService.updatePlayerAfterLadderMatch(player1, eloChanges.getPlayer1Change(), eloChanges.getPlayer1PhiChange(),eloChanges.getPlayer1SigmaChange(), false, false);
            playerService.updatePlayerAfterLadderMatch(player2, eloChanges.getPlayer2Change(), eloChanges.getPlayer2PhiChange(), eloChanges.getPlayer2SigmaChange(), true, false);
        } else if (status == MATCH_STATUS.DRAW) {
            playerService.updatePlayerAfterLadderMatch(player1, eloChanges.getPlayer1Change(), eloChanges.getPlayer1PhiChange(),eloChanges.getPlayer1SigmaChange(), false, true);
            playerService.updatePlayerAfterLadderMatch(player2, eloChanges.getPlayer2Change(), eloChanges.getPlayer2PhiChange(), eloChanges.getPlayer2SigmaChange(), false, true);
        }
    }

    public void submitGameMatchResults(GameMatchResult result) {
        if (!gameMatchService.isGameMatchIdExist(result.matchId())) {
            throw new RuntimeException("Game match id " + result.matchId() + " does not exist");
        }
        rabbitMQService.enqueueGameMatchResult(result);
    }

    private  void handleValidationResult(Player player, Submission submission) {
        submissionService.validateSubmissionAfterMatch(submission.getId());
        if (playerService.getCurrentSubmission(player.getId()).isEmpty()) {
            playerService.setCurrentSubmission(player.getId(), submission.getId());
        }
    }

    public List<GameMatchResult> deleteQueuedMatches() {
        List<GameMatchResult> removedResults = rabbitMQService.deleteGameResultQueue();
        return removedResults;
    }
}
