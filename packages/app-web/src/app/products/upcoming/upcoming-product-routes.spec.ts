import dayjs from 'dayjs';
import { upcomingBaseRoute } from './upcoming-product-routes';

describe.only('upcomingBaseRoute', () => {
  describe('generate', () => {
    it('by place and date', () => {
      const date = dayjs('2025/12/12', 'YYYY/MM/DD');
      const locale = 'de';
      const url = upcomingBaseRoute({})
        .events({})
        .countryCode({ countryCode: '1234' })
        .region({
          region: 'AR',
        })
        .place({
          place: 'Place',
        })
        .dateTime({
          year: parseInt(date.locale(locale).format('YYYY')),
          month: parseInt(date.locale(locale).format('MM')),
          day: parseInt(date.locale(locale).format('DD')),
        })
        .perimeter({
          perimeter: 10,
        }).$;
      expect(url).toEqual(
        'events/in/1234/AR/Place/am/2025/12/12/innerhalb/10Km"',
      );
    });
  });

  // describe('parse', () => {
  //   it('by place and date', () => {
  //     const date = dayjs('2025/12/12', 'YYYY/MM/DD');
  //     const locale = 'de';
  //
  //     const url = 'events/in/CH/ZH/Thalwil/am/2025/9/30/innerhalb/10Km';
  //
  //     parsePath();
  //     parseDateFromUrl(url);
  //     parseLocationFromUrl(url);
  //
  //     expect(url).toEqual(
  //       'events/in/1234/AR/Place/am/2025/12/12/innerhalb/10Km"',
  //     );
  //   });
  // });
});
