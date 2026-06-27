import { useAuth } from '../context/useAuth';

export function DashboardPage() {
  const { user } = useAuth();

  return (
    <main className="min-h-[calc(100vh-73px)] bg-[#060e1a] px-6 py-10">
      <section className="mx-auto max-w-6xl animate-fade-up">
        <div className="rounded-2xl border border-white/10 bg-[#0d1b2e] p-8 shadow-2xl">
          <p className="text-sm font-semibold text-blue-400">Dashboard</p>
          <h1 className="mt-2 text-3xl font-bold text-white">Welcome, {user?.firstName}</h1>
          <p className="mt-3 max-w-2xl text-slate-400">
            Auth flow is connected. Ride management cards will be added in the next milestone.
          </p>
        </div>
      </section>
    </main>
  );
}
