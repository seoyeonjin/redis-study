import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 200,
  duration: '30s',
};

const BASE_URL_RESERVE = 'http://seat-backend-b:8080';
const SEAT_ID = 2;

export default function () {
  const userId = `user-${__VU}`;

  const res = http.post(
    `${BASE_URL_RESERVE}/seats/${SEAT_ID}/reserve`,
    null,
    { headers: { 'X-USER-ID': userId } }
  );

  check(res, {
    'reserve success or expected fail': (r) =>
      r.status === 200 || r.status === 409 || r.status === 400,
  });
}
