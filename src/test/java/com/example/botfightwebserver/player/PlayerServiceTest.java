package com.example.botfightwebserver.player;

import com.example.botfightwebserver.PersistentTestBase;
import com.example.botfightwebserver.glicko.GlickoCalculator;
import com.example.botfightwebserver.submission.Submission;
import com.example.botfightwebserver.submission.SubmissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DataJpaTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
class PlayerServiceTest extends PersistentTestBase {

    @Autowired
    private PlayerRepository playerRepository;

    @MockBean
    private SubmissionService submissionService;

    @MockBean
    private GlickoCalculator glickoCalculator;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerService(playerRepository, submissionService);
    }

    @Test
    void testGetPlayers() {
        Submission submission1 = persistAndReturnEntity(new Submission());
        Submission submission2 = persistAndReturnEntity(new Submission());

        Player player1 = Player.builder()
            .currentSubmission(submission1)
            .glicko(1200.0)
            .email("tkwok123@gmail.com")
            .name("Tyler")
            .matchesPlayed(10)
            .numberWins(5)
            .numberLosses(3)
            .numberDraws(2)
            .build();

        Player player2 = Player.builder()
            .currentSubmission(submission2)
            .glicko(1400.0)
            .email("bkwok123@gmail.com")
            .name("Ben")
            .matchesPlayed(5)
            .numberWins(3)
            .numberLosses(2)
            .numberDraws(0)
            .build();

        persistEntity(player1);
        persistEntity(player2);

        List<Player> players = playerService.getPlayers();

        Player player1Persisted = players.get(0);
        Player player2Persisted = players.get(1);

        assertEquals(submission1.getId(), player1Persisted.getCurrentSubmission().getId());
        assertEquals(1200.0, player1Persisted.getGlicko());
        assertEquals("Tyler", player1Persisted.getName());
        assertEquals("tkwok123@gmail.com", player1Persisted.getEmail());
        assertEquals(10, player1Persisted.getMatchesPlayed());
        assertEquals(3, player1Persisted.getNumberLosses());
        assertEquals(2, player1Persisted.getNumberDraws());
        assertEquals(5, player1Persisted.getNumberWins());

        assertEquals(submission2.getId(), player2Persisted.getCurrentSubmission().getId());
        assertEquals(1400.0, player2Persisted.getGlicko());
        assertEquals("Ben", player2Persisted.getName());
        assertEquals("bkwok123@gmail.com", player2Persisted.getEmail());
        assertEquals(5, player2Persisted.getMatchesPlayed());
        assertEquals(3, player2Persisted.getNumberWins());
        assertEquals(2, player2Persisted.getNumberLosses());
        assertEquals(0, player2Persisted.getNumberDraws());
    }

    @Test
    void testGetPlayers_none() {
        List<Player> players = playerService.getPlayers();
        assertEquals(0, players.size());
    }

    @Test
    void testGetPlayerReferenceById() {
        Player expectedPlayer = Player.builder()
            .glicko(1200.0)
            .email("test@example.com")
            .name("Test Player")
            .matchesPlayed(0)
            .numberWins(0)
            .numberLosses(0)
            .numberDraws(0)
            .build();

        Player persistedPlayer = persistAndReturnEntity(expectedPlayer);
        Player retrievedPlayer = playerService.getPlayerReferenceById(persistedPlayer.getId());

        assertEquals(persistedPlayer.getId(), retrievedPlayer.getId());
        assertEquals(expectedPlayer.getEmail(), retrievedPlayer.getEmail());
        assertEquals(expectedPlayer.getName(), retrievedPlayer.getName());
        assertEquals(expectedPlayer.getGlicko(), retrievedPlayer.getGlicko());
        assertEquals(expectedPlayer.getMatchesPlayed(), retrievedPlayer.getMatchesPlayed());
        assertEquals(expectedPlayer.getNumberWins(), retrievedPlayer.getNumberWins());
        assertEquals(expectedPlayer.getNumberLosses(), retrievedPlayer.getNumberLosses());
        assertEquals(expectedPlayer.getNumberDraws(), retrievedPlayer.getNumberDraws());
    }

    @Test
    void testGetDTOById() {
        Submission submission = persistAndReturnEntity(new Submission());

        Player expectedPlayer = Player.builder()
            .currentSubmission(submission)
            .glicko(1200.0)
            .email("test@example.com")
            .name("Test Player")
            .matchesPlayed(5)
            .numberWins(3)
            .numberLosses(1)
            .numberDraws(1)
            .build();

        Player persistedPlayer = persistAndReturnEntity(expectedPlayer);

        PlayerDTO playerDTO = playerService.getDTOById(persistedPlayer.getId());

        assertEquals(persistedPlayer.getId(), playerDTO.getId());
        assertEquals(expectedPlayer.getEmail(), playerDTO.getEmail());
        assertEquals(expectedPlayer.getName(), playerDTO.getName());
        assertEquals(expectedPlayer.getGlicko(), playerDTO.getGlicko());
        assertEquals(expectedPlayer.getMatchesPlayed(), playerDTO.getMatchesPlayed());
        assertEquals(expectedPlayer.getNumberWins(), playerDTO.getNumberWins());
        assertEquals(expectedPlayer.getNumberLosses(), playerDTO.getNumberLosses());
        assertEquals(expectedPlayer.getNumberDraws(), playerDTO.getNumberDraws());
        assertEquals(submission.getId(), playerDTO.getCurrentSubmissionDTO().id());
    }

    @Test
    void testGetDTOById_WithNullSubmission() {
        Player expectedPlayer = Player.builder()
            .currentSubmission(null)
            .glicko(1200.0)
            .email("test@example.com")
            .name("Test Player")
            .matchesPlayed(0)
            .numberWins(0)
            .numberLosses(0)
            .numberDraws(0)
            .build();

        Player persistedPlayer = persistAndReturnEntity(expectedPlayer);

        PlayerDTO playerDTO = playerService.getDTOById(persistedPlayer.getId());

        assertEquals(persistedPlayer.getId(), playerDTO.getId());
        assertEquals(expectedPlayer.getEmail(), playerDTO.getEmail());
        assertEquals(expectedPlayer.getName(), playerDTO.getName());
        assertEquals(expectedPlayer.getGlicko(), playerDTO.getGlicko());
        assertEquals(expectedPlayer.getMatchesPlayed(), playerDTO.getMatchesPlayed());
        assertEquals(expectedPlayer.getNumberWins(), playerDTO.getNumberWins());
        assertEquals(expectedPlayer.getNumberLosses(), playerDTO.getNumberLosses());
        assertEquals(expectedPlayer.getNumberDraws(), playerDTO.getNumberDraws());
        assertEquals(null, playerDTO.getCurrentSubmissionDTO());
    }

    @Test
    void testCreatePlayer_Success() {
        String name = "New Player";
        String email = "newplayer@example.com";

        Player persistedPlayer = playerService.createPlayer(name, email);

        assertEquals(name, persistedPlayer.getName());
        assertEquals(email, persistedPlayer.getEmail());
        assertEquals(0, persistedPlayer.getMatchesPlayed());
        assertEquals(0, persistedPlayer.getNumberWins());
        assertEquals(0, persistedPlayer.getNumberLosses());
        assertEquals(0, persistedPlayer.getNumberDraws());
    }

    @Test
    void testCreatePlayer_DuplicateEmail() {
        String name1 = "First Player";
        String email = "same@example.com";
        playerService.createPlayer(name1, email);

        String name2 = "Second Player";
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> playerService.createPlayer(name2, email)
        );

        assertEquals("Player with email " + email + " already exists", exception.getMessage());
        assertEquals(1, playerRepository.count());
    }


    @Test
    void testValidatePlayers_Success() {
        Player player1 = persistAndReturnEntity(Player.builder()
            .name("Player 1")
            .email("player1@example.com")
            .build());

        Player player2 = persistAndReturnEntity(Player.builder()
            .name("Player 2")
            .email("player2@example.com")
            .build());

        assertDoesNotThrow(() ->
            playerService.validatePlayers(player1.getId(), player2.getId())
        );
    }

    @Test
    void testValidatePlayers_SamePlayer() {
        Player player = persistAndReturnEntity(Player.builder()
            .name("Player 1")
            .email("player1@example.com")
            .build());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> playerService.validatePlayers(player.getId(), player.getId())
        );

        assertEquals("Players must be different", exception.getMessage());
    }

    @Test
    void testValidatePlayers_PlayerDoesNotExist() {
        Player player = persistAndReturnEntity(Player.builder()
            .name("Player 1")
            .email("player1@example.com")
            .build());

        Long nonExistentId = 99999L;

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> playerService.validatePlayers(nonExistentId, player.getId())
        );

        assertEquals("One or both players do not exist", exception.getMessage());
    }


    @Test
    void testValidatePlayers_NullIds() {
        assertThrows(IllegalArgumentException.class,
            () -> playerService.validatePlayers(null, 1L));

        assertThrows(IllegalArgumentException.class,
            () -> playerService.validatePlayers(1L, null));

        assertThrows(IllegalArgumentException.class,
            () -> playerService.validatePlayers(null, null));
    }

    @Test
    void testUpdatePlayerAfterLadderMatch_Win() {
        Player initialPlayer = persistAndReturnEntity(Player.builder()
            .name("Test Player")
            .email("test@example.com")
            .glicko(1200.0)
            .matchesPlayed(5)
            .numberWins(2)
            .numberLosses(2)
            .numberDraws(1)
            .build());

        double glickoChange = 15.0;

        Player persistedPlayer = playerService.updatePlayerAfterLadderMatch(initialPlayer, glickoChange,0.0,0.0, true, false);

        assertEquals(1215.0, persistedPlayer.getGlicko());
        assertEquals(6, persistedPlayer.getMatchesPlayed());
        assertEquals(3, persistedPlayer.getNumberWins());
        assertEquals(2, persistedPlayer.getNumberLosses());
        assertEquals(1, persistedPlayer.getNumberDraws());
    }

    @Test
    void testUpdatePlayerAfterLadderMatch_Loss() {
        Player initialPlayer = persistAndReturnEntity(Player.builder()
            .name("Test Player")
            .email("test@example.com")
            .glicko(1200.0)
            .matchesPlayed(5)
            .numberWins(2)
            .numberLosses(2)
            .numberDraws(1)
            .build());

        double glickoChange = -15.0;

        Player persistedPlayer = playerService.updatePlayerAfterLadderMatch(initialPlayer, glickoChange,0.0,0.0, false, false);

        assertEquals(1185.0, persistedPlayer.getGlicko());
        assertEquals(6, persistedPlayer.getMatchesPlayed());
        assertEquals(2, persistedPlayer.getNumberWins());
        assertEquals(3, persistedPlayer.getNumberLosses());
        assertEquals(1, persistedPlayer.getNumberDraws());
    }

    @Test
    void testUpdatePlayerAfterLadderMatch_Draw() {
        Player initialPlayer = persistAndReturnEntity(Player.builder()
            .name("Test Player")
            .email("test@example.com")
            .glicko(1200.0)
            .matchesPlayed(5)
            .numberWins(2)
            .numberLosses(2)
            .numberDraws(1)
            .build());

        double glickoChange = 0.0;

        Player persistedPlayer = playerService.updatePlayerAfterLadderMatch(initialPlayer, glickoChange,0.0,0.0, false, true);

        assertEquals(1200.0, persistedPlayer.getGlicko());
        assertEquals(6, persistedPlayer.getMatchesPlayed());
        assertEquals(2, persistedPlayer.getNumberWins());
        assertEquals(2, persistedPlayer.getNumberLosses());
        assertEquals(2, persistedPlayer.getNumberDraws());
    }

    @Test
    void testUpdatePlayerAfterLadderMatch_InvalidWinAndDraw() {
        Player initialPlayer = persistAndReturnEntity(Player.builder()
            .name("Test Player")
            .email("test@example.com")
            .glicko(1200.0)
            .matchesPlayed(5)
            .numberWins(2)
            .numberLosses(2)
            .numberDraws(1)
            .build());


        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> playerService.updatePlayerAfterLadderMatch(initialPlayer, 15.0,0.0,0.0, true, true)
        );

        assertEquals("Result can't be a win and a draw", exception.getMessage());

        Player unchangedPlayer = playerRepository.findById(initialPlayer.getId()).get();
        assertEquals(1200.0, unchangedPlayer.getGlicko());
        assertEquals(5, unchangedPlayer.getMatchesPlayed());
        assertEquals(2, unchangedPlayer.getNumberWins());
        assertEquals(2, unchangedPlayer.getNumberLosses());
        assertEquals(1, unchangedPlayer.getNumberDraws());
    }

    @Test
    void testSetCurrentSubmission_Success() {
        Player player = persistAndReturnEntity(Player.builder()
            .name("Test Player")
            .email("test@example.com")
            .build());

        Long submissionId = 1L;
        Submission mockSubmission = new Submission();
        mockSubmission.setId(submissionId);

        when(submissionService.isSubmissionValid(submissionId)).thenReturn(true);
        when(submissionService.getSubmissionReferenceById(submissionId)).thenReturn(mockSubmission);

        assertDoesNotThrow(() ->
            playerService.setCurrentSubmission(player.getId(), submissionId)
        );

        Player updatedPlayer = playerRepository.findById(player.getId()).get();
        assertEquals(submissionId, updatedPlayer.getCurrentSubmission().getId());

        verify(submissionService).isSubmissionValid(submissionId);
        verify(submissionService).getSubmissionReferenceById(submissionId);
    }

    @Test
    void testSetCurrentSubmission_InvalidSubmission() {
        Player player = persistAndReturnEntity(Player.builder()
            .name("Test Player")
            .email("test@example.com")
            .build());

        Long submissionId = 1L;

        when(submissionService.isSubmissionValid(submissionId)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> playerService.setCurrentSubmission(player.getId(), submissionId)
        );

        assertEquals("Submission is not valid", exception.getMessage());

        Player unchangedPlayer = playerRepository.findById(player.getId()).get();
        assertNull(unchangedPlayer.getCurrentSubmission());

        verify(submissionService).isSubmissionValid(submissionId);
        verify(submissionService, never()).getSubmissionReferenceById(any());
    }

    @Test
    void testGetCurrentSubmission_WithSubmission() {
        Submission submission = persistAndReturnEntity(new Submission());

        Player player = persistAndReturnEntity(Player.builder()
            .name("Test Player")
            .email("test@example.com")
            .currentSubmission(submission)
            .build());

        Optional<Submission> result = playerService.getCurrentSubmission(player.getId());

        assertTrue(result.isPresent());
        assertEquals(submission.getId(), result.get().getId());
    }

    @Test
    void testGetCurrentSubmission_NoSubmission() {
        Player player = persistAndReturnEntity(Player.builder()
            .name("Test Player")
            .email("test@example.com")
            .currentSubmission(null)
            .build());

        Optional<Submission> result = playerService.getCurrentSubmission(player.getId());

        assertFalse(result.isPresent());
    }
    @Test
    void testPagination_ValidPageAndSize() {
        // Setup data: Create 10 players
        List<Player> players = IntStream.range(1, 11)
                .mapToObj(i -> persistAndReturnEntity(
                        Player.builder()
                                .name("Player " + i)
                                .email("player" + i + "@example.com")
                                .build()))
                .toList();

        // Fetch page 0 with a size of 5
        Player[] paginatedPlayers = playerService.pagination(0, 5);

        assertEquals(5, paginatedPlayers.length);
        assertEquals(players.get(0).getId(), paginatedPlayers[0].getId()); // First player on page 0
        assertEquals(players.get(4).getId(), paginatedPlayers[4].getId()); // Last player on page 0
    }

    @Test
    void testPagination_PageSizeLargerThanTotalPlayers() {
        // Setup data: Create 5 players
        List<Player> players = IntStream.range(1, 6)
                .mapToObj(i -> persistAndReturnEntity(
                        Player.builder()
                                .name("Player " + i)
                                .email("player" + i + "@example.com")
                                .build()))
                .toList();

        // Fetch page 0 with a size of 10
        Player[] paginatedPlayers = playerService.pagination(0, 10);

        assertEquals(players.size(), paginatedPlayers.length);
        assertEquals(players.get(0).getId(), paginatedPlayers[0].getId()); // First player
        assertEquals(players.get(4).getId(), paginatedPlayers[4].getId()); // Last player
    }

    @Test
    void testPagination_NoPlayers() {
        // Fetch page 0 with a size of 5 when there are no players
        Player[] paginatedPlayers = playerService.pagination(0, 5);

        assertEquals(0, paginatedPlayers.length);
    }

    @Test
    void testPagination_InvalidPageOrSize() {
        // Test invalid page size
        IllegalArgumentException exceptionForInvalidSize = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.pagination(0, 0)
        );
        assertEquals("Page size must be greater than 0", exceptionForInvalidSize.getMessage());

        // Test invalid page number
        IllegalArgumentException exceptionForInvalidPage = assertThrows(
                IllegalArgumentException.class,
                () -> playerService.pagination(-1, 5)
        );
        assertEquals("Page index must be zero or greater", exceptionForInvalidPage.getMessage());
    }

    @Test
    void testPagination_SpecificRange() {
        // Setup data: Create 100 players
        List<Player> players = IntStream.range(1, 101)
                .mapToObj(i -> persistAndReturnEntity(
                        Player.builder()
                                .name("Player " + i)
                                .email("player" + i + "@example.com")
                                .build()))
                .toList();

        // Fetch players 50–59 (page index 5, size 10)
        Player[] paginatedPlayers = playerService.pagination(5, 10);

        assertEquals(10, paginatedPlayers.length);
        assertEquals(players.get(50).getId(), paginatedPlayers[0].getId()); // First player on page 5
        assertEquals(players.get(59).getId(), paginatedPlayers[9].getId()); // Last player on page 5
    }


}