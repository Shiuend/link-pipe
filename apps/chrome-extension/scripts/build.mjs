import { build } from "esbuild";
import { cp, mkdir, readFile, rm, writeFile } from "node:fs/promises";
import { resolve } from "node:path";

const root = resolve(import.meta.dirname, "..");
const output = resolve(root, "dist");
const env = process.env;
const firebaseConfig = {
  apiKey: env.LINKPIPE_FIREBASE_API_KEY || "",
  authDomain: env.LINKPIPE_FIREBASE_AUTH_DOMAIN || "",
  projectId: env.LINKPIPE_FIREBASE_PROJECT_ID || "",
  appId: env.LINKPIPE_FIREBASE_APP_ID || "",
};
const oauthClientId = env.LINKPIPE_GOOGLE_OAUTH_CLIENT_ID || "000000000000-configure-me.apps.googleusercontent.com";

await rm(output, { recursive: true, force: true });
await mkdir(output, { recursive: true });
await build({
  entryPoints: {
    background: resolve(root, "src/background.ts"),
    popup: resolve(root, "src/popup.ts"),
    capture: resolve(root, "src/capture.ts"),
    library: resolve(root, "src/library.ts"),
  },
  outdir: output,
  bundle: true,
  format: "esm",
  target: "chrome120",
  define: {
    __LINKPIPE_FIREBASE_CONFIG__: JSON.stringify(firebaseConfig),
    __LINKPIPE_OAUTH_CONFIGURED__: JSON.stringify(Boolean(env.LINKPIPE_GOOGLE_OAUTH_CLIENT_ID)),
  },
});

await cp(resolve(root, "public"), output, { recursive: true });
const template = await readFile(resolve(root, "manifest.template.json"), "utf8");
const manifest = JSON.parse(template.replace("__GOOGLE_OAUTH_CLIENT_ID__", oauthClientId));
if (env.LINKPIPE_EXTENSION_KEY) {
  manifest.key = env.LINKPIPE_EXTENSION_KEY;
}
await writeFile(resolve(output, "manifest.json"), `${JSON.stringify(manifest, null, 2)}\n`);
