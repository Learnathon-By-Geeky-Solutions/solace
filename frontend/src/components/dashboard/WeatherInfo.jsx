import { Icons } from "../../lib/icons";

export function WeatherInfo() {
  return (
    <div className="bg-white overflow-hidden shadow rounded-lg">
      <div className="p-6">
        <div className="flex items-center">
          <div className="flex-shrink-0">
            <Icons.Cloud className="h-6 w-6 text-blue-600" />
          </div>
          <div className="ml-4">
            <h3 className="text-lg font-medium text-gray-900">
              Weather Conditions
            </h3>
            <p className="mt-1 text-sm text-gray-500">
              Current weather and forecast
            </p>
          </div>
        </div>
        <div className="mt-4 space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <Icons.Thermometer className="h-5 w-5 text-gray-400" />
              <span className="ml-2 text-sm text-gray-500">Temperature</span>
            </div>
            <span className="text-sm text-gray-900">24°C</span>
          </div>
          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <Icons.Sun className="h-5 w-5 text-gray-400" />
              <span className="ml-2 text-sm text-gray-500">Sunlight</span>
            </div>
            <span className="text-sm text-gray-900">Moderate</span>
          </div>
        </div>
      </div>
    </div>
  );
}
