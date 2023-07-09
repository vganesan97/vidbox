import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
    apiKey: "AIzaSyBJE4qRfhHlfAE65uaWSjZy9IY4-GmFJtE",
    authDomain: "vidbox-7d2c1.firebaseapp.com",
    projectId: "vidbox-7d2c1",
    storageBucket: "vidbox-7d2c1.appspot.com",
    messagingSenderId: "629869872334",
    appId: "1:629869872334:web:5133a4333033f732e4bc9f",
    measurementId: "G-PRNW4WCGQK"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
//export const analytics = getAnalytics(app);