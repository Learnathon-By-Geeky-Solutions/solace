import { GardenOverview } from './dashboard/GardenOverview';
import { WeatherInfo } from './dashboard/WeatherInfo';
import { PlantingCalendar } from './dashboard/PlantingCalendar';
import { AIRecommendations } from './dashboard/AIRecommendations';

export function Dashboard() {
  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <h1 className="text-3xl font-bold text-gray-900">My Balcony Garden</h1>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          <GardenOverview />
          <WeatherInfo />
          <PlantingCalendar />
        </div>
        <AIRecommendations />
      </main>
    </div>
  );
}