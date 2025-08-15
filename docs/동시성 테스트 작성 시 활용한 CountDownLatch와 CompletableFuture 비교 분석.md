# CountDownLatch
- **역할** : CountDownLatch는 동기화 도구로, 특정 작업이 완료될 때까지 다른 스레드가 대기하도록 하는 기능을 제공합니다.
- **동작 원리**:
  - CountDownLatch 는 카운트 수(count)로 초기화되어 생성된다.
  - countDown() 메서드가 호출되면 count가 1씩 감소한다.
  - await() 메서드는 count가 0이 될 때까지 대기한다.
- **활용법** :
  - 동시성 테스트 작성 시 사용


## 예시 코드
- 설명 : CountDownLatch를 사용하여 100개의 충전 요청을 처리하고, 모든 작업이 완료될 때까지 대기하는 예시

```kotlin
   @Test
    @DisplayName("100개의 동시 충전 요청 테스트")
    fun `동시 100개의 충전 요청 시 동시성 이슈가 발생할 수 있다`() {
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
        executor.submit {
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
}
```


# CompletableFuture
- **역할** : CompletableFuture는 비동기 프로그래밍을 지원하는 클래스입니다. 비동기 작업의 결과를 처리하고, 여러 작업을 조합할 수 있는 기능을 제공합니다.
- **동작 원리**:
  - CompletableFuture는 Future 인터페이스를 구현하며, 비동기 작업의 결과를 나타냅니다.
  - 여러 CompletableFuture를 조합하여 병렬로 작업을 수행하거나, 순차적으로 실행할 수 있습니다.

- **활용법** :
  - CompletableFuture를 사용하여 비동기 작업을 처리하는 동시성 테스트를 작성할 수 있습니다.
  - 비동기 작업의 결과를 기다리거나, 콜백 함수 조합을 사용하여 결과를 처리할 수 있습니다.


## 예시 코드 1
- 설명 : CompletableFuture를 사용하여 100개의 충전 요청을 처리하고, 모든 작업이 완료될 때까지 기다리는 예시

```kotlin
@Test
@DisplayName("100개의 동시 충전 요청 테스트")
fun `동시 100개의 충전 요청 시 동시성 이슈가 발생할 수 있다`() {
    // Given
    val userId = 1L
    val chargeAmount = 100L
    val threadCount = 100
    val expectedTotalAmount = chargeAmount * threadCount // 예상 총 충전 금액: 10,000
    val futures = mutableListOf<CompletableFuture<Void>>()
    val successCount = AtomicInteger(0)
    val failureCount = AtomicInteger(0)

    // When - 100개의 스레드가 동시에 충전 요청
    repeat(threadCount) {
        val future =
            CompletableFuture.runAsync {
                try {
                    pointService.charge(userId, chargeAmount)
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failureCount.incrementAndGet()
                    println("충전 실패: ${e.message}")
                }
            }
        futures.add(future)
    }

    // 모든 작업이 완료되기를 기다림
    CompletableFuture.allOf(*futures.toTypedArray()).join()
}

```

## 예시 코드 2
- 설명 : CompletableFuture를 사용하여 비동기 작업을 처리하고, 결과를 조합하는 예시

```kotlin
 @Test
@DisplayName("비동기 작업 결과 조합 테스트")
fun `비동기 작업 결과 조합 테스트`() {
    @Test
    @DisplayName("비동기 작업 결과 조합 테스트")
    fun thenRun() {
        val future =
            CompletableFuture
                .supplyAsync {
                    // 비동기 작업 수행
                    Thread.sleep(1000) // 예시로 1초 대기
                    "작업 결과"
                }.thenApplyAsync { value ->
                    // 비동기 작업 결과 처리
                    println("작업 결과: $value")
                }.thenRun {
                    // 작업 완료 후 실행할 코드
                    println("작업이 완료되었습니다.")
                }

        // 결과를 기다림
        future.get()
    }
}
```
