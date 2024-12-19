package io.hhplus.tdd.point;

import java.util.List;

public interface PointService {

    /**
     * [포인트 조회]
     * userId를 받아서 -> db조회 -> UserPoint 반환.
     * PointHistory에 insert 없음
     * @param userId
     * @return
     */
    UserPoint showPoint(long userId);

    /**
     * [포인트 충전]
     * userId와 충전할 값을 받아서 -> 포인트 db 업데이트 + PointHistory에 insert -> 충전한 현재 포인트 반환(UserPoint)
     * @param userId
     * @param amount
     * @return
     */
    UserPoint chargePoint(long userId, long amount);

    /**
     * [포인트 사용]
     * userId와 사용할 포인트를 받아서 -> 포인트 조회 -> 포인트 부족 여부 확인 -> 포인트 db에 잔액으로 변경 + PointHistory에 insert -> 사용하고 남은 잔액 포인트 반환(UserPoint)
     * @param userId
     * @param amount
     * @return
     */
    UserPoint usePoint(long userId, long amount);

    /**
     * userId를 받아서 -> history db조회 -> 모든 사용내역 출력(PointHistory)
     * @param userId
     * @return
     */
    List<PointHistory> showPointHistory(long userId);
}
