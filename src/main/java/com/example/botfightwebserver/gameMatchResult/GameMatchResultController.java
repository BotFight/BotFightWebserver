package com.example.botfightwebserver.gameMatchResult;

import com.example.botfightwebserver.User.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/game-match-result")
public class GameMatchResultController {

    private final GameMatchResultHandler gameMatchResultHandler;
    private final UserService userService;

    @PostMapping("/handle/results")
    public ResponseEntity<Void> handleMatchResults(@RequestBody GameMatchResult result) {
        if (!userService.hasAccess()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        gameMatchResultHandler.handleGameMatchResult(result);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/submit/results")
    public ResponseEntity<Void> submitResults(@RequestBody GameMatchResult result) {
        if (!userService.hasAccess()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        gameMatchResultHandler.submitGameMatchResults(result);
        return ResponseEntity.accepted().build();    }

    @PostMapping("/queue/remove_all")
    public ResponseEntity<List<GameMatchResult>> removeAllQueuedResults() {
        if (!userService.hasAccess()) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(gameMatchResultHandler.deleteQueuedMatches());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

}
