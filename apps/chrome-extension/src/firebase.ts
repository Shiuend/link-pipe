import { initializeApp } from "firebase/app";
import { getAuth, GoogleAuthProvider, signInWithCredential, signOut, type User } from "firebase/auth/web-extension";
import {
  collection,
  doc,
  getDocs,
  getFirestore,
  runTransaction,
  serverTimestamp,
  setDoc,
} from "firebase/firestore";
import { bookmarkWrite, normalizeUrl, sha256, type BookmarkRecord, type CaptureSource } from "./bookmark";
import { firebaseConfig, isConfigured } from "./config";

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const firestore = getFirestore(app);

export async function currentUser(): Promise<User | null> {
  await auth.authStateReady();
  return auth.currentUser;
}

export async function signInWithGoogle(): Promise<User> {
  if (!isConfigured()) throw new Error("尚未設定 Firebase，請先參考 README 完成設定");
  const token = await chrome.identity.getAuthToken({ interactive: true });
  if (!token.token) throw new Error("Google 登入未傳回存取權杖");
  const credential = GoogleAuthProvider.credential(null, token.token);
  return (await signInWithCredential(auth, credential)).user;
}

export async function signOutEverywhere(): Promise<void> {
  const token = await chrome.identity.getAuthToken({ interactive: false }).catch(() => null);
  if (token?.token) await chrome.identity.removeCachedAuthToken({ token: token.token });
  await signOut(auth);
}

function bookmarkRef(uid: string, hash: string) {
  return doc(firestore, "users", uid, "bookmarks", hash);
}

export async function saveBookmark(rawUrl: string, note: string, source: CaptureSource): Promise<string> {
  const user = await currentUser();
  if (!user) throw new Error("請先使用 Google 帳號登入");
  const url = normalizeUrl(rawUrl);
  const hash = await sha256(url);
  const reference = bookmarkRef(user.uid, hash);

  await runTransaction(firestore, async (transaction) => {
    const existing = await transaction.get(reference);
    const payload: Record<string, unknown> = {
      ...bookmarkWrite(url, hash, note.trim(), source),
      updatedAt: serverTimestamp(),
    };
    if (!existing.exists()) {
      payload.createdAt = serverTimestamp();
    } else {
      delete payload.createdBy;
      if (!note.trim()) delete payload.note;
    }
    transaction.set(reference, payload, { merge: true });
  });
  return hash;
}

export async function listBookmarks(): Promise<BookmarkRecord[]> {
  const user = await currentUser();
  if (!user) throw new Error("請先使用 Google 帳號登入");
  const snapshot = await getDocs(collection(firestore, "users", user.uid, "bookmarks"));
  return snapshot.docs
    .map((item) => ({ ...item.data(), id: item.id, urlHash: item.data().urlHash || item.id }) as BookmarkRecord)
    .filter((item) => !item.hidden)
    .sort((left, right) => timestampMillis(right.updatedAt ?? right.createdAt) - timestampMillis(left.updatedAt ?? left.createdAt));
}

function timestampMillis(value: unknown): number {
  if (value && typeof value === "object" && "toMillis" in value && typeof value.toMillis === "function") {
    return value.toMillis();
  }
  return 0;
}

export async function updateNote(hash: string, note: string): Promise<void> {
  const user = await currentUser();
  if (!user) throw new Error("請先使用 Google 帳號登入");
  await setDoc(bookmarkRef(user.uid, hash), { note: note.trim(), updatedAt: serverTimestamp() }, { merge: true });
}
