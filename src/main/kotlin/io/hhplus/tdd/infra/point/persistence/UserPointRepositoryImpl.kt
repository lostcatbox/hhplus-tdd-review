package io.hhplus.tdd.infra.point.persistence

import io.hhplus.tdd.infra.point.database.UserPointTable
import io.hhplus.tdd.point.domain.UserPoint
import io.hhplus.tdd.point.repository.UserPointRepository
import org.springframework.stereotype.Repository

@Repository
class UserPointRepositoryImpl(
    private val userPointTable: UserPointTable,
) : UserPointRepository {
    override fun getPoint(userId: Long): UserPoint = userPointTable.selectById(userId)

    override fun save(userPoint: UserPoint): UserPoint =
        userPointTable.insertOrUpdate(
            id = userPoint.id,
            amount = userPoint.point,
        )
}
