import { Route, Routes } from 'react-router-dom';

import { Layout } from './components/Layout';
import { useApiToken } from './auth/useApiToken';
import { LandingPage } from './pages/LandingPage';
import { RestaurantListPage } from './pages/RestaurantListPage';
import { RestaurantDetailPage } from './pages/RestaurantDetailPage';
import { OrderTrackingPage } from './pages/OrderTrackingPage';
import { NotFoundPage } from './pages/NotFoundPage';

export default function App() {
  useApiToken();

  return (
    <Layout>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/restaurants" element={<RestaurantListPage />} />
        <Route path="/restaurants/:restaurantId" element={<RestaurantDetailPage />} />
        <Route path="/orders/:orderId" element={<OrderTrackingPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Layout>
  );
}
