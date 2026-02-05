import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 100 },
    { duration: '60s', target: 300 },
    { duration: '60s', target: 500 },
  ],
};

const BASE_URL_QUEUE = 'http://seat-backend-a:8080';
const SEAT_ID = 2;

// [TODO] POLL_COUNT 설정 필수
const POLL_COUNT = __ENV.POLL_COUNT ? Number(__ENV.POLL_COUNT) : 10;

export default function () {
  const userId = `user-${__VU}-${__ITER}`;

  const joinRes = http.post(
    `${BASE_URL_QUEUE}/seats/${SEAT_ID}/queue`,
    null,
    { headers: { 'X-USER-ID': userId } }
  );

  check(joinRes, {
    'queue join success': (r) => r.status === 200,
  });

  for (let i = 0; i < POLL_COUNT; i++) {
    const res = http.get(
      `${BASE_URL_QUEUE}/seats/${SEAT_ID}/queue/status`,
      { headers: { 'X-USER-ID': userId } }
    );

    check(res, {
      'status ok': (r) => r.status === 200,
    });

    sleep(1);
  }
}
