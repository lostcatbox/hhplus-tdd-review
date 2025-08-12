package io.hhplus.tdd.point.domain

/**
 *
 * PointHistory 클래스란?
 * - 포인트 충전 및 사용 내역을 기록하고 조회하는 기능을 제공합니다.
 * - 어떤 유저가 언제 얼마를 충전하거나 사용했는지를 기록합니다.(현재의 포인트 정보는 UserPoint를 참고)
 *
 * 주요 구성
 * @property id 포인트 트랜잭션의 고유 ID
 * @property userId 트랜잭션을 발생시킨 유저의 ID
 * @property type 트랜잭션의 종류 (충전 또는 사용)
 * @property amount 트랜잭션 금액 (충전 시 양수, 사용 시 양수)
 * @property timeMillis 트랜잭션이 발생한 시간 (밀리초 단위)
 *
 * 주요 기능
 * - 포인트 트랜잭션을 저장하고 조회하는 기능을 제공합니다.
 * - 특정 유저의 포인트 트랜잭션을 조회할 수 있습니다.
 * - 포인트 충전 히스토리 생성 메서드를 제공합니다
 * - 포인트 사용 히스토리 생성 메서드를 제공합니다
 *
 * 주요 에러
 * - 포인트 트랜잭션 조회 시, 해당 유저의 트랜잭션이 없는 경우 빈 리스트를 반환해야 합니다.
 * - amount가 0 이하인 경우, 400 에러를 반환해야 합니다.
 */
data class PointHistory(
    val id: Long,
    val userId: Long,
    val type: TransactionType,
    val amount: Long,
    val timeMillis: Long,
) {
    init {
        require(amount > 0) { "amount는 0보다 커야 합니다." }
    }

    companion object {
        // 포인트 충전 히스토리 생성 팩토리 메서드
        fun createChargeHistory(
            userId: Long,
            amount: Long,
        ): PointHistory =
            PointHistory(
                id = 0L, // ID는 DB에서 자동 생성되므로 0L로 설정
                userId = userId,
                type = TransactionType.CHARGE,
                amount = amount,
                timeMillis = System.currentTimeMillis(),
            )

        // 포인트 사용 히스토리 생성 팩토리 메서드
        fun createUseHistory(
            userId: Long,
            amount: Long,
        ): PointHistory =
            PointHistory(
                id = 0L, // ID는 DB에서 자동 생성되므로 0L로 설정
                userId = userId,
                type = TransactionType.USE,
                amount = amount,
                timeMillis = System.currentTimeMillis(),
            )
    }
}

/**
 * 포인트 트랜잭션 종류
 * - CHARGE : 충전
 * - USE : 사용
 */
enum class TransactionType {
    CHARGE,
    USE,
}
