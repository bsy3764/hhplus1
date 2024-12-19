package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService{

    private final PointHistoryTable historyTable;
    private final UserPointTable  userPointTable;

    // 사용자별 lock을 각각 생성(사용자가 다르면 동시에 진행되도 문제 없으므로)
    // 멀티스레드 환경에서도 안전한? ConcurrentHashMap 사용
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();

    private ReentrantLock getLock(long userId) {
        return userLocks.computeIfAbsent(userId, id -> new ReentrantLock(true));    // 공정모드로 사용자가 요청한 작업을 순서대로
    }

    @Override
    public UserPoint showPoint(long userId) {
        if (UserPoint.isMinusOrZero(userId)) {
            throw new IllegalArgumentException(MessageFormat.format("User Id는 0이거나 음수일 수 없습니다. UserID: {0}", userId));
        }
        ReentrantLock lock = getLock(userId);
        lock.lock();
        try {
            return userPointTable.selectById(userId);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new RuntimeException(MessageFormat.format("[showPoint] {0}", ex.getMessage()));
        } finally{
            lock.unlock();
        }
    }

    @Override
    public UserPoint chargePoint(long userId, long amount) {
        if (UserPoint.isMinusOrZero(userId)) {
            throw new IllegalArgumentException(MessageFormat.format("User Id는 0이거나 음수일 수 없습니다. UserID: {0}", userId));
        }

        ReentrantLock lock = getLock(userId);
        lock.lock();
        try {
            if (isAmountMinusOrZero(amount)) {
                throw new IllegalArgumentException(MessageFormat.format("충전 포인트는 0이거나 음수일 수 없습니다. 충전할 포인트: {0}", amount));
            }
            UserPoint beforePoint = showPoint(userId);
            long afterPoint = beforePoint.addUserPoint(amount);
            if (UserPoint.isMaxChargeOver(afterPoint)) {
                throw new IllegalArgumentException(MessageFormat.format("포인트는 최대 {0}까지만 적립 가능합니다.", UserPoint.MAX_SAVE_POINT));
            }
            UserPoint chargedPoint = userPointTable.insertOrUpdate(userId, afterPoint);
            historyTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

            return chargedPoint;
        } catch (Exception ex) {
            throw new RuntimeException(MessageFormat.format("[chargePoint] {0}", ex.getMessage()));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public UserPoint usePoint(long userId, long amount) {
        if (UserPoint.isMinusOrZero(userId)) {
            throw new IllegalArgumentException(MessageFormat.format("User Id는 0이거나 음수일 수 없습니다. UserID: {0}", userId));
        }

        ReentrantLock lock = getLock(userId);
        lock.lock();
        try {
            if (isAmountMinusOrZero(amount)) {
                throw new IllegalArgumentException(MessageFormat.format("사용 포인트는 0이거나 음수일 수 없습니다. 사용할 포인트: {0}", amount));
            }
            if (UserPoint.isMinUseLess(amount)){
                throw new IllegalArgumentException(MessageFormat.format("포인트는 최소 {0}포인트 부터 사용 가능합니다.", UserPoint.MIN_USE_POINT));
            }
            UserPoint beforePoint = showPoint(userId);
            if (beforePoint.isOverUse(amount)) {
                // 잔액 < 사용할 포인트
                throw new IllegalArgumentException(MessageFormat.format("사용할 포인트가 잔액 포인트보다 큽니다. 잔액 포인트: {0}", beforePoint.point()));
            }
            long afterPoint = beforePoint.subUserPoint(amount); // 사용 완료한 포인트
            UserPoint usedPoint = userPointTable.insertOrUpdate(userId, afterPoint);
            historyTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

            return usedPoint;
        } catch (Exception ex) {
            throw new RuntimeException(MessageFormat.format("[usePoint] {0}", ex.getMessage()));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<PointHistory> showPointHistory(long userId) {
        if (UserPoint.isMinusOrZero(userId)) {
            throw new IllegalArgumentException(MessageFormat.format("User Id는 0이거나 음수일 수 없습니다. UserID: {0}", userId));
        }

        ReentrantLock lock = getLock(userId);
        lock.lock();
        try {
            return historyTable.selectAllByUserId(userId);
        } catch (Exception ex) {
            throw new RuntimeException(MessageFormat.format("[showPointHistory] {0}", ex.getMessage()));
        } finally{
            lock.unlock();
        }
    }

    private boolean isAmountMinusOrZero(long amount) {
        return amount <= 0;
    }
}
