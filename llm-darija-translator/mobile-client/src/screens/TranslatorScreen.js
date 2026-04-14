/**
 * TranslatorScreen.js — Main (and only) screen for the Darija Translator app.
 *
 * Features:
 *  - Language chip selectors (source and target)
 *  - Multiline text input with character counter
 *  - Translate button with loading spinner
 *  - Translation result display
 *  - Error display
 *  - Copy to clipboard (expo-clipboard)
 *  - Backend status indicator
 */

import React, { useState, useEffect, useCallback } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Platform,
  StatusBar,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import * as Clipboard from "expo-clipboard";
import { translate, checkHealth } from "../api/translatorApi";
import Config from "../config/config";

// ── Constants ─────────────────────────────────────────────────────────────
const SOURCE_LANGUAGES = ["English", "French", "Spanish", "Arabic", "German"];
const TARGET_LANGUAGES = ["Moroccan Darija", "English", "French", "Spanish", "Arabic"];
const MAX_TEXT_LENGTH  = 5000;
const BLUE             = "#2b6cb0";

// ── Component ─────────────────────────────────────────────────────────────
export default function TranslatorScreen() {
  const [inputText,      setInputText]      = useState("");
  const [result,         setResult]         = useState(null);
  const [isLoading,      setIsLoading]      = useState(false);
  const [error,          setError]          = useState(null);
  const [sourceLanguage, setSourceLanguage] = useState("English");
  const [targetLanguage, setTargetLanguage] = useState("Moroccan Darija");
  const [backendStatus,  setBackendStatus]  = useState("checking"); // "checking" | "up" | "down"

  // Check backend health once on mount
  useEffect(() => {
    checkHealth().then((up) => setBackendStatus(up ? "up" : "down"));
  }, []);

  const handleTranslate = useCallback(async () => {
    if (!inputText.trim()) {
      setError("Please enter some text to translate.");
      return;
    }
    setIsLoading(true);
    setError(null);
    setResult(null);
    try {
      const data = await translate(inputText.trim(), sourceLanguage, targetLanguage);
      setResult(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsLoading(false);
    }
  }, [inputText, sourceLanguage, targetLanguage]);

  const handleCopy = useCallback(async () => {
    if (!result?.translatedText) return;
    await Clipboard.setStringAsync(result.translatedText);
    Alert.alert("Copied!", "Translation copied to clipboard.");
  }, [result]);

  const handleClear = useCallback(() => {
    setInputText("");
    setResult(null);
    setError(null);
  }, []);

  // ── Render ───────────────────────────────────────────────────────────────
  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="light-content" backgroundColor={BLUE} />

      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === "ios" ? "padding" : "height"}
      >
        <ScrollView
          style={styles.scroll}
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled"
        >

          {/* ── Header ──────────────────────────────── */}
          <View style={styles.header}>
            <View style={styles.headerLeft}>
              <Text style={styles.headerTitle}>🌙 Darija Translator</Text>
              <Text style={styles.headerSubtitle}>Powered by Google Gemini AI</Text>
            </View>
            {/* Backend status indicator dot */}
            <View style={[
              styles.statusDot,
              backendStatus === "up"       ? styles.statusDotUp
              : backendStatus === "down"   ? styles.statusDotDown
              : styles.statusDotChecking
            ]} />
          </View>

          {/* ── Backend offline warning ──────────────── */}
          {backendStatus === "down" && (
            <View style={styles.warningBanner}>
              <Text style={styles.warningText}>
                ⚠ Backend is offline.{"\n"}
                Start Payara Micro, then check:{"\n"}
                {Config.BACKEND_URL}
              </Text>
            </View>
          )}

          {/* ── Source Language ──────────────────────── */}
          <View style={styles.card}>
            <Text style={styles.label}>From</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false}
                        contentContainerStyle={styles.chipRow}>
              {SOURCE_LANGUAGES.map((lang) => (
                <TouchableOpacity
                  key={lang}
                  style={[styles.chip, sourceLanguage === lang && styles.chipActive]}
                  onPress={() => setSourceLanguage(lang)}
                >
                  <Text style={[styles.chipText, sourceLanguage === lang && styles.chipTextActive]}>
                    {lang}
                  </Text>
                </TouchableOpacity>
              ))}
            </ScrollView>

            <Text style={[styles.label, { marginTop: 14 }]}>To</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false}
                        contentContainerStyle={styles.chipRow}>
              {TARGET_LANGUAGES.map((lang) => (
                <TouchableOpacity
                  key={lang}
                  style={[styles.chip, targetLanguage === lang && styles.chipActive]}
                  onPress={() => setTargetLanguage(lang)}
                >
                  <Text style={[styles.chipText, targetLanguage === lang && styles.chipTextActive]}>
                    {lang}
                  </Text>
                </TouchableOpacity>
              ))}
            </ScrollView>
          </View>

          {/* ── Text Input ───────────────────────────── */}
          <View style={styles.card}>
            <Text style={styles.label}>Text to Translate</Text>
            <TextInput
              style={styles.textInput}
              value={inputText}
              onChangeText={setInputText}
              placeholder={`Enter ${sourceLanguage} text here…`}
              placeholderTextColor="#a0aec0"
              multiline
              textAlignVertical="top"
              maxLength={MAX_TEXT_LENGTH}
            />
            <Text style={styles.charCount}>{inputText.length} / {MAX_TEXT_LENGTH}</Text>

            <View style={styles.buttonRow}>
              <TouchableOpacity
                style={[styles.btnTranslate,
                  (isLoading || backendStatus === "down") && styles.btnDisabled]}
                onPress={handleTranslate}
                disabled={isLoading || backendStatus === "down"}
                activeOpacity={0.8}
              >
                {isLoading
                  ? <ActivityIndicator color="#fff" />
                  : <Text style={styles.btnTranslateText}>Translate →</Text>
                }
              </TouchableOpacity>

              <TouchableOpacity style={styles.btnClear} onPress={handleClear}
                                activeOpacity={0.7}>
                <Text style={styles.btnClearText}>✕</Text>
              </TouchableOpacity>
            </View>
          </View>

          {/* ── Error ────────────────────────────────── */}
          {!!error && !isLoading && (
            <View style={styles.errorBox}>
              <Text style={styles.errorText}>⚠ {error}</Text>
            </View>
          )}

          {/* ── Result ───────────────────────────────── */}
          {!!result && !isLoading && (
            <View style={styles.resultCard}>
              <View style={styles.resultHeader}>
                <Text style={styles.resultLabel}>
                  Translation ({result.targetLanguage})
                </Text>
                <View style={styles.providerBadge}>
                  <Text style={styles.providerText}>{result.provider}</Text>
                </View>
              </View>

              <Text style={styles.resultText}>{result.translatedText}</Text>

              <TouchableOpacity style={styles.copyBtn} onPress={handleCopy}
                                activeOpacity={0.7}>
                <Text style={styles.copyBtnText}>📋 Copy</Text>
              </TouchableOpacity>
            </View>
          )}

        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

