import type { ReactNode } from 'react';
import { BrowserRouter, Link, Route, Routes } from 'react-router-dom';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AuthProvider } from './context/AuthContext';
import { ChatUnreadProvider } from './context/ChatUnreadProvider';
import { NotificationsProvider } from './context/NotificationsProvider';
import { useAuth } from './context/useAuth';
import { ChatsPage } from './pages/ChatsPage';
import { CreateRidePage } from './pages/CreateRidePage';
import { DashboardPage } from './pages/DashboardPage';
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/LoginPage';
import { MyBookingsPage } from './pages/MyBookingsPage';
import { MyRidesPage } from './pages/MyRidesPage';
import { RegisterPage } from './pages/RegisterPage';
import { RideBookingRequestsPage } from './pages/RideBookingRequestsPage';
import { RideChatPage } from './pages/RideChatPage';
import { RideDetailsPage } from './pages/RideDetailsPage';
import { RidesPage } from './pages/RidesPage';

function SessionProviders({ children }: { children: ReactNode }) {
  const { token } = useAuth();

  return (
    <NotificationsProvider>
      <ChatUnreadProvider key={token ?? 'anonymous'}>{children}</ChatUnreadProvider>
    </NotificationsProvider>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <SessionProviders>
          <Navbar />

          <Route
            path="*"
            element={
              <main className="flex min-h-[calc(100svh-65px)] items-center justify-center bg-[#060e1a] px-6">
                <div className="text-center">
                  <h1 className="text-2xl font-semibold text-white">Page not found</h1>

                  <p className="mt-3 text-sm text-slate-400">This page does not exist.</p>

                  <Link to="/" className="app-button-primary mt-6">
                    Back to home
                  </Link>
                </div>
              </main>
            }
          />

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

            <Route
              path="/rides/:rideId/chat"
              element={
                <ProtectedRoute>
                  <RideChatPage />
                </ProtectedRoute>
              }
            />

            <Route
              path="/chats"
              element={
                <ProtectedRoute>
                  <ChatsPage />
                </ProtectedRoute>
              }
            />
          </Routes>
        </SessionProviders>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
