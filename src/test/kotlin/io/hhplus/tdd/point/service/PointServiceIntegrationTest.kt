package io.hhplus.tdd.point.service

import io.hhplus.tdd.controller.point.PointServiceInterface
import io.hhplus.tdd.infra.point.database.PointHistoryTable
import io.hhplus.tdd.infra.point.database.UserPointTable
import io.hhplus.tdd.infra.point.persistence.PointHistoryRepositoryImpl
import io.hhplus.tdd.infra.point.persistence.UserPointRepositoryImpl
import io.hhplus.tdd.point.domain.TransactionType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@DisplayName("PointService 통합 테스트 - 동시성 이슈 검증")
class PointServiceIntegrationTest {
    private lateinit var userPointTable: UserPointTable
    private lateinit var pointHistoryTable: PointHistoryTable
    private lateinit var userPointRepository: UserPointRepositoryImpl
    private lateinit var pointHistoryRepository: PointHistoryRepositoryImpl
    private lateinit var pointService: PointServiceInterface

    /**
     * 테스트 환경 설정
     *
     * 테스트가 진행되기 전, 항상 DB를 초기화하고 Repository를 PointService에 주입합니다.
     */
    @BeforeEach
    fun setUp() {
        // 실제 구현체들을 사용한 통합 테스트
        userPointTable = UserPointTable()
        pointHistoryTable = PointHistoryTable()
        userPointRepository = UserPointRepositoryImpl(userPointTable)
        pointHistoryRepository = PointHistoryRepositoryImpl(pointHistoryTable)
        pointService = PointService(userPointRepository, pointHistoryRepository)
    }

