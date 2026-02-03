import http from 'k6/http';
import { sleep, check } from 'k6';
import { Trend } from 'k6/metrics';

const holdToReserveLatency = new Trend(
  'hold_to_reserve_latency',
  true
);

export const options = {
  stages: [
   { duration: '30s', target: 50 },
   { duration: '60s', target: 70 },
   { duration: '60s', target: 70 },
  ]
};

const BASE_URLS = [
  'http://seat-backend-a:8080',
  'http://seat-backend-b:8080',
];

function pickBaseUrl() {
  return BASE_URLS[Math.floor(Math.random() * BASE_URLS.length)];
}

export default function () {
  const seatId = 4;
  const userId = `user-${__VU}-${__ITER}`;
  const BASE_URL = pickBaseUrl();

  /* 1️⃣ Queue Join */
  http.post(
    `${BASE_URL}/seats/${seatId}/queue`,
    null,
    { headers: { 'X-USER-ID': userId }, tags: { action: 'queue_join' } }
  );

  /* 2️⃣ Queue Status Polling */
  let holding = false;
  let holdDetectedAt = null;

  for (let i = 0; i < 10; i++) {
    const statusRes = http.get(
      `${BASE_URL}/seats/${seatId}/queue/status`,
      {
        headers: { 'X-USER-ID': userId },
        tags: { action: 'queue_status' },
      }
    );

    if (
      statusRes &&
      statusRes.status === 200 &&
      statusRes.json('status') === 'HOLDING'
    ) {
      holding = true;
      holdDetectedAt = Date.now(); // 👈 HOLD 감지 시각
      break;
    }

    sleep(0.3);
  }

  /* 3️⃣ Reserve */
  if (holding) {
    const reserveStart = Date.now();

    const reserveRes = http.post(
      `${BASE_URL}/seats/${seatId}/reserve`,
      null,
      {
        headers: { 'X-USER-ID': userId },
        tags: { action: 'reserve' },
      }
    );

    const reserveEnd = Date.now();

    // 👇 HOLD → RESERVE 실제 지연 시간 기록
    holdToReserveLatency.add(reserveEnd - holdDetectedAt);

    check(reserveRes, {
      'reserve result is valid': (r) =>
        r.status === 200 || r.status === 409 || r.status === 400,
    });
  }

  sleep(1);
}
