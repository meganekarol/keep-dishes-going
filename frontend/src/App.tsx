import { Route, Routes } from 'react-router-dom';

import { Layout } from './components/layout/Layout';
import { RequireOwner } from './components/auth/RequireOwner';
import { LandingPage } from './pages/LandingPage';
import { RestaurantListPage } from './pages/RestaurantListPage';
import { RestaurantDetailPage } from './pages/RestaurantDetailPage';
import { OrderTrackingPage } from './pages/OrderTrackingPage';
import { OwnerDashboardPage } from './pages/owner/OwnerDashboardPage';
import { CreateRestaurantPage } from './pages/owner/CreateRestaurantPage';
import { NotFoundPage } from './pages/NotFoundPage';

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<LandingPage />} />

        {/* Open to everyone: customers order without an account (US-30). */}
        <Route path="/restaurants" element={<RestaurantListPage />} />
        <Route path="/restaurants/:restaurantId" element={<RestaurantDetailPage />} />
        <Route path="/orders/:orderId" element={<OrderTrackingPage />} />

        <Route
          path="/owner"
          element={
            <RequireOwner>
              <OwnerDashboardPage />
            </RequireOwner>
          }
        />
        <Route
          path="/owner/restaurant/new"
          element={
            <RequireOwner>
              <CreateRestaurantPage />
            </RequireOwner>
          }
        />

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Layout>
  );
}