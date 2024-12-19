package io.hhplus.tdd.point;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class PointServiceIntegrateTest {

    private static final Logger log = LoggerFactory.getLogger(PointServiceIntegrateTest.class);
    @Autowired
    private PointService pointService;

    @Test
    void PointServiceConcurrencyTest() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);// 다른 스래드에서 수행중인 작업을 완료될때까지 대기하도록 도와주는 CountDownLatch

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    //수행할 작업
                    pointService.chargePoint(1L, 10L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        assertThat(pointService.showPoint(1L).point()).isEqualTo(threadCount * 10L);
    }
}
