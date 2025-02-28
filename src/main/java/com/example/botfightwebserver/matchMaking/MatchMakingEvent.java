package com.example.botfightwebserver.matchMaking;

import com.example.botfightwebserver.gameMatch.MATCH_STATUS;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
@Builder
public class MatchMakingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer numberTeams;

    private Integer numberMatches;

    private LocalDateTime creationDateTime;

    @Enumerated(EnumType.STRING)
    private MATCHMAKING_REASON reason;

}
