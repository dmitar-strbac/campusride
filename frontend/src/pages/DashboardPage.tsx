import { Link } from 'react-router-dom';
import { useAuth } from '../context/useAuth';

export function DashboardPage() {
  const { user } = useAuth();

  return (
    <main className="min-h-[calc(100vh-73px)] bg-[#060e1a] px-6 py-10">
      <div className="mx-auto max-w-6xl animate-fade-up space-y-8">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.25em] text-blue-400">
            Dashboard
          </p>
          <h1 className="mt-3 text-4xl font-bold text-white">Welcome back, {user?.firstName}</h1>
          <p className="mt-2 text-slate-400">
            Here's a quick overview of your CampusRide activity.
          </p>
        </div>

        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <Link
            to="/rides"
            className="group flex flex-col gap-4 rounded-2xl border border-white/10 bg-[#0d1b2e] p-6 transition hover:border-blue-500/40 hover:bg-[#10213a]"
          >
            <div className="flex h-12 w-12 items-center justify-center rounded-xl border border-blue-500/30 bg-blue-500/10">
              <svg
                className="h-6 w-6 text-blue-400"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.75"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <circle cx="11" cy="11" r="8" />
                <line x1="21" y1="21" x2="16.65" y2="16.65" />
              </svg>
            </div>
            <div>
              <p className="font-semibold text-white group-hover:text-blue-300 transition">
                Find a ride
              </p>
              <p className="mt-1 text-sm text-slate-400">
                Browse available student rides between cities.
              </p>
            </div>
            <span className="mt-auto text-xs font-semibold text-blue-400 group-hover:translate-x-1 transition-transform">
              Browse rides →
            </span>
          </Link>

          <Link
            to="/rides/create"
            className="group flex flex-col gap-4 rounded-2xl border border-white/10 bg-[#0d1b2e] p-6 transition hover:border-emerald-500/40 hover:bg-[#0a2018]"
          >
            <div className="flex h-12 w-12 items-center justify-center rounded-xl border border-emerald-500/30 bg-emerald-500/10">
              <svg
                className="h-6 w-6 text-emerald-400"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.75"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M19 17H5a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h1.5L8 5h8l1.5 2H19a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2z" />
                <circle cx="7.5" cy="17" r="2" />
                <circle cx="16.5" cy="17" r="2" />
              </svg>
            </div>
            <div>
              <p className="font-semibold text-white group-hover:text-emerald-300 transition">
                Offer a ride
              </p>
              <p className="mt-1 text-sm text-slate-400">
                Share your trip and split the cost with students.
              </p>
            </div>
            <span className="mt-auto text-xs font-semibold text-emerald-400 group-hover:translate-x-1 transition-transform">
              Create offer →
            </span>
          </Link>

          <Link
            to="/rides/my"
            className="group flex flex-col gap-4 rounded-2xl border border-white/10 bg-[#0d1b2e] p-6 transition hover:border-yellow-500/40 hover:bg-[#18160a]"
          >
            <div className="flex h-12 w-12 items-center justify-center rounded-xl border border-yellow-500/30 bg-yellow-500/10">
              <svg
                className="h-6 w-6 text-yellow-400"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.75"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M9 11l3 3L22 4" />
                <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
              </svg>
            </div>
            <div>
              <p className="font-semibold text-white group-hover:text-yellow-300 transition">
                My rides
              </p>
              <p className="mt-1 text-sm text-slate-400">
                View and manage rides you've offered as a driver.
              </p>
            </div>
            <span className="mt-auto text-xs font-semibold text-yellow-400 group-hover:translate-x-1 transition-transform">
              View rides →
            </span>
          </Link>
        </div>

        <div className="rounded-2xl border border-white/10 bg-[#0d1b2e] p-6">
          <div className="flex items-start gap-4">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full border border-blue-500/30 bg-blue-500/10">
              <svg
                className="h-5 w-5 text-blue-400"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
            </div>
            <div>
              <p className="font-semibold text-white">Getting started</p>
              <p className="mt-1 text-sm text-slate-400">
                Browse available rides to find a trip that fits your schedule, or offer your own
                ride to earn money while helping fellow students get around.
              </p>
            </div>
          </div>
        </div>
      </div>
    </main>
  );
}
