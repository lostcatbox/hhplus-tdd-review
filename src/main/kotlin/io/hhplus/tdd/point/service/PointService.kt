package io.hhplus.tdd.point.service

import io.hhplus.tdd.controller.point.PointServiceInterface
import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.UserPoint
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.UserPointRepository
import org.springframework.stereotype.Service

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
    override fun charge(
        userId: Long,
        amount: Long,
    ): UserPoint {
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

        // 충전된 포인트 정보를 반환
        return savedUserPoint
    }

    override fun use(
        userId: Long,
        amount: Long,
    ): UserPoint {
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
    }

    override fun getPoint(userId: Long): UserPoint {
        // 특정 유저의 현재 포인트 조회
        return userPointRepository.getPoint(userId)
    }

    // 해당 유저의 트랜잭션이 없는 경우 빈 리스트를 반환
    override fun getHistory(userId: Long): List<PointHistory> {
        // 특정 유저의 포인트 트랜잭션 조회
        return pointHistoryRepository
            .getHistory(userId)
            .ifEmpty { emptyList() } // 트랜잭션이 없는 경우 빈 리스트 반환
    }
}
