import http from "k6/http";

import { sleep } from "k6";

// k6 run --vus 400 --duration 60s performance-test.js
export default function () {

  http.get('http://localhost:8080/api/w2f?v=0.1&url=http%3A%2F%2Fheise.de&link=.%2Fa%5B1%5D&context=%2F%2Fdiv%5B5%5D%2Fdiv%5B1%5D%2Fdiv%2Fsection%5B1%5D%2Fsection%5B1%5D%2Farticle&re=none&out=json&token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0eXBlIjoiYW5vbnltb3VzIiwiaWF0IjoxNjU1MjM5MjA0fQ.Xm9IgmruVxjxwx-3-A0pMUME0ZD5PddMvqOqq5XB_aY');

  sleep(1);

}
