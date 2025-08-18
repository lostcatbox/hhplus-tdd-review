package io.hhplus.tdd.point.domain

import io.hhplus.tdd.point.domain.UserPoint
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.jvm.java

class UserPointTest {
    @Nested
    inner class UserPointChargeTest {
        @Test
        fun `UserPoint 충전은 0 이하 금액을 허용하지 않는다`() {
            val userPoint = UserPoint(id = 1, point = 1000, updateMillis = System.currentTimeMillis())

            assertThrows(IllegalArgumentException::class.java) {
                userPoint.charge(0)
            }
        }

        @Test
        fun `UserPoint 충전은 2,000,000 초과 금액을 허용하지 않는다`() {
            val userPoint = UserPoint(id = 1, point = 1000, updateMillis = System.currentTimeMillis())

            assertThrows(IllegalArgumentException::class.java) {
                userPoint.charge(2000001)
            }
        }
    }

    @Nested
    inner class UserPointUseTest {
        @Test
        fun `UserPoint 사용은 0 이하 금액을 허용하지 않는다`() {
            val userPoint = UserPoint(id = 1, point = 1000, updateMillis = System.currentTimeMillis())

            assertThrows(IllegalArgumentException::class.java) {
                userPoint.use(0)
            }
        }

        @Test
        fun `UserPoint 사용은 현재 포인트보다 큰 금액을 허용하지 않는다`() {
            val userPoint = UserPoint(id = 1, point = 1000, updateMillis = System.currentTimeMillis())

            assertThrows(IllegalArgumentException::class.java) {
                userPoint.use(1001)
            }
        }
    }
}
