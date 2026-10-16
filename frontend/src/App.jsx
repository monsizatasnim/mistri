import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage.jsx';
import PortalPlaceholder from './pages/PortalPlaceholder.jsx';
import RegisterCustomerPage from './pages/RegisterCustomerPage.jsx';
import RegisterShopOwnerPage from './pages/RegisterShopOwnerPage.jsx';
import RegisterProviderPage from './pages/RegisterProviderPage.jsx';
import ForgotPasswordPage from './pages/ForgotPasswordPage.jsx';
import ResetPasswordPage from './pages/ResetPasswordPage.jsx';
import RankedProvidersPage from './pages/RankedProvidersPage.jsx';
import ShopSettingsPage from './pages/ShopSettingsPage.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import AppLayout from './components/AppLayout.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register/customer" element={<RegisterCustomerPage />} />
      <Route path="/register/shop-owner" element={<RegisterShopOwnerPage />} />
      <Route path="/register/provider" element={<RegisterProviderPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />

      <Route element={<AppLayout />}>
        <Route element={<ProtectedRoute role="ROLE_CUSTOMER" />}>
          <Route path="/customer" element={<PortalPlaceholder title="Customer Portal" />} />
          <Route path="/customer/providers" element={<RankedProvidersPage />} />
        </Route>
        <Route element={<ProtectedRoute role="ROLE_PROVIDER" />}>
          <Route path="/provider" element={<PortalPlaceholder title="Service Provider Portal" />} />
        </Route>
        <Route element={<ProtectedRoute role="ROLE_SHOP_OWNER" />}>
          <Route path="/shop-owner" element={<PortalPlaceholder title="Shop Owner Portal" />} />
          <Route path="/shop-owner/settings" element={<ShopSettingsPage />} />
        </Route>
        <Route element={<ProtectedRoute role="ROLE_ADMIN" />}>
          <Route path="/admin" element={<PortalPlaceholder title="Admin Portal" />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}


