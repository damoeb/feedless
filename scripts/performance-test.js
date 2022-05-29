import http from "k6/http";

import { sleep } from "k6";


export default function () {

  http.get('http://localhost:8080/api/web-to-feed/atom?version=0.1&url=http%3A%2F%2Fheise.de&linkXPath=.%2Fa%5B1%5D&extendContext=&contextXPath=%2F%2Fdiv%5B6%5D%2Fdiv%5B32%5D%2Fsection%5B1%5D%2Fdiv%2Fsection%5B1%5D%2Farticle&recovery=NONE&filter=');

  sleep(1);

}
