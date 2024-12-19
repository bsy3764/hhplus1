package io.hhplus.tdd.point;
;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserPointTest {

    private UserPoint userPoint;

    @BeforeEach
    void createUser() {
        userPoint = new UserPoint(1L, 1000L, System.currentTimeMillis());
    }

    @Test
    @DisplayName("포인트를 더하는 테스트")
    void addUserPoint() {
        long result = userPoint.addUserPoint(1000L);
        assertThat(result).isEqualTo(2000L);
    }

    @Test
    @DisplayName("포인트를 빼는 테스트")
    void subUserPoint() {
        long result = userPoint.subUserPoint(500L);
        assertThat(result).isEqualTo(500L);
    }

    @Test
    @DisplayName("최대 보유할 수 포인트와 같거나 작으면 false 반환")
    void isMaxChargeNotOver() {
        UserPoint userPoint = new UserPoint(1L, 9999L, System.currentTimeMillis());
        boolean notMaxOver = UserPoint.isMaxChargeOver(userPoint.point());
        assertThat(notMaxOver).isFalse();
    }

    @Test
    @DisplayName("최대 보유할 수 포인트를 넘으면 true 반환")
    void isMaxChargeOver() {
        UserPoint userPoint = new UserPoint(1L, 10000L, System.currentTimeMillis());
        boolean maxOver = UserPoint.isMaxChargeOver(userPoint.point());
        assertThat(maxOver).isTrue();
    }

    @Test
    @DisplayName("최소 사용 포인트보다 작으면 true 반환")
    void isMinUseLess() {
        boolean minUseLess = UserPoint.isMinUseLess(9L);
        assertThat(minUseLess).isTrue();
    }

    @Test
    @DisplayName("최소 사용 포인트보다 크거나 같으면 false 반환")
    void isMinUseNotLess() {
        boolean minUseNotLess = UserPoint.isMinUseLess(10L);
        assertThat(minUseNotLess).isFalse();
    }

    @Test
    @DisplayName("보유 포인트보다 사용하려는 포인트가 많으면 true 반환")
    void isOverUse() {
        boolean overUse = userPoint.isOverUse(1001L);
        assertThat(overUse).isTrue();
    }

    @Test
    @DisplayName("보유 포인트보다 사용하려는 포인트가 작거나 같으면 false 반환")
    void isNotOverUse() {
        boolean notOverUse = userPoint.isOverUse(1000L);
        assertThat(notOverUse).isFalse();
    }

}