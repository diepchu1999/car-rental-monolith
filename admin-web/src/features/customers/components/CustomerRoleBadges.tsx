import { roleLabel, roleTagClassName } from "../constants";
import type { CustomerRole } from "../types";

export function CustomerRoleBadges({ roles }: { roles: CustomerRole[] }) {
  if (roles.length === 0) {
    return <span className="tag">—</span>;
  }
  return (
    <span className="customer-role-badges">
      {roles.map((role) => (
        <span key={role} className={`tag ${roleTagClassName[role]}`}>
          {roleLabel[role]}
        </span>
      ))}
    </span>
  );
}
