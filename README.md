# Redis를 이용한 공정 대기열 좌석 예약 시스템

## 1. 프로젝트 소개
Spring Boot와 Redis(Sorted Set)를 이용하여 높은 동시성 환경의 **선착순 좌석 예매 시스템**을 구현한 프로젝트입니다.

대기열 처리, 데이터 정합성 보장, 그리고 Docker Compose 기반의 통합 테스트 및 모니터링 환경에 초점을 맞추었습니다.

## 2. 주요 기능
-   **공정 대기열**: Redis Sorted Set을 활용한 선입선출(FIFO) 대기열
-   **원자적 처리**: Lua 스크립트를 통한 동시성 제어 및 데이터 정합성 보장
-   **통합 개발 환경**: Docker Compose로 백엔드, DB, 모니터링 스택을 한 번에 관리
-   **성능 검증**: k6를 이용한 시나리오 기반 부하 테스트 및 Grafana 시각화
-   **실시간 시뮬레이터**: 웹 UI(`simulator.html`)를 통한 대기열 동작 시각화

## 3. 사용 기술 (Tech Stack)
-   **Backend**: Java 17, Spring Boot
-   **Database**: PostgreSQL, Redis
-   **Build**: Gradle
-   **Test**: k6, JUnit
-   **Infrastructure**: Docker, Docker Compose
-   **Monitoring**: Prometheus, Grafana, cAdvisor

## 4. 시작하기 (Getting Started)
### 요구 사항
-   Docker Desktop
-   Java 17 JDK

### 설정 및 실행
1.  **`.env` 파일 생성**: `cp .env.example .env` (필요시 내부 포트 등 수정)
2.  **시스템 실행**: `docker-compose up -d --build`
3.  **Grafana 확인**: `http://localhost:3000`

### 시스템 종료
-   `docker-compose down`

## 5. API 명세
모든 요청 시, 사용자를 식별하기 위해 `X-USER-ID` 헤더가 필요합니다.

-   `POST /seats/{seatId}/queue`
    -   **설명**: 특정 좌석에 대한 예약 대기열에 참여합니다.
    -   **성공 응답**: `200 OK`와 함께 현재 대기 순번(e.g., `5`)을 반환합니다.

-   `GET /seats/{seatId}/queue/status`
    -   **설명**: 대기열에서의 현재 상태(대기 순번, 총 대기자 수)를 확인합니다.

-   `GET /seats/{seatId}/hold/status`
    -   **설명**: 좌석을 임시 선점(HOLD)했는지와 남은 유효 시간을 확인합니다.

-   `POST /seats/{seatId}/reserve`
    -   **설명**: 임시 선점한 좌석의 예약을 최종 확정합니다. HOLD 상태일 때만 성공합니다.
    -   **성공 응답**: `200 OK`

## 6. 성능 테스트 및 결과 분석

### 테스트 환경 및 실행
-   **도구**: k6, Prometheus, Grafana
-   **부하 테스트 실행**: `docker-compose run k6`
-   **데이터 초기화**: `docker exec -it seat-postgres psql -U seat -d seatdb -c "TRUNCATE TABLE reservations;"`

### 테스트 결과 요약
| 테스트 시나리오 | 가상 사용자(VU) | 평균 응답 시간 | p95 응답 시간 |
| --- | --- | --- | --- |
| **선착순 경쟁** | 50 (총 50회) | - | - |
| **지속 부하** | 50 (1분) | 7.29ms | 23.59ms |
| **실전형(점진적 증가)** | 10→30→50 | **2.48ms** | **7.19ms** |

<br>

<details>
<summary>**시나리오 1: 정확히 N명 경쟁 실험 (상세 분석)**</summary>

-   **목표**: 50명의 사용자가 동시에 단 하나의 좌석을 두고 경쟁, 선착순 로직의 정확성을 검증합니다.
-   **k6 설정**: `vus: 50, iterations: 50`
-   **결과**:
    -   정확히 **1명**의 사용자만 예약에 성공했으며, Redis 잠금과 DB Unique 제약 조건이 효과적으로 동작함을 확인했습니다.
    -   테스트 중 관찰된 메모리 사용량의 급감 후 회복 패턴은, GC(Garbage Collection)에 의한 **전형적이고 건강한 상태**임을 의미합니다. (메모리 누수 아님)

</details>

<details>
<summary>**시나리오 2: 지속 부하 안정성 검증 (상세 분석)**</summary>

-   **목표**: 50명의 사용자가 1분 동안 지속적으로 요청을 보내는 상황에서 시스템의 안정성을 검증합니다.
-   **k6 설정**: `duration: '1m', vus: 50`
-   **결과**:
    -   초당 약 **250건**의 요청을 안정적으로 처리했습니다.
    -   좌석이 1개이므로 대부분의 요청은 의도된 대로 "예약 실패" 처리되었으나, 이 실패 처리 역시 **평균 7ms, p95 24ms**로 매우 빠르게 완료되었습니다.
    -   시스템 다운이나 타임아웃 없이 모든 부하를 안정적으로 처리했습니다.

</details>

<details>
<summary>**시나리오 3: 점진적 부하 실전형 테스트 (상세 분석)**</summary>

-   **목표**: 이벤트 시작 직후 사용자가 점차 증가하는 실제 상황을 모사하여 시스템 반응을 확인합니다.
-   **결과**:
    -   초당 약 **160건**의 요청을 안정적으로 처리했으며, 평균 응답 시간 **2.48ms**, p95 **7.19ms**로 매우 우수한 성능을 보였습니다.
    -   트래픽 증가에 따라 예약 실패율은 자연스럽게 상승했으나, 이는 시스템이 부하를 의도적으로 제어하고 있음을 의미하며, 실패 요청 또한 지연 없이 빠르게 처리되었습니다.

</details>

## 7. 프론트엔드 시뮬레이터
`src/main/resources/static/index.html` 파일을 브라우저에서 열면 대기열 시스템의 동작을 실시간으로 시각화할 수 있습니다.

사용자별 대기 순번, 좌석 선점 상태, 남은 시간 등을 시각적으로 보여주어 전체 흐름을 직관적으로 파악하는 데 유용합니다.
