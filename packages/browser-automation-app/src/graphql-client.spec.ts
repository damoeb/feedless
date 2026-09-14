import { registerAgentInput } from './graphql-client';

describe('registerAgentInput', () => {
  const target = registerAgentInput;
  const agent = {
    name: 'agent',
    version: 'dev',
    connectionId: 'c1',
    email: 'admin@localhost',
  };

  it('sends the secret key when one is set', () => {
    const expected = { email: 'admin@localhost', secretKey: 'key' };

    const actual = target({ ...agent, secretKey: 'key' });

    expect(actual.secretKey).toEqual(expected);
  });

  it('omits the secret key when none is set', () => {
    const actual = target({ ...agent, secretKey: undefined });

    expect(actual).not.toHaveProperty('secretKey');
  });

  it('omits the secret key when it is blank', () => {
    const actual = target({ ...agent, secretKey: '  ' });

    expect(actual).not.toHaveProperty('secretKey');
  });
});
