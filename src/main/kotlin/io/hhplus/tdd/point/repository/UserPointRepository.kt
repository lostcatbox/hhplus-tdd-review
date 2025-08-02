package io.hhplus.tdd.point.repository

import io.hhplus.tdd.point.domain.UserPoint

interface UserPointRepository {
    // 특정 유저의 현재 포인트 조회
    fun getPoint(userId: Long): UserPoint

    // 유저 포인트 저장
    fun save(userPoint: UserPoint): UserPoint
}
