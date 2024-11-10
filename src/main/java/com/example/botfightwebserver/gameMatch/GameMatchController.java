package com.example.botfightwebserver.gameMatch;

import lombok.RequiredArgsConstructor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/game-match")
public class GameMatchController {

    private final GameMatchService gameMatchService;
    private final GameMatchResultHandler gameMatchResultHandler;

    @PostMapping("/submit/match")
    public ResponseEntity<GameMatchJob> submitMatch(@RequestBody MatchSubmissionRequest request) {
        GameMatchJob job = gameMatchService.submitGameMatch(
            request.getPlayer1Id(),
            request.getPlayer2Id(),
            request.getSubmission1Id(),
            request.getSubmission2Id(),
            request.getReason(),
            request.getMap()
        );
        return ResponseEntity.ok(job);
    }

    @PostMapping("/submit/results")
    public ResponseEntity<GameMatchResult> submitMatchResults(@RequestBody GameMatchResult result) {
        gameMatchResultHandler.handleGameMatchResult(result);
        return ResponseEntity.ok(result);
    }

}
