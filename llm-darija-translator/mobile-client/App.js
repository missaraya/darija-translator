/**
 * App.js — Root component for the Darija Translator mobile app.
 *
 * Single-screen app — no navigation stack needed.
 * SafeAreaProvider wraps the screen to handle notches and status bars.
 */

import React from "react";
import { SafeAreaProvider } from "react-native-safe-area-context";
import TranslatorScreen from "./src/screens/TranslatorScreen";

export default function App() {
  return (
    <SafeAreaProvider>
      <TranslatorScreen />
    </SafeAreaProvider>
  );
}
