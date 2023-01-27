// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getMessaging } from "firebase/messaging";
import { getAnalytics } from "firebase/analytics";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
  apiKey: "AIzaSyCLKwiFY7N6g_05ZLQKXasgnPHBPQvqJt0",
  authDomain: "howabouthis-a28bd.firebaseapp.com",
  projectId: "howabouthis-a28bd",
  storageBucket: "howabouthis-a28bd.appspot.com",
  messagingSenderId: "925856737015",
  appId: "1:925856737015:web:6eaf05696cf7fa221815f2",
  measurementId: "G-GH5GFRZ5NH",
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);
const messaging = getMessaging(app);

export const firebase = { analytics, messaging };
