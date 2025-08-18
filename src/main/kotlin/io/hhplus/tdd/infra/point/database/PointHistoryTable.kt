package io.hhplus.tdd.infra.point.database

import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.TransactionType
import org.springframework.stereotype.Component

/**
 * 해당 Table 클래스는 변경하지 않고 공개된 API 만을 사용해 데이터를 제어합니다.
 *
 * 동시성 이슈 발생시키기 위한 DB 테이블 역할
 */
@Component
class PointHistoryTable {
    private val table = mutableListOf<PointHistory>()
    private var cursor: Long = 1L

    fun insert(
        id: Long,
        amount: Long,
        transactionType: TransactionType,
        updateMillis: Long,
    ): PointHistory {
        Thread.sleep(Math.random().toLong() * 300L)
        val history =
            PointHistory(
                id = cursor++,
                userId = id,
                amount = amount,
                type = transactionType,
                timeMillis = updateMillis,
            )
        table.add(history)
        return history
    }

    fun selectAllByUserId(userId: Long): List<PointHistory> = table.filter { it.userId == userId }
}
