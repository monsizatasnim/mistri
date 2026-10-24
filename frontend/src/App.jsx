import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage.jsx';
import PortalPlaceholder from './pages/PortalPlaceholder.jsx';
import RegisterCustomerPage from './pages/RegisterCustomerPage.jsx';
import RegisterShopOwnerPage from './pages/RegisterShopOwnerPage.jsx';
import RegisterProviderPage from './pages/RegisterProviderPage.jsx';
import ForgotPasswordPage from './pages/ForgotPasswordPage.jsx';
import ResetPasswordPage from './pages/ResetPasswordPage.jsx';
import RankedProvidersPage from './pages/RankedProvidersPage.jsx';
import ProviderNearbyShopsPage from './pages/ProviderNearbyShopsPage.jsx';
import ShopSettingsPage from './pages/ShopSettingsPage.jsx';
import ShopOrdersPage from './pages/ShopOrdersPage.jsx';
import ShopProfilePage from './pages/ShopProfilePage.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import AppLayout from './components/AppLayout.jsx';
import AdminGuard from './admin/AdminGuard.jsx';
import AdminLayout from './admin/AdminLayout.jsx';
import AdminLoginPage from './admin/AdminLoginPage.jsx';
import DashboardPage from './admin/pages/DashboardPage.jsx';
import VerificationPage from './admin/pages/VerificationPage.jsx';
import AccountsPage from './admin/pages/AccountsPage.jsx';
import DisputesPage from './admin/pages/DisputesPage.jsx';
import SettingsPage from './admin/pages/SettingsPage.jsx';
import ReportsPage from './admin/pages/ReportsPage.jsx';
import ProviderLoginPage from './pages/ProviderLoginPage.jsx';
import ProviderPricePage from './pages/ProviderPricePage.jsx';
import ProviderProfilePage from './pages/ProviderProfilePage.jsx';
import ProviderOrderPaymentPage from './pages/ProviderOrderPaymentPage.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/provider/login" element={<ProviderLoginPage />} />
      <Route path="/register/customer" element={<RegisterCustomerPage />} />
      <Route path="/register/shop-owner" element={<RegisterShopOwnerPage />} />
      <Route path="/register/provider" element={<RegisterProviderPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />

      <Route path="/admin/login" element={<AdminLoginPage />} />
      <Route
        path="/admin"
        element={
          <AdminGuard>
            <AdminLayout />
          </AdminGuard>
        }
      >
        <Route index element={<DashboardPage />} />
        <Route path="verification" element={<VerificationPage />} />
        <Route path="accounts" element={<AccountsPage />} />
        <Route path="disputes" element={<DisputesPage />} />
        <Route path="settings" element={<SettingsPage />} />
        <Route path="reports" element={<ReportsPage />} />
      </Route>

      <Route element={<AppLayout />}>
        <Route element={<ProtectedRoute role="ROLE_CUSTOMER" />}>
          <Route path="/customer" element={<PortalPlaceholder title="Customer Portal" />} />
          <Route path="/customer/providers" element={<RankedProvidersPage />} />
          <Route path="/customer/providers/:id" element={<ProviderProfilePage />} />
        </Route>
        <Route element={<ProtectedRoute role="ROLE_PROVIDER" />}>
          <Route path="/provider" element={<PortalPlaceholder title="Service Provider Portal" />} />
          <Route path="/provider/price" element={<ProviderPricePage />} />
          <Route path="/provider/pay" element={<ProviderOrderPaymentPage />} />
          <Route path="/provider/shops" element={<ProviderNearbyShopsPage />} />
        </Route>
        <Route element={<ProtectedRoute role="ROLE_SHOP_OWNER" />}>
          <Route path="/shop-owner" element={<PortalPlaceholder title="Shop Owner Portal" />} />
          <Route path="/shop-owner/settings" element={<ShopSettingsPage />} />
          <Route path="/shop-owner/orders" element={<ShopOrdersPage />} />
          <Route path="/shop-owner/profile" element={<ShopProfilePage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}
