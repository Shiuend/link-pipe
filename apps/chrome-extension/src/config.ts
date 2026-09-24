import type { FirebaseOptions } from "firebase/app";

declare const __LINKPIPE_FIREBASE_CONFIG__: FirebaseOptions;
declare const __LINKPIPE_OAUTH_CONFIGURED__: boolean;

export const firebaseConfig = __LINKPIPE_FIREBASE_CONFIG__;

export function isConfigured(): boolean {
  return Boolean(
    __LINKPIPE_OAUTH_CONFIGURED__ &&
      firebaseConfig.apiKey &&
      firebaseConfig.authDomain &&
      firebaseConfig.projectId &&
      firebaseConfig.appId,
  );
}
