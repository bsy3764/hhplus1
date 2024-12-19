package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceUnitTest {

    private UserPoint userPoint;

    // 주입받을 객체
    @InjectMocks
    private PointServiceImpl pointService;

    // 가짜 객체를 주입
    @Mock
    private PointHistoryTable historyTable;

    // 가짜 객체를 주입
    @Mock
    private UserPointTable userPointTable;

    @BeforeEach
    void createUser() {
        userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());
    }

    @Test
    @DisplayName("UserId는 0이거나 음수일 경우 에러발생")
    void checkUserId() {
        long userId = 0L;
        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, () -> pointService.showPoint(userId));
        assertThat(e.getMessage()).isEqualTo("User Id는 0이거나 음수일 수 없습니다. UserID: " + userId);
    }

    @Test
    @DisplayName("사용자ID를 받아 UserPoint 반환")
    void showPoint() {
        when(userPointTable.selectById(1L)).thenReturn(userPoint);

        UserPoint showPoint = pointService.showPoint(1L);

        assertThat(showPoint.point()).isEqualTo(1000L);
        verify(userPointTable).selectById(1L);
    }

    @Test
    @DisplayName("충전 테스트")
    void chargePoint() {
        when(userPointTable.selectById(1L)).thenReturn(userPoint);
        when(userPointTable.insertOrUpdate(1L, 1100L)).thenReturn(new UserPoint(1L, 1100L, System.currentTimeMillis()));

        UserPoint chargedPoint = pointService.chargePoint(1L, 100L);

        assertThat(chargedPoint.point()).isEqualTo(1100L);
        verify(userPointTable).selectById(1L);
        verify(userPointTable).insertOrUpdate(1L, 1100L);
    }

    @Test
    @DisplayName("포인트 사용")
    void usePoint() {
        when(userPointTable.selectById(1L)).thenReturn(userPoint);
        when(userPointTable.insertOrUpdate(1L, 500L)).thenReturn(new UserPoint(1L, 500L, System.currentTimeMillis()));

        UserPoint usedPoint = pointService.usePoint(1L, 500L);

        assertThat(usedPoint.point()).isEqualTo(500L);
        verify(userPointTable).selectById(1L);
        verify(userPointTable).insertOrUpdate(1L, 500L);
    }

    @Test
    @DisplayName("충전 내역 조회")
    void showPointHistory() {
        // Given: Mock 데이터 설정
        long userId = 1L;
        long chargeAmount = 100L;
        long updatedBalance = 1100L;
        long now = System.currentTimeMillis();

        UserPoint mockUserPoint = new UserPoint(userId, updatedBalance, now);
        PointHistory mockPointHistory = new PointHistory(1L, userId, chargeAmount, TransactionType.CHARGE, now);

        when(userPointTable.selectById(userId)).thenReturn(userPoint);
        when(userPointTable.insertOrUpdate(userId, updatedBalance)).thenReturn(mockUserPoint);
        when(historyTable.selectAllByUserId(userId)).thenReturn(List.of(mockPointHistory));

        // When: 메서드 호출
        pointService.chargePoint(userId, chargeAmount);
        List<PointHistory> histories = pointService.showPointHistory(userId);

        // Then: 결과 검증
        assertThat(histories).hasSize(1);
//        assertThat(histories.get(0).getAmount()).isEqualTo(chargeAmount);
//        assertThat(histories.get(0).getType()).isEqualTo(TransactionType.CHARGE);

        // Verify: Mock 호출 확인
        verify(historyTable).selectAllByUserId(userId);
    }
}