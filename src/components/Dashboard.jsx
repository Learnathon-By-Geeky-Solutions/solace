import { GardenOverview } from "./dashboard/GardenOverview";
import { WeatherInfo } from "./dashboard/WeatherInfo";
import { PlantingCalendar } from "./dashboard/PlantingCalendar";
import { AIRecommendations } from "./dashboard/AIRecommendations";
import { useAuth } from "../contexts/AuthContext"; // Add this import

export function Dashboard() {
  const { signOut } = useAuth();
  const handleSignout = async () => {
    try {
      await signOut();
      // alert("Sign out clicked");
    } catch (error) {
      console.error("Sign out error:", error);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 flex justify-between items-center">
          <h1 className="text-3xl font-bold text-gray-900">
            My Balcony Garden
          </h1>
          <button
            onClick={handleSignout}
            className="bg-red-600 hover:bg-red-700 text-white font-medium py-2 px-4 rounded-md"
          >
            Sign Out
          </button>
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