// ── Styles ────────────────────────────────────────────────────────────────
const styles = StyleSheet.create({
  safeArea:    { flex: 1, backgroundColor: BLUE },
  flex:        { flex: 1, backgroundColor: "#f8f9fa" },
  scroll:      { flex: 1 },
  scrollContent: { paddingBottom: 40 },

  // Header
  header: {
    backgroundColor: BLUE,
    paddingTop: 16,
    paddingBottom: 20,
    paddingHorizontal: 20,
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
  },
  headerLeft:     { flex: 1 },
  headerTitle:    { color: "#fff", fontSize: 22, fontWeight: "800" },
  headerSubtitle: { color: "rgba(255,255,255,0.8)", fontSize: 12, marginTop: 3 },

  statusDot: {
    width: 12, height: 12, borderRadius: 6,
    marginLeft: 12,
  },
  statusDotUp:       { backgroundColor: "#68d391" },
  statusDotDown:     { backgroundColor: "#fc8181" },
  statusDotChecking: { backgroundColor: "#fbd38d" },

  // Warning banner
  warningBanner: {
    backgroundColor: "#fffbeb",
    borderLeftWidth: 4,
    borderLeftColor: "#f6ad55",
    margin: 12,
    padding: 12,
    borderRadius: 8,
  },
  warningText: { color: "#744210", fontSize: 13, lineHeight: 20 },

  // Cards
  card: {
    backgroundColor: "#fff",
    margin: 12,
    marginBottom: 0,
    borderRadius: 12,
    padding: 16,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 6,
    elevation: 3,
  },

  label: {
    fontSize: 11,
    fontWeight: "700",
    textTransform: "uppercase",
    letterSpacing: 0.5,
    color: "#4a5568",
    marginBottom: 8,
  },

  // Language chips
  chipRow: { flexDirection: "row", paddingBottom: 2 },
  chip: {
    paddingHorizontal: 14, paddingVertical: 7,
    borderRadius: 999, borderWidth: 1.5, borderColor: "#e2e8f0",
    marginRight: 8, backgroundColor: "#fff",
  },
  chipActive:     { backgroundColor: BLUE, borderColor: BLUE },
  chipText:       { fontSize: 13, color: "#4a5568", fontWeight: "500" },
  chipTextActive: { color: "#fff", fontWeight: "700" },

  // Text input
  textInput: {
    minHeight: 120, borderWidth: 1.5, borderColor: "#e2e8f0",
    borderRadius: 8, padding: 12, fontSize: 15, color: "#2d3748",
    lineHeight: 22,
  },
  charCount: { textAlign: "right", fontSize: 11, color: "#a0aec0", marginTop: 4 },

  // Buttons
  buttonRow: { flexDirection: "row", marginTop: 12, gap: 10 },
  btnTranslate: {
    flex: 1, backgroundColor: BLUE, paddingVertical: 13,
    borderRadius: 8, alignItems: "center", justifyContent: "center",
  },
  btnTranslateText: { color: "#fff", fontSize: 16, fontWeight: "700" },
  btnDisabled:      { opacity: 0.5 },
  btnClear: {
    width: 48, height: 48, borderRadius: 8,
    borderWidth: 1.5, borderColor: "#e2e8f0",
    alignItems: "center", justifyContent: "center",
  },
  btnClearText: { fontSize: 16, color: "#718096" },

  // Error
  errorBox: {
    margin: 12, padding: 14,
    backgroundColor: "#fff5f5", borderRadius: 8,
    borderWidth: 1.5, borderColor: "#feb2b2",
  },
  errorText: { color: "#c53030", fontSize: 13, lineHeight: 20 },

  // Result
  resultCard: {
    margin: 12, padding: 16,
    backgroundColor: "#f0fff4", borderRadius: 12,
    borderWidth: 1.5, borderColor: "#9ae6b4",
  },
  resultHeader: {
    flexDirection: "row", alignItems: "center",
    justifyContent: "space-between", marginBottom: 12,
  },
  resultLabel:   { fontSize: 11, fontWeight: "800", textTransform: "uppercase",
                   letterSpacing: 0.5, color: "#276749" },
  providerBadge: { backgroundColor: "#c6f6d5", paddingHorizontal: 10,
                   paddingVertical: 3, borderRadius: 999 },
  providerText:  { fontSize: 11, color: "#22543d", fontWeight: "700" },
  resultText:    { fontSize: 18, color: "#22543d", lineHeight: 28,
                   fontWeight: "500", marginBottom: 14 },
  copyBtn: {
    borderWidth: 1.5, borderColor: "#9ae6b4", borderRadius: 6,
    paddingHorizontal: 14, paddingVertical: 7, alignSelf: "flex-start",
  },
  copyBtnText: { color: "#276749", fontSize: 13, fontWeight: "600" },
});
