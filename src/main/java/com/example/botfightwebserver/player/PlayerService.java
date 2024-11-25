package com.example.botfightwebserver.player;

import com.example.botfightwebserver.submission.Submission;
import com.example.botfightwebserver.submission.SubmissionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final SubmissionService submissionService;

    public List<Player> getPlayers() {
        return playerRepository.findAll()
            .stream()
            .collect(Collectors.toUnmodifiableList());
    }

    public Player getPlayerReferenceById(Long id) {
        return playerRepository.getReferenceById(id);
    }

    public PlayerDTO getDTOById(Long id) {
        return PlayerDTO.fromEntity(playerRepository.getReferenceById(id));
    }


    public Player createPlayer(String name, String email) {
        if (playerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Player with email " + email + " already exists");
        }
        Player player = new Player();
        player.setName(name);
        player.setEmail(email);
        return playerRepository.save(player);
    }

    public void validatePlayers(Long player1Id, Long player2Id) {
        if (player1Id == null || player2Id == null) {
            throw new IllegalArgumentException("PlayerIds cannot be null");
        }
        if (!playerRepository.existsById(player1Id) || !playerRepository.existsById(player2Id)) {
            throw new IllegalArgumentException("One or both players do not exist");
        }
        if (player1Id.equals(player2Id)) {
            throw new IllegalArgumentException("Players must be different");
        }
    }

    public Player updatePlayerAfterLadderMatch(Player player, double glickoChange, double phiChange, double sigmaChange, boolean isWin, boolean isDraw) {
        if(isWin && isDraw) {
            throw new IllegalArgumentException("Result can't be a win and a draw");
        }
        double currentGlicko = player.getGlicko();
        double currentPhi = player.getPhi();
        double currentSigma = player.getSigma();
        double newGlicko = currentGlicko + glickoChange;
        double newPhi = currentPhi + phiChange;
        double newSigma = currentSigma + sigmaChange;
        player.setGlicko(newGlicko);
        player.setPhi(newPhi);
        player.setSigma(newSigma);
        player.setMatchesPlayed(player.getMatchesPlayed() + 1);
        if (!isWin && !isDraw) {
            player.setNumberLosses(player.getNumberLosses() + 1);
        } else if (isWin) {
            player.setNumberWins(player.getNumberWins() + 1);
        } else if (isDraw) {
            player.setNumberDraws(player.getNumberDraws() + 1);
        }
        return playerRepository.save(player);
    }

    public void setCurrentSubmission(Long playerId, Long submissionId) {
        if (!submissionService.isSubmissionValid(submissionId)) {
            throw new IllegalArgumentException("Submission is not valid");
        }
        Player player = playerRepository.findById(playerId).get();
        player.setCurrentSubmission(submissionService.getSubmissionReferenceById(submissionId));
    }

    public Optional<Submission> getCurrentSubmission(Long playerId) {
        Optional<Submission> submission = playerRepository.findById(playerId)
            .map(Player::getCurrentSubmission);
        return submission;
    }

    public boolean setCurrentSubmissionIfNone(Long playerId, Long submissionId) {
        Player player = playerRepository.findById(playerId).get();
        if (player.getCurrentSubmission() == null) {
            player.setCurrentSubmission(submissionService.getSubmissionReferenceById(submissionId));
            return true;
        }
        return false;
    }

    public Player[] pagination(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must be zero or greater");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }

        Pageable pageable = PageRequest.of(page, size);

        // Fetch the players for the specified page
        Page<Player> playerPage = playerRepository.findAll(pageable);

        // Return the players as an array
        return playerPage.getContent().toArray(new Player[0]);
    }

}


