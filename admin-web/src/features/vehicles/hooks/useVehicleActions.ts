import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  changeListingStatus,
  changeVehicleStatus,
  type ListingStatusAction,
  type VehicleStatusAction,
} from "../api/vehicleAPI";
import type { VehicleAction } from "../components/VehicleActionMenu";

// Cache invalidation strategy: invalidate the paged list + the specific detail key
// after any mutation. We do NOT optimistic-update because backend may reject a
// transition (e.g. publish without image) and the server response is authoritative.
export function useVehicleActions(vehicleId: string | null) {
  const queryClient = useQueryClient();

  function invalidateAll() {
    queryClient.invalidateQueries({ queryKey: ["vehicles"] });
  }

  const vehicleStatusMutation = useMutation({
    mutationFn: (action: VehicleStatusAction) =>
      changeVehicleStatus(vehicleId!, action),
    onSuccess: invalidateAll,
  });

  const listingStatusMutation = useMutation({
    mutationFn: (action: ListingStatusAction) =>
      changeListingStatus(vehicleId!, action),
    onSuccess: invalidateAll,
  });

  function dispatch(action: VehicleAction) {
    if (action.kind === "vehicle") {
      vehicleStatusMutation.mutate(action.action);
    } else {
      listingStatusMutation.mutate(action.action);
    }
  }

  const isPending =
    vehicleStatusMutation.isPending || listingStatusMutation.isPending;
  const error = vehicleStatusMutation.error ?? listingStatusMutation.error;

  return { dispatch, isPending, error };
}
