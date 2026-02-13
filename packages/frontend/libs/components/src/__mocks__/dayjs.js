// Mock for dayjs - used when Jest resolves 'dayjs'
/* eslint-disable @typescript-eslint/no-empty-function */
const chain = {
  toNow: () => '',
  format: () => '',
  subtract: () => chain,
};
function fn() {
  return chain;
}
fn.extend = function () {};
fn.plugin = function () {};
module.exports = fn;
module.exports.default = fn;
