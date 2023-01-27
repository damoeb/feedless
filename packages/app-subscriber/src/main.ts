import { getToken } from "firebase/messaging";
import { createApp } from "vue";
import App from "./App.vue";
import "./registerServiceWorker";
import router from "./router";
import store from "./store";

import { firebase } from "./firebase";

const app = createApp(App);

navigator.serviceWorker
  .getRegistration()
  .then((serviceWorkerRegistration) => {
    return getToken(firebase.messaging, {
      vapidKey:
        "BKs9vXwJlLROPhvk2e2FSRvf45hjr8CE20N16kicJa-g2x7h3dbORKHqg74UfKyMOGTIQ_AzgO07j74gYn071jA",
      serviceWorkerRegistration,
    });
  })
  .then((token) => console.log("token", token))
  .catch(console.error);

app.config.globalProperties.$messaging = firebase.messaging;
createApp(App).use(store).use(router).mount("#app");
