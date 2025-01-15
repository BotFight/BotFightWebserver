package com.example.botfightwebserver.matchMaking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchMakerController {

    private final MatchMaker matchMaker;

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.OK)
    public void generateMatches() {
        matchMaker.generateMatches();
    }
}