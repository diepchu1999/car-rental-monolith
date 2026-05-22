export function getDriverInitials(name: string): string {
  return name
    .split(" ")
    .map((word) => word[0])
    .slice(-2)
    .join("");
}
