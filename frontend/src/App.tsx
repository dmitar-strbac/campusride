import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AuthProvider } from './context/AuthContext';
import { CreateRidePage } from './pages/CreateRidePage';
import { DashboardPage } from './pages/DashboardPage';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { MyBookingsPage } from './pages/MyBookingsPage';
import { MyRidesPage } from './pages/MyRidesPage';
import { RegisterPage } from './pages/RegisterPage';
import { RideBookingRequestsPage } from './pages/RideBookingRequestsPage';
import { RideDetailsPage } from './pages/RideDetailsPage';
import { RidesPage } from './pages/RidesPage';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Navbar />

        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/rides"
            element={
              <ProtectedRoute>
                <RidesPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/rides/create"
            element={
              <ProtectedRoute>
                <CreateRidePage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/rides/my"
            element={
              <ProtectedRoute>
                <MyRidesPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/bookings/my"
            element={
              <ProtectedRoute>
                <MyBookingsPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/rides/:rideId/bookings"
            element={
              <ProtectedRoute>
                <RideBookingRequestsPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/rides/:id"
            element={
              <ProtectedRoute>
                <RideDetailsPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
