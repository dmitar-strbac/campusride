import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ridesApi } from '../api/ridesApi';
import { useAuth } from '../context/useAuth';
import type { Ride } from '../types/ride';

function formatDate(value: string) {
  return new Intl.DateTimeFormat('en', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

function StatusBadge({ status }: { status: string }) {
  const styles: Record<string, string> = {
    ACTIVE: 'border-emerald-500/20 bg-emerald-500/10 text-emerald-300',
    CANCELLED: 'border-red-500/20 bg-red-500/10 text-red-300',
    COMPLETED: 'border-slate-500/20 bg-slate-500/10 text-slate-300',
  };
  return (
    <span
      className={`rounded-full border px-3 py-1 text-xs font-semibold ${styles[status] ?? 'border-white/10 bg-white/5 text-slate-400'}`}
    >
      {status}
    </span>
  );
}

export function MyRidesPage() {
  const { token } = useAuth();

  const [rides, setRides] = useState<Ride[]>([]);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function load() {
      if (!token) return;
      try {
        const data = await ridesApi.getMyOfferedRides(token);
        setRides(data);
      } catch {
        setError('Could not load your rides.');
      } finally {
        setIsLoading(false);
      }
    }
    void load();
  }, [token]);

  async function handleCancel(rideId: number) {
    if (!token) return;
    try {
      const updated = await ridesApi.cancelRide(rideId, token);
      setRides((current) => current.map((r) => (r.id === rideId ? updated : r)));
    } catch {
      setError('Could not cancel ride.');
    }
  }

  return (
    <main className="min-h-[calc(100vh-73px)] bg-[#060e1a] px-6 py-10">
      <section className="mx-auto max-w-6xl">
        <div className="flex flex-col justify-between gap-5 md:flex-row md:items-end">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.25em] text-blue-400">
              My rides
            </p>
            <h1 className="mt-3 text-4xl font-bold text-white">Your offered rides</h1>
            <p className="mt-3 text-slate-400">Manage trips you created as a driver.</p>
          </div>
          <Link
            to="/rides/create"
            className="flex items-center gap-2 rounded-xl border border-blue-500/40 px-5 py-3 text-sm font-semibold text-blue-400 transition hover:border-blue-400 hover:text-blue-300"
          >
            Offer a ride
            <svg
              className="h-4 w-4"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <line x1="5" y1="12" x2="19" y2="12" />
              <polyline points="12 5 19 12 12 19" />
            </svg>
          </Link>
        </div>

        {error && (
          <div className="mt-6 rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-400">
            {error}
          </div>
        )}

        <div className="mt-8 grid gap-4">
          {isLoading ? (
            <div className="flex items-center gap-3 py-8 text-slate-400">
              <svg className="h-5 w-5 animate-spin" viewBox="0 0 24 24" fill="none">
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                />
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8v4l3-3-3-3v4a8 8 0 00-8 8h4z"
                />
              </svg>
              Loading your rides...
            </div>
          ) : rides.length === 0 ? (
            <div className="rounded-2xl border border-white/10 bg-[#0d1b2e] p-10 text-center">
              <svg
                className="mx-auto h-10 w-10 text-slate-600"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M19 17H5a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h1.5L8 5h8l1.5 2H19a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2z" />
                <circle cx="7.5" cy="17" r="2" />
                <circle cx="16.5" cy="17" r="2" />
              </svg>
              <p className="mt-4 font-semibold text-white">No rides yet</p>
              <p className="mt-1 text-sm text-slate-400">
                Share your first trip and start earning.
              </p>
              <Link
                to="/rides/create"
                className="mt-5 inline-block rounded-xl bg-blue-500 px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-blue-500/20 transition hover:bg-blue-600"
              >
                Offer a ride
              </Link>
            </div>
          ) : (
            rides.map((ride) => (
              <div key={ride.id} className="rounded-2xl border border-white/10 bg-[#0d1b2e] p-6">
                <div className="flex flex-col justify-between gap-5 md:flex-row md:items-center">
                  <div className="flex items-center gap-4">
                    <div className="hidden shrink-0 flex-col items-center gap-1 sm:flex">
                      <div className="h-2.5 w-2.5 rounded-full bg-blue-400" />
                      <div className="h-8 w-px bg-white/10" />
                      <div className="h-2.5 w-2.5 rounded-full bg-emerald-400" />
                    </div>
                    <div>
                      <div className="flex flex-wrap items-center gap-3">
                        <div className="flex items-center gap-2">
                          <span className="text-xl font-bold text-white">{ride.origin}</span>
                          <svg
                            className="h-4 w-4 text-slate-500"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth="2"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                          >
                            <line x1="5" y1="12" x2="19" y2="12" />
                            <polyline points="12 5 19 12 12 19" />
                          </svg>
                          <span className="text-xl font-bold text-white">{ride.destination}</span>
                        </div>
                        <StatusBadge status={ride.status} />
                      </div>
                      <p className="mt-1.5 text-sm text-slate-400">
                        {formatDate(ride.departureTime)}
                      </p>
                      <div className="mt-2 flex gap-3">
                        <span className="text-xs text-slate-500">
                          {ride.availableSeats} seats · {ride.pricePerSeat} RSD/seat
                        </span>
                      </div>
                    </div>
                  </div>

                  <div className="flex flex-wrap gap-3">
                    <Link
                      to={`/rides/${ride.id}`}
                      className="rounded-xl border border-white/10 px-4 py-2 text-sm font-semibold text-slate-300 transition hover:-translate-y-0.5 hover:bg-white/10 hover:text-white"
                    >
                      Details
                    </Link>
                    {ride.status === 'ACTIVE' && (
                      <button
                        onClick={() => handleCancel(ride.id)}
                        className="rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-2 text-sm font-semibold text-red-400 transition hover:bg-red-500/20 hover:text-red-300"
                      >
                        Cancel ride
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </section>
    </main>
  );
}
