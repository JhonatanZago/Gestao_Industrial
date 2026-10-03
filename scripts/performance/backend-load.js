import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const companyId = __ENV.COMPANY_ID || '1';
const sectorId = __ENV.SECTOR_ID || '1';
const employeeId = __ENV.EMPLOYEE_ID || '1';
const email = __ENV.TEST_EMAIL || '';
const password = __ENV.TEST_PASSWORD || '';
const failures = new Rate('backend_failures');
const responseBytes = new Trend('backend_response_bytes');

export const options = {
  vus: Number(__ENV.VUS || 2),
  duration: __ENV.DURATION || '30s',
  thresholds: { http_req_failed: ['rate<0.05'], backend_failures: ['rate<0.05'] },
};

export function setup() {
  const response = http.post(`${baseUrl}/api/v1/auth/login`, JSON.stringify({ email, senha: password }), { headers: { 'Content-Type': 'application/json' } });
  check(response, { 'login autorizado': (r) => r.status === 200 });
  if (response.status !== 200) throw new Error(`login falhou: ${response.status}`);
  return { token: response.json('accessToken') };
}

export default function (data) {
  const headers = { headers: { Authorization: `Bearer ${data.token}` } };
  const period = 'inicio=2026-01-01T00:00:00&fim=2026-01-31T23:59:59';
  const requests = [
    ['dashboard', `${baseUrl}/api/v1/empresas/${companyId}/analytics/resumo?${period}`],
    ['setores', `${baseUrl}/api/v1/empresas/${companyId}/analytics/setores?${period}&page=0&size=20`],
    ['funcionario', `${baseUrl}/api/v1/empresas/${companyId}/analytics/funcionarios/${employeeId}?${period}`],
    ['producao', `${baseUrl}/api/v1/empresas/${companyId}/producoes?setorId=${sectorId}&page=0&size=20`],
  ];
  for (const [name, url] of requests) {
    const response = http.get(url, headers);
    const ok = check(response, { [`${name} sem erro 5xx`]: (r) => r.status < 500 });
    failures.add(!ok);
    responseBytes.add(Number(response.body ? response.body.length : 0));
  }
  sleep(1);
}
