import { toErrorMessage } from './error-message';

describe('toErrorMessage', () => {
  it('uses the message of an error', () => {
    expect(toErrorMessage(new Error('ERR_CONNECTION_RESET'))).toEqual(
      'ERR_CONNECTION_RESET',
    );
  });

  it('uses a rejected string as is', () => {
    expect(toErrorMessage('timeout exceeded')).toEqual('timeout exceeded');
  });

  it('names the error type when there is no message', () => {
    expect(toErrorMessage(new TypeError(''))).toEqual('TypeError');
  });

  it('never returns a blank message', () => {
    expect(toErrorMessage(undefined)).toEqual('unknown error');
    expect(toErrorMessage('  ')).toEqual('unknown error');
  });
});
