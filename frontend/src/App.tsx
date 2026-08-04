import { Navigate, Route, Routes } from 'react-router-dom';

import { Layout } from './components/Layout';
import { useApiToken } from './auth/useApiToken';
import { RestaurantListPage } from './pages/RestaurantListPage';
import { RestaurantDetailPage } from './pages/RestaurantDetailPage';
import { NotFoundPage } from './pages/NotFoundPage';

export default function App() {
  useApiToken();

  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/restaurants" replace />} />
        <Route path="/restaurants" element={<RestaurantListPage />} />
        <Route path="/restaurants/:restaurantId" element={<RestaurantDetailPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Layout>
  );
}
