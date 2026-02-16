import { checkOutdated } from './server-utils';
import dayjs, { Dayjs } from 'dayjs';
import { times } from 'lodash-es';

describe('server', () => {
  let now: Dayjs;
  beforeEach(() => {
    now = dayjs();
  });
  times(10)
    .map((offset) => 8 + offset)
    .forEach((offset) => {
      it(`redirects URLs older than ${offset} days`, () => {
        const outdatedDate = now.subtract(offset, 'days');
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
    });

  times(10)
    .map((offset) => 7 - offset)
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
