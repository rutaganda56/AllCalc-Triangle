const calculationNumberFormat = new Intl.NumberFormat("en", {
  maximumFractionDigits: 3,
});

export function formatNumber(value: number) {
  return calculationNumberFormat.format(value);
}

export function formatCalculationText(value: string) {
  return value.replace(/-?\d+(?:\.\d+)?(?:e[+-]?\d+)?/gi, (number) => {
    if (!/[.e]/i.test(number)) return number;
    const parsed = Number(number);
    return Number.isFinite(parsed) ? formatNumber(parsed) : number;
  });
}

export function formatPoint(point: { x: number; y: number }) {
  return `(${formatNumber(point.x)}, ${formatNumber(point.y)})`;
}
