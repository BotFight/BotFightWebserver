package com.example.botfightwebserver.submission;

import com.example.botfightwebserver.gameMatch.GameMatch;
import com.example.botfightwebserver.gameMatch.GameMatchJob;
import com.example.botfightwebserver.gameMatch.GameMatchService;
import com.example.botfightwebserver.gameMatch.MATCH_REASON;
import com.example.botfightwebserver.permissions.PermissionsService;
import com.example.botfightwebserver.player.Player;
import com.example.botfightwebserver.player.PlayerService;
import com.example.botfightwebserver.rabbitMQ.RabbitMQService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/submission")
public class SubmissionController {

    private final SubmissionService submissionService;
    private final GameMatchService gameMatchService;
    private final RabbitMQService rabbitMQService;
    private final PlayerService playerService;
    private final PermissionsService permissionsService;

    @PostMapping(consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SubmissionDTO> uploadSubmission(
        @RequestParam("file") MultipartFile file, @RequestParam(defaultValue = "false") Boolean isAutoSet) {


        permissionsService.validateAllowNewSubmission();

        String authId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Player player = playerService.getPlayer(UUID.fromString(authId));
        Long teamId = player.getTeamId();
        SubmissionDTO submissionDTO = SubmissionDTO.fromEntity(submissionService.createSubmission(teamId, file, isAutoSet));
        GameMatch valMatch = gameMatchService.createMatch(teamId, teamId, submissionDTO.getId(), submissionDTO.getId(),
            MATCH_REASON.VALIDATION,
            "empty");
        rabbitMQService.enqueueGameMatchJob(GameMatchJob.fromEntity(valMatch));
        return ResponseEntity.ok(submissionDTO);
        }

    @GetMapping("/team")
    public ResponseEntity<List<SubmissionDTO>> getTeamSubmissions() {
        String authId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Player player = playerService.getPlayer(UUID.fromString(authId));
        Long teamId = player.getTeamId();
        return ResponseEntity.ok(submissionService.getTeamSubmissions(teamId));
    }

    @DeleteMapping("")
    public ResponseEntity<SubmissionDTO> deleteSubmission(@RequestParam Long submissionId) {
//        permissionsService.validateAllowDeleteSubmission();

        String authId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Player player = playerService.getPlayer(UUID.fromString(authId));
        Long teamId = player.getTeamId();

        Submission deleted = submissionService.deleteSubmission(submissionId, teamId);

        return ResponseEntity.ok(SubmissionDTO.fromEntity(deleted));
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleException(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
