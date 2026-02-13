import dayjs from 'dayjs';
import { upcomingBaseRoute } from './upcoming-product-routes';
import { parsePath, renderPath } from 'typesafe-routes';

describe.only('upcomingBaseRoute', () => {
  describe('generate', () => {
    it('by place and date', () => {
      const date = dayjs('2025/12/12', 'YYYY/MM/DD');
      const locale = 'de';
      const url = renderPath(upcomingBaseRoute.events.countryCode.region.place.dateTime, {
        countryCode: '1234',
        region: 'AR',
        place: 'Place',
        year: parseInt(date.locale(locale).format('YYYY')),
        month: parseInt(date.locale(locale).format('MM')),
        day: parseInt(date.locale(locale).format('DD')),
      });
      expect(url).toEqual('/events/in/1234/AR/Place/am/2025/12/12');
    });
  });

  describe('parse', () => {
    it('by place and date', () => {
      const url = '/events/in/CH/ZH/Thalwil/am/2025/9/30/innerhalb/10Km';

      const { year, month, day } = parsePath(
        upcomingBaseRoute.events.countryCode.region.place.dateTime,
        url
      );

      expect(year).toEqual(2025);
      expect(month).toEqual(9);
      expect(day).toEqual(30);
    });
  });
});
