package io.hhplus.tdd.infra.point.persistence

import io.hhplus.tdd.infra.point.database.PointHistoryTable
import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.repository.PointHistoryRepository
import org.springframework.stereotype.Repository

@Repository
class PointHistoryRepositoryImpl(
    private val pointHistoryTable: PointHistoryTable,
) : PointHistoryRepository {
    override fun getHistory(userId: Long): List<PointHistory> = pointHistoryTable.selectAllByUserId(userId)

    override fun save(history: PointHistory): PointHistory =
        pointHistoryTable.insert(
            history.userId,
            history.amount,
            history.type,
            history.timeMillis,
        )
}
