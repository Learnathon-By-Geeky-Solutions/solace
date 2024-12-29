import { Icons } from "../../lib/icons";

export function AIRecommendations() {
  return (
    <div className="mt-8">
      <h2 className="text-2xl font-bold text-gray-900 mb-6">
        AI Recommendations
      </h2>
      <div className="bg-white shadow rounded-lg p-6">
        <p className="text-gray-600">
          Based on your location and current weather conditions, here are some
          recommended plants for your balcony:
        </p>
        <ul className="mt-4 space-y-4">
          <li className="flex items-start">
            <span className="flex-shrink-0 h-6 w-6 rounded-full bg-green-100 flex items-center justify-center">
              <Icons.Plant className="h-4 w-4 text-green-600" />
            </span>
            <div className="ml-3">
              <p className="text-sm font-medium text-gray-900">
                Herbs: Basil, Mint, Rosemary
              </p>
              <p className="mt-1 text-sm text-gray-500">
                Perfect for your sunny balcony spot
              </p>
            </div>
          </li>
          <li className="flex items-start">
            <span className="flex-shrink-0 h-6 w-6 rounded-full bg-green-100 flex items-center justify-center">
              <Icons.Plant className="h-4 w-4 text-green-600" />
            </span>
            <div className="ml-3">
              <p className="text-sm font-medium text-gray-900">
                Cherry Tomatoes
              </p>
              <p className="mt-1 text-sm text-gray-500">
                Ideal for container growing
              </p>
            </div>
          </li>
        </ul>
      </div>
    </div>
  );
}
