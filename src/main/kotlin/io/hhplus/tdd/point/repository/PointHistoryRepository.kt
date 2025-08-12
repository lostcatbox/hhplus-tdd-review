package io.hhplus.tdd.point.repository

import io.hhplus.tdd.point.domain.PointHistory

interface PointHistoryRepository {
    // 포인트 충전 기록 조회
    fun getHistory(userId: Long): List<PointHistory>

    // 포인트 충전 기록 저장
    fun save(history: PointHistory): PointHistory
}
