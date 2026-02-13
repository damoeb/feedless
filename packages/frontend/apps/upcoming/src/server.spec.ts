import { checkOutdated } from './server-utils';
import dayjs, { Dayjs } from 'dayjs';
import { times } from 'lodash-es';

describe('server', () => {
  let now: Dayjs;
  beforeEach(() => {
    now = dayjs();
  });
  it('redirects URLs older than 7 days', () => {
    const outdatedDate = now.subtract(8, 'days');
    const path = `/events/in/CH/ZH/Thalwil/am/${outdatedDate.year()}/${outdatedDate.month()}/${outdatedDate.day()}`;
    const result = checkOutdated(path);

    expect(result.outdated).toBe(true);
    if (result.outdated) {
      expect(result.params).toBeDefined();
      expect(result.params).toEqual({
        countryCode: 'CH',
        region: 'ZH',
        place: 'Thalwil',
        year: outdatedDate.year(),
        month: outdatedDate.month(),
        day: outdatedDate.day(),
      });
    }
  });

  times(1)
    .map((offset) => 6 - offset)
    .forEach((offset) => {
      fit(`does not redirect URLs younger than ${offset} days`, () => {
        const nonOutdatedDate = now.add(offset, 'days');
        const path = `/events/in/CH/ZH/Thalwil/am/${nonOutdatedDate.year()}/${nonOutdatedDate.month()}/${nonOutdatedDate.day()}`;

        const result = checkOutdated(path);

        expect(result.outdated).toBe(false);
        if (result.outdated) {
          expect(result.params).toBeDefined();
          expect(result.params).toEqual({
            countryCode: 'CH',
            region: 'ZH',
            place: 'Thalwil',
            year: nonOutdatedDate.year(),
            month: nonOutdatedDate.month(),
            day: nonOutdatedDate.day(),
          });
        }
      });
    });
});
