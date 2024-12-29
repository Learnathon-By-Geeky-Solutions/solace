import { Icons } from "../../lib/icons";

export function PlantingCalendar() {
  return (
    <div className="bg-white overflow-hidden shadow rounded-lg">
      <div className="p-6">
        <div className="flex items-center">
          <div className="flex-shrink-0">
            <Icons.Calendar className="h-6 w-6 text-purple-600" />
          </div>
          <div className="ml-4">
            <h3 className="text-lg font-medium text-gray-900">
              Planting Calendar
            </h3>
            <p className="mt-1 text-sm text-gray-500">
              Upcoming tasks and seasons
            </p>
          </div>
        </div>
        <div className="mt-4">
          <div className="space-y-4">
            <div className="border-l-4 border-green-400 pl-4">
              <p className="text-sm font-medium text-gray-900">March - April</p>
              <p className="mt-1 text-sm text-gray-500">
                Perfect time for herb planting
              </p>
            </div>
            <div className="border-l-4 border-yellow-400 pl-4">
              <p className="text-sm font-medium text-gray-900">May - June</p>
              <p className="mt-1 text-sm text-gray-500">
                Summer vegetables season
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