    @Test
    @DisplayName("100개의 동시 충전 요청 테스트")
    fun `100개의 동시 충전 요청시 동시성 이슈가 발생할 수 있다`() {
        // Given
        val userId = 1L
        val chargeAmount = 100L
        val threadCount = 100
        val expectedTotalAmount = chargeAmount * threadCount // 예상 총 충전 금액: 10,000

        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount) // 100개 작업 완료를 기다림
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)

        // When - 100개의 스레드가 동시에 충전 요청
        repeat(threadCount) {
            executor.execute {
                try {
                    pointService.charge(userId, chargeAmount)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failureCount.incrementAndGet()
                    println("충전 실패: ${e.message}")
                } finally {
                    latch.countDown() // 작업 완료 신호
                }
            }
        }

        // 모든 작업 완료 대기 (최대 30초)
        val completed = latch.await(30, TimeUnit.SECONDS)
        if (!completed) {
            println("30초 내에 모든 작업이 완료되지 않았습니다!")
        }
        executor.shutdown()

        // Then - 결과 검증
        val finalUserPoint = pointService.getPoint(userId)
        val historyList = pointService.getHistory(userId)
        val chargeHistories = historyList.filter { it.type == TransactionType.CHARGE }

        println("=== 충전 테스트 결과 ===")
        println("성공한 충전 요청: ${successCount.get()}")
        println("실패한 충전 요청: ${failureCount.get()}")
        println("최종 포인트: ${finalUserPoint.point}")
        println("예상 포인트: $expectedTotalAmount")
        println("충전 히스토리 개수: ${chargeHistories.size}")
        println("전체 히스토리 개수: ${historyList.size}")

        assertThat(successCount.get()).isEqualTo(threadCount)
        assertThat(expectedTotalAmount).isEqualTo(finalUserPoint.point)
        assertThat(chargeHistories.size).isEqualTo(threadCount)
        assertThat(finalUserPoint.point).isEqualTo(expectedTotalAmount)
    }

    @Test
    @DisplayName("100개의 동시 사용 요청 테스트 - 동시성 이슈 발생 가능")
    fun `100개의 동시 사용 요청시 동시성 이슈가 발생할 수 있다`() {
        // Given - 먼저 충분한 포인트를 충전
        val userId = 2L
        val initialAmount = 50000L // 충분한 초기 포인트
        val useAmount = 100L
        val threadCount = 100
        val expectedRemainingAmount = initialAmount - (useAmount * threadCount) // 예상 잔액: 40,000

        // 초기 포인트 충전
        pointService.charge(userId, initialAmount)

        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount) // 100개 작업 완료를 기다림
        val successCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)

        // When - 100개의 스레드가 동시에 사용 요청
        repeat(threadCount) {
            executor.execute {
                try {
                    pointService.use(userId, useAmount)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failureCount.incrementAndGet()
                    println("사용 실패: ${e.message}")
                } finally {
                    latch.countDown() // 작업 완료 신호
                }
            }
        }

        // 모든 작업 완료 대기 (최대 30초)
        val completed = latch.await(30, TimeUnit.SECONDS)
        if (!completed) {
            println("30초 내에 모든 작업이 완료되지 않았습니다!")
        }
        executor.shutdown()

        // Then - 결과 검증
        val finalUserPoint = pointService.getPoint(userId)
        val historyList = pointService.getHistory(userId)
        val useHistories = historyList.filter { it.type == TransactionType.USE }

        println("=== 충전 테스트 결과 ===")
        println("성공한 충전 요청: ${successCount.get()}")
        println("실패한 충전 요청: ${failureCount.get()}")
        println("최종 포인트: ${finalUserPoint.point}")
        println("예상 포인트: $expectedRemainingAmount")
        println("사용 히스토리 개수: ${useHistories.size}")
        println("전체 히스토리 개수: ${historyList.size}")

        assertThat(successCount.get()).isEqualTo(threadCount)
        assertThat(expectedRemainingAmount).isEqualTo(finalUserPoint.point)
        assertThat(useHistories.size).isEqualTo(threadCount)
        assertThat(finalUserPoint.point).isEqualTo(expectedRemainingAmount)
    }

    @Test
    @DisplayName("충전과 사용을 동시에 수행하는 복합 동시성 테스트")
    fun `충전과 사용을 동시에 수행하는 복합 동시성 테스트 - 사용 요청 중 잔액부족 에러도 포함될 수 있다`() {
        // Given
        val userId = 3L
        val chargeAmount = 500L
        val useAmount = 300L
        val chargeThreadCount = 50
        val useThreadCount = 50
        val totalThreadCount = chargeThreadCount + useThreadCount

        // 초기 포인트 설정
        val initialAmount = 10000L
        pointService.charge(userId, initialAmount)

        val executor = Executors.newFixedThreadPool(totalThreadCount)
        val latch = CountDownLatch(totalThreadCount) // 100개 작업 완료를 기다림
        val chargeSuccessCount = AtomicInteger(0)
        val useSuccessCount = AtomicInteger(0)
        val failureCount = AtomicInteger(0)

        // When - 충전과 사용을 동시에 수행

        // 50개의 충전 요청
        repeat(chargeThreadCount) {
            executor.execute {
                try {
                    pointService.charge(userId, chargeAmount)
                    chargeSuccessCount.incrementAndGet()
                } catch (e: Exception) {
                    failureCount.incrementAndGet()
                    println("충전 실패: ${e.message}")
                } finally {
                    latch.countDown() // 작업 완료 신호
                }
            }
        }

        // 50개의 사용 요청
        repeat(useThreadCount) {
            executor.execute {
                try {
                    pointService.use(userId, useAmount)
                    useSuccessCount.incrementAndGet()
                } catch (e: Exception) {
                    failureCount.incrementAndGet()
                    println("사용 실패: ${e.message}")
                } finally {
                    latch.countDown() // 작업 완료 신호
                }
            }
        }

        // 모든 작업 완료 대기 (최대 60초)
        val completed = latch.await(60, TimeUnit.SECONDS)
        if (!completed) {
            println("60초 내에 모든 작업이 완료되지 않았습니다!")
        }
        executor.shutdown()

        // Then - 결과 검증
        val finalUserPoint = pointService.getPoint(userId)
        val historyList = pointService.getHistory(userId)
        val chargeHistories = historyList.filter { it.type == TransactionType.CHARGE }
        val useHistories = historyList.filter { it.type == TransactionType.USE }

        // 예상 최종 포인트 계산
        val expectedFinalPoint =
            initialAmount +
                (chargeAmount * chargeSuccessCount.get()) -
                (useAmount * useSuccessCount.get())

        println("=== 복합 동시성 테스트 결과 ===")
        println("초기 포인트: $initialAmount")
        println("성공한 충전 요청: ${chargeSuccessCount.get()}")
        println("성공한 사용 요청: ${useSuccessCount.get()}")
        println("실패한 요청: ${failureCount.get()}")
        println("최종 포인트: ${finalUserPoint.point}")
        println("예상 최종 포인트: $expectedFinalPoint")
        println("충전 히스토리: ${chargeHistories.size}")
        println("사용 히스토리: ${useHistories.size}")
        println("전체 히스토리: ${historyList.size}")

        // 검증
        assertThat(chargeSuccessCount.get() + useSuccessCount.get()).isEqualTo(totalThreadCount)
        assertThat(finalUserPoint.point).isEqualTo(expectedFinalPoint)
    }

    @Test
    @DisplayName("동일한 유저에 대한 대량 동시 요청 테스트")
    fun `한 유저에 대해 대량의 동시 요청을 처리할 때 동시성 이슈 확인`() {
        // Given
        val userId = 4L
        val requestCount = 200
        val amount = 50L

        val executor = Executors.newFixedThreadPool(requestCount)
        val latch = CountDownLatch(requestCount) // 200개 작업 완료를 기다림
        val useSuccessCount = AtomicInteger(0)
        val chargeSuccessCount = AtomicInteger(0)
        val useFailureCount = AtomicInteger(0)
        val chargeFailureCount = AtomicInteger(0)

        // When - 충전과 사용을 번갈아가며 수행
        repeat(requestCount) { index ->
            executor.execute {
                if (index % 2 == 0) {
                    // 짝수: 충전 요청 실행
                    try {
                        pointService.charge(userId, amount)
                        chargeSuccessCount.incrementAndGet()
                    } catch (e: Exception) {
                        chargeFailureCount.incrementAndGet()
                    }
                } else {
                    // 홀수: 사용 요청 실행
                    try {
                        pointService.use(userId, amount)
                        useSuccessCount.incrementAndGet()
                    } catch (e: Exception) {
                        useFailureCount.incrementAndGet()
                    }
                }

                latch.countDown() // 작업 완료 신호
            }
        }

        // 모든 작업 완료 대기 (최대 60초)
        val completed = latch.await(60, TimeUnit.SECONDS)
        if (!completed) {
            println("60초 내에 모든 작업이 완료되지 않았습니다!")
        }
        executor.shutdown()

        // Then - 결과 분석

        val finalUserPoint = pointService.getPoint(userId)
        val historyList = pointService.getHistory(userId)

        // 예상 최종 포인트 (충전 성공 - 사용 성공)
        val expectedFinalPoint = (chargeSuccessCount.get() - useSuccessCount.get()) * amount

        println("=== 대량 동시 요청 테스트 결과 ===")
        println("총 요청 수: $requestCount")
        println("충전 성공: $chargeSuccessCount, 충전 실패: $chargeFailureCount")
        println("사용 성공: $useSuccessCount, 사용 실패: $useFailureCount")
        println("최종 포인트: ${finalUserPoint.point}")
        println("예상 최종 포인트: $expectedFinalPoint")
        println("히스토리 총 개수: ${historyList.size}")

        // 검증
        // 총 요청 = 충전 성공 횟수 + 사용 성공 횟수 + 사용 실패 횟수
        // 사용 실패 횟수가 포함되는 이유 : 잔액 부족으로 인한 실패이므로
        assertThat(
            chargeSuccessCount.get() + useSuccessCount.get() + useFailureCount.get(),
        ).isEqualTo(requestCount)
        assertThat(finalUserPoint.point).isEqualTo(expectedFinalPoint)
    }
}
