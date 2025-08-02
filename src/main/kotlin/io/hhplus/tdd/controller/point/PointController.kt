package io.hhplus.tdd.controller.point

import io.hhplus.tdd.controller.point.PointServiceInterface
import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.UserPoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/point")
class PointController(
    private val pointService: PointServiceInterface,
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * 특정 유저의 포인트를 조회하는 기능
     */
    @GetMapping("{id}")
    fun point(
        @PathVariable id: Long,
    ): UserPoint {
        logger.info("포인트 조회 요청: userId=$id")
        return pointService.getPoint(id)
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회하는 기능
     */
    @GetMapping("{id}/histories")
    fun history(
        @PathVariable id: Long,
    ): List<PointHistory> {
        logger.info("포인트 충전/이용 내역 조회 요청: userId=$id")
        return pointService.getHistory(id)
    }

    /**
     * 특정 유저의 포인트를 충전하는 기능
     */
    @PatchMapping("{id}/charge")
    fun charge(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        logger.info("포인트 충전 요청: userId=$id, amount=$amount")
        return pointService.charge(id, amount)
    }

    /**
     * 특정 유저의 포인트를 사용하는 기능
     */
    @PatchMapping("{id}/use")
    fun use(
        @PathVariable id: Long,
        @RequestBody amount: Long,
    ): UserPoint {
        logger.info("포인트 사용 요청: userId=$id, amount=$amount")
        return pointService.use(id, amount)
    }
}
