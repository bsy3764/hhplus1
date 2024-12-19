package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointController;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.PointServiceImpl;
import io.hhplus.tdd.point.UserPoint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class TddApplicationTest {

    @Autowired
    private PointController controller;
    @Autowired
    private PointServiceImpl pointService;
    @Autowired
    private UserPointTable userPointTable;
    @Autowired
    private PointHistoryTable pointHistoryTable;

    @Test
    void ControllerGetTest() {
        long userId = 1L;
        UserPoint userPoint = controller.point(userId);
        assertThat(userPoint.point()).isEqualTo(0);
    }

    @Test
    void ControllerChargeTest() {
        long userId = 2L;
        UserPoint charge = controller.charge(userId, 1000L);
        assertThat(charge).isEqualTo(userPointTable.selectById(userId));
    }

    @Test
    void ControllerUseTest() {
        long userId = 3L;
        controller.charge(userId, 1000L);
        UserPoint use = controller.use(userId, 500L);
        assertThat(use.point()).isEqualTo(500L);
    }

    @Test
    void PointControllerConcurrencyTest() throws InterruptedException {
        long userId = 5L;
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);// 다른 스래드에서 수행중인 작업을 완료될때까지 대기하도록 도와주는 CountDownLatch

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    //수행할 작업
                    controller.charge(userId, 10L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        assertThat(controller.point(userId).point()).isEqualTo(threadCount * 10L);
    }
}