package com.example.botfightwebserver.gameMatchLogs;

import com.example.botfightwebserver.gameMatch.GameMatch;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.*;

@Entity
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameMatchLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long matchId;

    @Column(columnDefinition = "TEXT")
    private String matchLog;

    private Double team1GlickoChange;

    private Double team2GlickoChange;
}
