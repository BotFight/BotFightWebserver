package com.example.botfightwebserver.searchEngine;

import com.example.botfightwebserver.gameMatch.GameMatchDTO;
import com.example.botfightwebserver.gameMatch.MATCH_REASON;
import com.example.botfightwebserver.gameMatch.MATCH_STATUS;
import com.example.botfightwebserver.team.TeamDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/search")
public class SearchEngineController {

    private final SearchEngineService searchEngineService;

    @GetMapping("/team")
    public ResponseEntity<Page<TeamDTO>> searchTeam(@RequestParam String searchParam,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TeamDTO> teamSearchResult = searchEngineService.searchTeamByNameFuzzy(searchParam, pageable)
            .map(TeamDTO::fromEntity);
        return ResponseEntity.ok(teamSearchResult);
    }

    @GetMapping("/match")
    public ResponseEntity<Page<GameMatchDTO>> searchGame(
        @RequestParam(required = false) String teamSearchparam,
        @RequestParam(required = false) String requiredTeamName,
        @RequestParam(required = false) MATCH_STATUS matchStatus,
        @RequestParam(required = false) MATCH_REASON matchReason,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GameMatchDTO> gameSearchResult = searchEngineService.searchGame(teamSearchparam, requiredTeamName, matchStatus, matchReason, pageable);
        return ResponseEntity.ok(gameSearchResult);
    }
}
