import { Icons } from "../../lib/icons";

export function GardenOverview() {
  return (
    <div className="bg-white overflow-hidden shadow rounded-lg">
      <div className="p-6">
        <div className="flex items-center">
          <div className="flex-shrink-0">
            <Icons.Plant className="h-6 w-6 text-emerald-600" />
          </div>
          <div className="ml-4">
            <h3 className="text-lg font-medium text-gray-900">
              Garden Overview
            </h3>
            <p className="mt-1 text-sm text-gray-500">
              Current plants and health status
            </p>
          </div>
        </div>
        <div className="mt-4">
          <div className="border-t border-gray-200 pt-4">
            <div className="flex justify-between">
              <span className="text-sm font-medium text-gray-500">
                Active Plants
              </span>
              <span className="text-sm text-gray-900">6</span>
            </div>
            <div className="mt-2 flex justify-between">
              <span className="text-sm font-medium text-gray-500">
                Health Status
              </span>
              <span className="text-sm text-green-600">Healthy</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
