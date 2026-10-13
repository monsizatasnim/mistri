import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage.jsx';
import PortalPlaceholder from './pages/PortalPlaceholder.jsx';
import RegisterCustomerPage from './pages/RegisterCustomerPage.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register/customer" element={<RegisterCustomerPage />} />
      <Route path="/customer" element={<PortalPlaceholder title="Customer Portal" />} />
      <Route path="/provider" element={<PortalPlaceholder title="Service Provider Portal" />} />
      <Route path="/shop-owner" element={<PortalPlaceholder title="Shop Owner Portal" />} />
      <Route path="/admin" element={<PortalPlaceholder title="Admin Portal" />} />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

