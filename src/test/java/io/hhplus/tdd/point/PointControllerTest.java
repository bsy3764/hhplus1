package io.hhplus.tdd.point;

import io.hhplus.tdd.point.PointController;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;

    @BeforeEach
    void setup() {
        // 필요한 초기 설정이 있으면 여기에 추가
    }

    @Test
    void shouldReturnUserPoint_whenShowPointIsCalled() throws Exception {
        // Given
        when(pointService.showPoint(1L)).thenReturn(new UserPoint(1L, 1000L, System.currentTimeMillis()));

        // When & Then
        mockMvc.perform(get("/point/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.point").value(1000L))
                .andDo(print());
    }

    @Test
    void shouldChargeUserPointCorrectly() {
        // Given
        when(pointService.charge(1L, 1000L)).thenReturn(new UserPoint(1L, 1000L, System.currentTimeMillis()));

        // When
        UserPoint userPoint = pointService.charge(1L, 1000L);

        // Then
        assertThat(userPoint.point()).isEqualTo(1000L);
    }

    @Test
    void shouldHandleConcurrencyInPointCharging() throws InterruptedException {
        // Given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        when(pointService.charge(Mockito.anyLong(), Mockito.anyLong()))
                .thenAnswer(invocation -> new UserPoint(1L, 10L * threadCount, System.currentTimeMillis()));

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.charge(1L, 10L);
                } finally {
                    latch.countDown();
                }
            });
        }

        // When
        latch.await();

        // Then
        UserPoint userPoint = pointService.showPoint(1L);
        assertThat(userPoint.point()).isEqualTo(threadCount * 10L);
    }
}
