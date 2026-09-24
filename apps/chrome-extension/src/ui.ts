export function requiredElement<T extends Element>(selector: string): T {
  const element = document.querySelector<T>(selector);
  if (!element) throw new Error(`Missing element: ${selector}`);
  return element;
}

export function showMessage(element: HTMLElement, message: string, kind: "success" | "error" | "neutral" = "neutral") {
  element.textContent = message;
  element.dataset.kind = kind;
}

export function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : "發生未知錯誤";
}
