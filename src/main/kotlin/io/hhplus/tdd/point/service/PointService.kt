package io.hhplus.tdd.point.service

import io.hhplus.tdd.controller.point.PointServiceInterface
import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.UserPoint
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.UserPointRepository
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

/**
 * PointService 클래스란?
 * - 의도: 포인트 충전, 사용, 조회 등의 기능을 제공하는 서비스 클래스입니다.
 *
 * 주요 기능
 * - 포인트 충전: 유저의 포인트를 충전합니다.
 * - 포인트 사용: 유저의 포인트를 사용합니다.
 * - 포인트 조회: 유저의 현재 포인트 정보를 조회합니다.
 *
 * 주요 에러
 */
@Service
class PointService(
    private val userPointRepository: UserPointRepository,
    private val pointHistoryRepository: PointHistoryRepository,
) : PointServiceInterface {
    // 사용자별 락 객체 관리 (각 사용자별 ReentrantLock 을 사용하여 동시성 제어)
    private val userLocks = ConcurrentHashMap<Long, ReentrantLock>()

    override fun charge(
        userId: Long,
        amount: Long,
    ): UserPoint {
        // 유저 포인트에 대한 락 획득
        val lock = userLocks.computeIfAbsent(userId) { ReentrantLock() }
        lock.lock()
        try {
            // 유저의 현재 포인트 조회
            val userPoint = userPointRepository.getPoint(userId)

            // 포인트 충전 수행
            val chargedUserPoint = userPoint.charge(amount)

            // 유저 포인트를 저장
            val savedUserPoint = userPointRepository.save(chargedUserPoint)

            // 포인트 충전 이력을 저장
            pointHistoryRepository.save(
                PointHistory.createChargeHistory(
                    userId = chargedUserPoint.id,
                    amount = chargedUserPoint.point,
                ),
            )

            return savedUserPoint
        } finally {
            lock.unlock()
        }
    }

    override fun use(
        userId: Long,
        amount: Long,
    ): UserPoint {
        // 유저 포인트에 대한 락 획득
        val lock = userLocks.computeIfAbsent(userId) { ReentrantLock() }
        lock.lock()
        try {
            // 유저의 현재 포인트 조회
            val userPoint = userPointRepository.getPoint(userId)

            // 유저 포인트 사용
            val updatedUserPoint = userPoint.use(amount)

            // 포인트 사용 이력을 저장
            pointHistoryRepository.save(
                PointHistory.createUseHistory(
                    userId = userId,
                    amount = amount,
                ),
            )

            // 유저 포인트를 저장
            return userPointRepository.save(updatedUserPoint)
        } finally {
            lock.unlock()
        }
    }

    override fun getPoint(userId: Long): UserPoint = userPointRepository.getPoint(userId)

    override fun getHistory(userId: Long): List<PointHistory> = pointHistoryRepository.getHistory(userId).ifEmpty { emptyList() }
}
