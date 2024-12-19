package io.hhplus.tdd.point;

/*
* UserPoint 정책
* 최대 보유 포인트 지정(9,999)
* 포인트 사용시 최저 사용치 지정(10)
* */

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    // 최대로 보유할 수 있는 포인트
    public static final long MAX_SAVE_POINT = 9999L;
    // 사용시 최소한의 포인트
    public static final long MIN_USE_POINT = 10L;

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public static boolean isMinusOrZero(long userId) {
        return userId <= 0;
    }

    /**
     * 주어진 포인트가 최대 보유 포인트를 초과하는지 확인
     * @param chargedPoint 확인할 포인트 값
     * @return 초과하면 true, 그렇지 않으면 false
     */
    public static boolean isMaxChargeOver(long chargedPoint){
         return chargedPoint > MAX_SAVE_POINT;
    }

    /**
     * 주어진 포인트가 최소 사용 포인트보다 작은지 확인
     * @param usingPoint 확인할 포인트 값
     * @return 작으면 true, 같거나 크면 false
     */
    public static boolean isMinUseLess(long usingPoint) {
        return usingPoint < MIN_USE_POINT;
    }

    public boolean isOverUse(long usingPoint) {
        return point < usingPoint;
    }

    /**
     * 포인트 충전 계산
     * @param chargePoint
     * @return
     */
    public long addUserPoint(long chargePoint){
        return point + chargePoint;
    }

    /**
     * 포인트 사용 계산
     * @param usePoint
     * @return
     */
    public long subUserPoint(long usePoint){
        return point - usePoint;
    }
}
