package com.example.botfightwebserver.player;
;
import com.example.botfightwebserver.permissions.PermissionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PermissionsService permissionsService;

    public List<Player> getPlayers() {
        return playerRepository.findAll()
            .stream()
            .toList();
    }

    public Player createPlayer(String name, String email, UUID authId, Long teamId) {
        if (playerRepository.existsByEmail(email)) {throw new IllegalArgumentException("Player with email " + email + " already exists");
        }
        Player player = new Player();
        player.setName(name);
        player.setEmail(email);
        player.setTeamId(teamId);
        player.setAuthId(authId);
        return playerRepository.save(player);
    }

    public void setName(Long playerId, String name) {
        permissionsService.validateAllowUpdateProfile();
        if (!playerRepository.existsById(playerId)) {
            throw new IllegalArgumentException("Player with id " + playerId + " does not exist");
        }
        Player player = playerRepository.findById(playerId).get();
        player.setName(name);
        playerRepository.save(player);
    }

    public Player setPlayerTeam(UUID playerId, Long teamId) {
        permissionsService.validateAllowJoinTeam();
        if (!playerRepository.existsByAuthId(playerId)) {
            throw new IllegalArgumentException("Player with id " + playerId + " does not exist");
        }
        Player player = playerRepository.findByAuthId(playerId).orElse(null);
        player.setTeamId(teamId);
        player.setHasTeam(true);
        return playerRepository.save(player);
    }

    public Long leaveTeam(UUID playerId) {
        if (!playerRepository.existsByAuthId(playerId)) {
            throw new IllegalArgumentException("Player with id " + playerId + " does not exist");
        }
        Player player = playerRepository.findByAuthId(playerId).orElse(null);
        Long oldTeamId = player.getTeamId();
        player.setHasTeam(false);
        player.setTeamId(null);
        playerRepository.save(player);
        return oldTeamId;
    }

    public List<Player> getPlayersByTeam(Long teamId) {
        return playerRepository.findByTeamId(teamId);
    }

    public Player getPlayer(Long playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException("Player id cannot be null");
        }
        return playerRepository.findById(playerId).orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }

    public Player getPlayer(UUID authId) {
        if (authId == null) {
            throw new IllegalArgumentException("Auth id cannot be null");
        }
        return playerRepository.findByAuthId(authId).orElseThrow(() -> new IllegalArgumentException("Player not found"));
    }

    public boolean isUsernameExist(String username) {
        System.out.println(username);
        return playerRepository.existsByName(username);
    }

    public boolean isEmailExist(String email) {
        return playerRepository.existsByEmail(email);
    }

    public Long getTeamFromUUID(UUID uuid) {
        Player player = getPlayer(uuid);
        if (!player.isHasTeam()) {
            throw new IllegalArgumentException("Player with UUID " + uuid + " has no team");
        }
        return player.getTeamId();
    }

    public Long getNumberPlayers() {
        return playerRepository.count();
    }


}
