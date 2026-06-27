import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/useAuth';

export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return <div className="min-h-screen bg-slate-50 p-8 text-slate-600">Loading...</div>;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return children;
}
