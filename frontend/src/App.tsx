import { Route, Routes } from 'react-router-dom';

import { Layout } from './components/layout/Layout';
import { RequireOwner } from './components/auth/RequireOwner';
import { LandingPage } from './pages/LandingPage';
import { RestaurantListPage } from './pages/RestaurantListPage';
import { RestaurantDetailPage } from './pages/RestaurantDetailPage';
import { BasketPage } from './pages/BasketPage';
import { CheckoutPage } from './pages/CheckoutPage';
import { MyOrdersPage } from './pages/MyOrdersPage';
import { OrderTrackingPage } from './pages/OrderTrackingPage';
import { OwnerDashboardPage } from './pages/owner/OwnerDashboardPage';
import { CreateRestaurantPage } from './pages/owner/CreateRestaurantPage';
import { MenuPage } from './pages/owner/MenuPage';
import { OwnerOrdersPage } from './pages/owner/OwnerOrdersPage';
import { NewDishPage } from './pages/owner/NewDishPage';
import { EditDishPage } from './pages/owner/EditDishPage';
import { NotFoundPage } from './pages/NotFoundPage';

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<LandingPage />} />

        {/* Open to everyone: customers order without an account (US-30). */}
        <Route path="/restaurants" element={<RestaurantListPage />} />
        <Route path="/restaurants/:restaurantId" element={<RestaurantDetailPage />} />
        <Route path="/basket" element={<BasketPage />} />
        <Route path="/checkout" element={<CheckoutPage />} />
        {/* Listed before the tracking route: "orders" would otherwise be read
            as an order id and looked up as one. */}
        <Route path="/orders" element={<MyOrdersPage />} />
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
        <Route
          path="/owner/orders"
          element={
            <RequireOwner>
              <OwnerOrdersPage />
            </RequireOwner>
          }
        />
        <Route
          path="/owner/menu"
          element={
            <RequireOwner>
              <MenuPage />
            </RequireOwner>
          }
        />
        <Route
          path="/owner/menu/new"
          element={
            <RequireOwner>
              <NewDishPage />
            </RequireOwner>
          }
        />
        <Route
          path="/owner/menu/:dishId/edit"
          element={
            <RequireOwner>
              <EditDishPage />
            </RequireOwner>
          }
        />

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Layout>
  );
}