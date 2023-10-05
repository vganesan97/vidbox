import '@/styles/globals.css'
import "../styles/globals2.css";
import React from "react";
import type { AppProps } from "next/app";


export default function App({ Component, pageProps }: AppProps) {
  return (
      <Component {...pageProps} />
  );
}
