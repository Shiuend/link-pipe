import { writeFile } from "node:fs/promises";

export async function saveLocalCopy(
  outputPath: string,
  value: unknown
): Promise<string> {
  const json = `${JSON.stringify(value, null, 2)}\n`;
  await writeFile(outputPath, json, "utf8");
  return json;
}
