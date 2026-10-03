import { ApiRequestError } from './error.interceptor';

describe('ApiRequestError', () => {
  it('keeps status and correlation id without exposing secrets', () => {
    const error = new ApiRequestError('Acesso não autorizado para esta operação. [corr-1]', 403, 'corr-1');
    expect(error.status).toBe(403);
    expect(error.correlationId).toBe('corr-1');
    expect(error.message).not.toContain('token');
    expect(error.message).not.toContain('senha');
  });
});
