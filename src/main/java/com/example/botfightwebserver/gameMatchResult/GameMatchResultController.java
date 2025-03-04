package com.example.botfightwebserver.gameMatchResult;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/game-match-result")
public class GameMatchResultController {

    private final GameMatchResultHandler gameMatchResultHandler;

    @PostMapping("/handle/results")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> handleMatchResults(@RequestBody GameMatchResult result) {
        gameMatchResultHandler.handleGameMatchResult(result);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/submit/results")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> submitResults(@RequestBody GameMatchResult result) {

        gameMatchResultHandler.submitGameMatchResults(result);
        return ResponseEntity.accepted().build();    }

    @PostMapping("/queue/remove_all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GameMatchResult>> removeAllQueuedResults() {

        return ResponseEntity.ok(gameMatchResultHandler.deleteQueuedMatches());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

}
