const ISO_DATE_TIME_RE =
  /^(\d{4})-(\d{2})-(\d{2})(?:[ T](\d{2}):(\d{2})(?::(\d{2})(?:\.(\d+))?)?)?(?:Z|([+-])(\d{2}):?(\d{2}))?$/;
const FALLBACK = "Unknown";

function isValidDate(date: Date): boolean {
  return Number.isFinite(date.getTime());
}

function parseArrayDate(parts: unknown[]): Date | null {
  if (parts.length < 3) return null;

  const numbers = parts.map((part) =>
    typeof part === "number" ? part : Number(part),
  );
  if (numbers.some((num) => Number.isNaN(num))) return null;

  const [year, month, day, hour = 0, minute = 0, second = 0, nano = 0] =
    numbers;
  if (![year, month, day].every((num) => Number.isFinite(num))) return null;

  const ms = Number.isFinite(nano) ? Math.floor(nano / 1_000_000) : 0;
  const date = new Date(year, month - 1, day, hour, minute, second, ms);
  return isValidDate(date) ? date : null;
}

function parseIsoLike(value: string): Date | null {
  const match = ISO_DATE_TIME_RE.exec(value);
  if (!match) return null;

  const [, y, m, d, hh, mm, ss, fraction, tzSign, tzHour, tzMinute] = match;
  const year = Number(y);
  const month = Number(m) - 1;
  const day = Number(d);
  const hour = Number(hh || 0);
  const minute = Number(mm || 0);
  const second = Number(ss || 0);
  const ms = fraction ? Math.floor(Number(`0.${fraction}`) * 1000) : 0;

  if (![year, month, day].every((num) => Number.isFinite(num))) return null;

  const isUtc = value.endsWith("Z");
  if (isUtc || tzSign) {
    const utc = Date.UTC(year, month, day, hour, minute, second, ms);
    if (tzSign && tzHour && tzMinute) {
      const offsetMinutes = Number(tzHour) * 60 + Number(tzMinute);
      const offsetMs = offsetMinutes * 60 * 1000;
      const direction = tzSign === "+" ? -1 : 1;
      return new Date(utc + direction * offsetMs);
    }
    return new Date(utc);
  }

  return new Date(year, month, day, hour, minute, second, ms);
}

export function parseApiDate(value: unknown): Date | null {
  if (value == null) return null;

  if (value instanceof Date) {
    return isValidDate(value) ? value : null;
  }

  if (typeof value === "number") {
    const ms = value < 10_000_000_000 ? value * 1000 : value;
    const date = new Date(ms);
    return isValidDate(date) ? date : null;
  }

  if (Array.isArray(value)) {
    return parseArrayDate(value);
  }

  if (typeof value === "string") {
    const trimmed = value.trim();
    if (!trimmed) return null;

    if (/^\d+$/.test(trimmed)) {
      return parseApiDate(Number(trimmed));
    }

    const direct = new Date(trimmed);
    if (isValidDate(direct)) return direct;

    const normalized = trimmed.includes("T")
      ? trimmed
      : trimmed.replace(" ", "T");
    if (normalized !== trimmed) {
      const normalizedDate = new Date(normalized);
      if (isValidDate(normalizedDate)) return normalizedDate;
    }

    const manual = parseIsoLike(trimmed);
    if (manual && isValidDate(manual)) return manual;
  }

  return null;
}

export function formatDate(
  value: unknown,
  options?: Intl.DateTimeFormatOptions,
  locale?: string,
): string {
  const date = parseApiDate(value);
  return date ? date.toLocaleDateString(locale, options) : FALLBACK;
}

export function formatDateTime(
  value: unknown,
  options?: Intl.DateTimeFormatOptions,
  locale?: string,
): string {
  const date = parseApiDate(value);
  return date ? date.toLocaleString(locale, options) : FALLBACK;
}
