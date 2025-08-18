package io.hhplus.tdd.controller.point

import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.UserPoint

interface PointServiceInterface {
    // 포인트 충전
    fun charge(
        userId: Long,
        amount: Long,
    ): UserPoint

    // 포인트 사용
    fun use(
        userId: Long,
        amount: Long,
    ): UserPoint

    // 특정 유저의 현재 포인트 조회
    fun getPoint(userId: Long): UserPoint

    // 해당 유저의 트랜잭션이 없는 경우 빈 리스트를 반환
    fun getHistory(userId: Long): List<PointHistory>
}
