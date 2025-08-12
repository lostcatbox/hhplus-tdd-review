package io.hhplus.tdd.point.domain

/**
* UserPoint 클래스란?
* 의도: 특정 유저의 "현재" 포인트 정보를 나타내는 데이터 클래스입니다.
*
* 주요 구성
* @property id 유저의 고유 ID
* @property point 현재 유저가 보유한 포인트
* @property updateMillis 마지막으로 포인트가 업데이트된 시간 (밀리초 단위)
*
* 주요 기능
* 충전, 사용, 조회 등의 기능을 제공해야합니다.
*
* 주요 에러
* - 포인트 충전 시, 유저가 존재하지 않으면 404 에러를 반환해야 합니다.
* - 포인트 충전 시 0 이하의 금액을 충전하려고 하면 400 에러를 반환해야 합니다.
* - 포인트 충전 시, 2,000,000 이상 충전하려고 하면 400 에러를 반환해야 합니다.
* - 포인트 사용 시, 유저가 존재하지 않으면 404 에러를 반환해야 합니다.
* - 포인트 사용 시, 사용할 금액보다 현재 잔액이 부족하면 400 에러를 반환해야 합니다.
* - 포인트 사용 시, 0 이하의 금액을 사용하려고 하면 400 에러를 반환해야 합니다.
*/
data class UserPoint(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
) {
    fun charge(amount: Long): UserPoint {
        require(amount > 0) { "충전 금액은 0보다 커야 합니다." }
        require(amount <= 2000000) { "충전 금액은 2,000,000을 초과할 수 없습니다." }

        // 충전 로직 구현
        return this.copy(point = this.point + amount, updateMillis = System.currentTimeMillis())
    }

    fun use(amount: Long): UserPoint {
        require(amount > 0) { "사용 금액은 0보다 커야 합니다." }
        require(amount <= this.point) { "사용 금액이 현재 포인트보다 클 수 없습니다." }

        // 사용 로직 구현
        return this.copy(point = this.point - amount, updateMillis = System.currentTimeMillis())
    }
}
