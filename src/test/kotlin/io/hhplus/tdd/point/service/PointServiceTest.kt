package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.TransactionType
import io.hhplus.tdd.point.domain.UserPoint
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.UserPointRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("PointService 단위 테스트")
class PointServiceTest {
    private val userPointRepository: UserPointRepository = mockk()
    private val pointHistoryRepository: PointHistoryRepository = mockk()
    private val pointService = PointService(userPointRepository, pointHistoryRepository)

    @BeforeEach
    fun setUp() {
        // 각 테스트마다 Mock 초기화
        io.mockk.clearMocks(userPointRepository, pointHistoryRepository)
    }

    @Nested
    @DisplayName("포인트 충전 테스트")
    inner class ChargeTest {
        @Test
        @DisplayName("정상적으로 포인트를 충전할 수 있다")
        fun `정상적으로 포인트를 충전할 수 있다`() {
            // Given
            val userId = 1L
            val initialAmount = 1000L
            val chargeAmount = 500L
            val expectedFinalAmount = initialAmount + chargeAmount

            val existingUserPoint = UserPoint(userId, initialAmount, System.currentTimeMillis())
            val chargedUserPoint = UserPoint(userId, expectedFinalAmount, System.currentTimeMillis())
            val savedHistory = PointHistory(1L, userId, TransactionType.CHARGE, chargeAmount, System.currentTimeMillis())

            // Mock 설정 (stub setup)
            every { userPointRepository.getPoint(userId) } returns existingUserPoint
            every { userPointRepository.save(any()) } returns chargedUserPoint
            every { pointHistoryRepository.save(any()) } returns savedHistory

            // When
            val result = pointService.charge(userId, chargeAmount)

            // Then
            assertThat(result.point).isEqualTo(expectedFinalAmount)
            assertThat(result.id).isEqualTo(userId)

            // Mock 호출 검증 (mock verification)
            verify(exactly = 1) { userPointRepository.save(any()) }
            verify(exactly = 1) { pointHistoryRepository.save(any()) }
        }

        @Test
        @DisplayName("충전할 때 히스토리가 올바르게 저장된다")
        fun `충전할 때 히스토리가 올바르게 저장된다`() {
            // Given
            val userId = 1L
            val chargeAmount = 500L
            val existingUserPoint = UserPoint(userId, 1000L, System.currentTimeMillis())
            val chargedUserPoint = UserPoint(userId, 1500L, System.currentTimeMillis())

            val savedHistory = PointHistory(1L, userId, TransactionType.CHARGE, chargeAmount, System.currentTimeMillis())

            every { userPointRepository.getPoint(userId) } returns existingUserPoint
            every { userPointRepository.save(any()) } returns chargedUserPoint
            every { pointHistoryRepository.save(any()) } returns savedHistory

            // When
            pointService.charge(userId, chargeAmount)

            // Then
            verify(exactly = 1) { pointHistoryRepository.save(any()) }
        }

        @Test
        @DisplayName("충전 금액이 0 이하일 때 예외가 발생한다")
        fun `충전 금액이 0 이하일 때 예외가 발생한다`() {
            // Given
            val userId = 1L
            val invalidAmount = 0L
            val existingUserPoint = UserPoint(userId, 1000L, System.currentTimeMillis())

            every { userPointRepository.getPoint(userId) } returns existingUserPoint

            // When & Then
            assertThatThrownBy {
                pointService.charge(userId, invalidAmount)
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("충전 금액은 0보다 커야 합니다.")

            // Repository 호출이 발생하지 않아야 함
            verify(exactly = 1) { userPointRepository.getPoint(userId) }
            verify(exactly = 0) { userPointRepository.save(any()) }
            verify(exactly = 0) { pointHistoryRepository.save(any()) }
        }

        @Test
        @DisplayName("충전 금액이 2,000,000을 초과할 때 예외가 발생한다")
        fun `충전 금액이 2,000,000을 초과할 때 예외가 발생한다`() {
            // Given
            val userId = 1L
            val invalidAmount = 2000001L
            val existingUserPoint = UserPoint(userId, 1000L, System.currentTimeMillis())

            every { userPointRepository.getPoint(userId) } returns existingUserPoint

            // When & Then
            assertThatThrownBy {
                pointService.charge(userId, invalidAmount)
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("충전 금액은 2,000,000을 초과할 수 없습니다.")

            verify(exactly = 1) { userPointRepository.getPoint(userId) }
            verify(exactly = 0) { userPointRepository.save(any()) }
            verify(exactly = 0) { pointHistoryRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("포인트 사용 테스트")
    inner class UseTest {
        @Test
        @DisplayName("정상적으로 포인트를 사용할 수 있다")
        fun `초기 1000 포인트에서 300 포인트를 사용할 수 있다`() {
            // Given
            val userId = 1L
            val initialAmount = 1000L
            val useAmount = 300L
            val expectedFinalAmount = initialAmount - useAmount

            val existingUserPoint = UserPoint(userId, initialAmount, System.currentTimeMillis())
            val usedUserPoint = UserPoint(userId, expectedFinalAmount, System.currentTimeMillis())
            val savedHistory = PointHistory(1L, userId, TransactionType.USE, useAmount, System.currentTimeMillis())

            every { userPointRepository.getPoint(userId) } returns existingUserPoint
            every { userPointRepository.save(any()) } returns usedUserPoint
            every { pointHistoryRepository.save(any()) } returns savedHistory

            // When
            val result = pointService.use(userId, useAmount)

            // Then
            assertThat(result.point).isEqualTo(expectedFinalAmount)
            assertThat(result.id).isEqualTo(userId)

            verify(exactly = 1) { userPointRepository.getPoint(userId) }
            verify(exactly = 1) { userPointRepository.save(any()) }
            verify(exactly = 1) { pointHistoryRepository.save(any()) }
        }

        @Test
        @DisplayName("사용할 때 히스토리가 올바르게 저장된다")
        fun `사용할 때 히스토리가 올바르게 저장된다`() {
            // Given
            val userId = 1L
            val useAmount = 300L
            val existingUserPoint = UserPoint(userId, 1000L, System.currentTimeMillis())
            val usedUserPoint = UserPoint(userId, 700L, System.currentTimeMillis())

            val savedHistory = PointHistory(1L, userId, TransactionType.USE, useAmount, System.currentTimeMillis())

            every { userPointRepository.getPoint(userId) } returns existingUserPoint
            every { userPointRepository.save(any()) } returns usedUserPoint
            every { pointHistoryRepository.save(any()) } returns savedHistory

            // When
            pointService.use(userId, useAmount)

            // Then
            verify(exactly = 1) { pointHistoryRepository.save(any()) }
        }

        @Test
        @DisplayName("잔액이 부족할 때 예외가 발생한다")
        fun `잔액이 부족할 때 예외가 발생한다`() {
            // Given
            val userId = 1L
            val currentAmount = 1000L
            val useAmount = 1500L // 현재 잔액보다 많은 금액
            val existingUserPoint = UserPoint(userId, currentAmount, System.currentTimeMillis())

            every { userPointRepository.getPoint(userId) } returns existingUserPoint

            // When & Then
            assertThatThrownBy {
                pointService.use(userId, useAmount)
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("사용 금액이 현재 포인트보다 클 수 없습니다.")

            verify(exactly = 1) { userPointRepository.getPoint(userId) }
            verify(exactly = 0) { userPointRepository.save(any()) }
            verify(exactly = 0) { pointHistoryRepository.save(any()) }
        }

        @Test
        @DisplayName("사용 금액이 0 이하일 때 예외가 발생한다")
        fun `사용 금액이 0 이하일 때 예외가 발생한다`() {
            // Given
            val userId = 1L
            val invalidAmount = -100L
            val existingUserPoint = UserPoint(userId, 1000L, System.currentTimeMillis())

            every { userPointRepository.getPoint(userId) } returns existingUserPoint

            // When & Then
            assertThatThrownBy {
                pointService.use(userId, invalidAmount)
            }.isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("사용 금액은 0보다 커야 합니다.")

            verify(exactly = 1) { userPointRepository.getPoint(userId) }
            verify(exactly = 0) { userPointRepository.save(any()) }
            verify(exactly = 0) { pointHistoryRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("포인트 조회 테스트")
    inner class GetPointTest {
        @Test
        @DisplayName("유저의 포인트를 정상적으로 조회할 수 있다")
        fun `유저의 포인트를 정상적으로 조회할 수 있다`() {
            // Given
            val userId = 1L
            val expectedPoint = 1500L
            val userPoint = UserPoint(userId, expectedPoint, System.currentTimeMillis())

            every { userPointRepository.getPoint(userId) } returns userPoint

            // When
            val result = pointService.getPoint(userId)

            // Then
            assertThat(result.id).isEqualTo(userId)
            assertThat(result.point).isEqualTo(expectedPoint)

            verify(exactly = 1) { userPointRepository.getPoint(userId) }
        }
    }

    @Nested
    @DisplayName("포인트 히스토리 조회 테스트")
    inner class GetHistoryTest {
        @Test
        @DisplayName("유저의 포인트 히스토리를 정상적으로 조회할 수 있다")
        fun `유저의 포인트 히스토리를 정상적으로 조회할 수 있다`() {
            // Given
            val userId = 1L
            val expectedHistories =
                listOf(
                    PointHistory(1L, userId, TransactionType.CHARGE, 1000L, System.currentTimeMillis()),
                    PointHistory(2L, userId, TransactionType.USE, 500L, System.currentTimeMillis()),
                )

            every { pointHistoryRepository.getHistory(userId) } returns expectedHistories

            // When
            val result = pointService.getHistory(userId)

            // Then
            assertThat(result).hasSize(2)
            assertThat(result[0].type).isEqualTo(TransactionType.CHARGE)
            assertThat(result[1].type).isEqualTo(TransactionType.USE)

            verify(exactly = 1) { pointHistoryRepository.getHistory(userId) }
        }

        @Test
        @DisplayName("히스토리가 없는 경우 빈 리스트를 반환한다")
        fun `히스토리가 없는 경우 빈 리스트를 반환한다`() {
            // Given
            val userId = 1L
            every { pointHistoryRepository.getHistory(userId) } returns emptyList()

            // When
            val result = pointService.getHistory(userId)

            // Then
            assertThat(result).isEmpty()

            verify(exactly = 1) { pointHistoryRepository.getHistory(userId) }
        }
    }
}
