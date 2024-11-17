package com.example.botfightwebserver.rabbitMQ;

import com.example.botfightwebserver.gameMatch.GameMatchResult;
import com.example.botfightwebserver.gameMatch.GameMatchResultHandler;
import com.example.botfightwebserver.gameMatch.MATCH_STATUS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMQListenerTest {

    @Mock
    private GameMatchResultHandler gameMatchResultHandler;

    private RabbitMQListener rabbitMQListener;

    @BeforeEach
    void setUp() {
        rabbitMQListener = new RabbitMQListener(gameMatchResultHandler);
    }

    @Test
    void receiveGameMatchResults_ShouldProcessMessageSuccessfully() {
        GameMatchResult gameMatchResult = new GameMatchResult(1L, MATCH_STATUS.PLAYER_TWO_WIN, "SOME LOGS");

        rabbitMQListener.receiveGameMatchResults(gameMatchResult);

        verify(gameMatchResultHandler, times(1)).handleGameMatchResult(gameMatchResult);
    }
}