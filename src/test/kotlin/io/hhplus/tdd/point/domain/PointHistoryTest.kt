package io.hhplus.tdd.point.domain

import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.TransactionType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PointHistoryTest {
    @Test
    fun `포인트 충전에 대한 PointHistory 생성 시 amount가 0 이하인 경우 예외 발생`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                PointHistory(
                    id = 1,
                    userId = 1,
                    type = TransactionType.CHARGE,
                    amount = 0,
                    timeMillis = System.currentTimeMillis(),
                )
            }
        assertEquals("amount는 0보다 커야 합니다.", exception.message)
    }

    @Test
    fun `포인트 사용에 대한 PointHistory 생성 시 amount가 0 이하인 경우 예외 발생`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                PointHistory(
                    id = 1,
                    userId = 1,
                    type = TransactionType.USE,
                    amount = 0,
                    timeMillis = System.currentTimeMillis(),
                )
            }
        assertEquals("amount는 0보다 커야 합니다.", exception.message)
    }
}
