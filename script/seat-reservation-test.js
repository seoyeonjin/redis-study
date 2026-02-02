import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  stages: [
    { duration: '20s', target: 10 },
    { duration: '20s', target: 30 },
    { duration: '20s', target: 50 },
  ],
};

//export const options = {
//  duration: '1m',
//  vus: 50,
//};

const BASE_URLS = [
  'http://seat-backend-a:8080',
  'http://seat-backend-b:8080',
];

function pickBaseUrl() {
  return BASE_URLS[Math.floor(Math.random() * BASE_URLS.length)];
}

export default function () {
  const seatId = 1;
  const userId = `user-${__VU}-${__ITER}`;

  const BASE_URL = pickBaseUrl();

  const joinRes = http.post(`${BASE_URL}/seats/${seatId}/queue`, null, {
    headers: { 'X-USER-ID': userId }
  });

  check(joinRes, {
    'queue join success': (r) => r.status === 200,
  });

  let holding = false;
  for (let i = 0; i < 10; i++) {
    const res = http.get(`${BASE_URL}/seats/${seatId}/queue/status`, {
      headers: { 'X-USER-ID': userId }
    });

    if (res && res.status === 200 && res.json('status') === 'HOLDING') {
      holding = true;
      break;
    }
    sleep(0.3);
  }

  if (holding) {
    const reserveRes = http.post(`${BASE_URL}/seats/${seatId}/reserve`, null, {
      headers: { 'X-USER-ID': userId }
    });

    check(reserveRes, {
      'reserve success or expected fail': (r) =>
        r.status === 200 || r.status === 409 || r.status === 400,
    });
  }
}
