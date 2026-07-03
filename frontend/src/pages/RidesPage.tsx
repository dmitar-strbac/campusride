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

export function RidesPage() {
  const { token } = useAuth();

  const [rides, setRides] = useState<Ride[]>([]);
  const [origin, setOrigin] = useState('');
  const [destination, setDestination] = useState('');
  const [date, setDate] = useState('');
  const [seats, setSeats] = useState(1);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function fetchRides(from: string, to: string) {
      setError('');
      setIsLoading(true);
      try {
        const data = await ridesApi.searchRides(from, to, token ?? undefined);
        setRides(data);
      } catch {
        setError('Could not load rides. Please try again.');
      } finally {
        setIsLoading(false);
      }
    }

    void fetchRides('', '');
  }, [token]);

  async function handleSearch(event: React.FormEvent) {
    event.preventDefault();
    setError('');
    setIsLoading(true);
    try {
      const data = await ridesApi.searchRides(origin, destination, token ?? undefined);
      setRides(data);
    } catch {
      setError('Could not load rides. Please try again.');
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <main className="min-h-[calc(100vh-73px)] bg-[#060e1a] px-6 py-10">
      <section className="mx-auto max-w-6xl">
        <div className="flex flex-col justify-between gap-5 md:flex-row md:items-end">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.25em] text-blue-400">
              Find a ride
            </p>
            <h1 className="mt-3 text-4xl font-bold text-white">Available student rides</h1>
            <p className="mt-3 max-w-2xl text-slate-400">
              Search upcoming intercity rides shared by verified CampusRide students.
            </p>
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

        <form
          onSubmit={handleSearch}
          className="mt-8 flex flex-col gap-2 md:flex-row md:items-stretch md:gap-0 md:rounded-2xl md:border md:border-white/10 md:bg-[#0d1b2e] md:p-1.5 md:shadow-xl"
        >
          <div className="group flex flex-1 items-center gap-3 rounded-xl border border-white/10 bg-[#0d1b2e] px-4 py-3.5 transition focus-within:border-blue-500/50 focus-within:bg-[#0f2040] md:rounded-xl md:border-0 md:bg-transparent md:px-5 md:focus-within:bg-[#0f2040]">
            <svg
              className="h-4 w-4 shrink-0 text-blue-400"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <circle cx="12" cy="10" r="3" />
              <path d="M12 2a8 8 0 0 1 8 8c0 5.25-8 12-8 12S4 15.25 4 10a8 8 0 0 1 8-8z" />
            </svg>
            <div className="flex-1 min-w-0">
              <label className="block text-[10px] font-semibold uppercase tracking-widest text-slate-500">
                From
              </label>
              <input
                value={origin}
                onChange={(e) => setOrigin(e.target.value)}
                placeholder="Departure city"
                className="mt-0.5 w-full bg-transparent text-sm font-medium text-white outline-none placeholder:text-slate-600"
              />
            </div>
          </div>

          <div className="hidden md:flex items-center px-1 text-slate-700">
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
          </div>

          <div className="group flex flex-1 items-center gap-3 rounded-xl border border-white/10 bg-[#0d1b2e] px-4 py-3.5 transition focus-within:border-blue-500/50 focus-within:bg-[#0f2040] md:rounded-xl md:border-0 md:bg-transparent md:px-5 md:focus-within:bg-[#0f2040]">
            <svg
              className="h-4 w-4 shrink-0 text-emerald-400"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <circle cx="12" cy="10" r="3" />
              <path d="M12 2a8 8 0 0 1 8 8c0 5.25-8 12-8 12S4 15.25 4 10a8 8 0 0 1 8-8z" />
            </svg>
            <div className="flex-1 min-w-0">
              <label className="block text-[10px] font-semibold uppercase tracking-widest text-slate-500">
                To
              </label>
              <input
                value={destination}
                onChange={(e) => setDestination(e.target.value)}
                placeholder="Destination city"
                className="mt-0.5 w-full bg-transparent text-sm font-medium text-white outline-none placeholder:text-slate-600"
              />
            </div>
          </div>

          <div className="hidden md:block w-px self-stretch bg-white/10 mx-1" />

          <div className="group flex items-center gap-3 rounded-xl border border-white/10 bg-[#0d1b2e] px-4 py-3.5 transition focus-within:border-blue-500/50 focus-within:bg-[#0f2040] md:rounded-xl md:border-0 md:bg-transparent md:px-5 md:focus-within:bg-[#0f2040]">
            <svg
              className="h-4 w-4 shrink-0 text-slate-500"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <rect x="3" y="4" width="18" height="18" rx="2" />
              <line x1="16" y1="2" x2="16" y2="6" />
              <line x1="8" y1="2" x2="8" y2="6" />
              <line x1="3" y1="10" x2="21" y2="10" />
            </svg>
            <div className="min-w-0">
              <label className="block text-[10px] font-semibold uppercase tracking-widest text-slate-500">
                Date
              </label>
              <input
                type="date"
                value={date}
                onChange={(e) => setDate(e.target.value)}
                className="mt-0.5 w-full bg-transparent text-sm font-medium text-white outline-none [color-scheme:dark]"
              />
            </div>
          </div>

          <div className="hidden md:block w-px self-stretch bg-white/10 mx-1" />

          <div className="flex items-center gap-3 rounded-xl border border-white/10 bg-[#0d1b2e] px-4 py-3.5 md:rounded-xl md:border-0 md:bg-transparent md:px-5">
            <svg
              className="h-4 w-4 shrink-0 text-slate-500"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <circle cx="12" cy="8" r="4" />
              <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7" />
            </svg>
            <div>
              <p className="text-[10px] font-semibold uppercase tracking-widest text-slate-500">
                Seats
              </p>
              <div className="mt-0.5 flex items-center gap-2.5">
                <button
                  type="button"
                  onClick={() => setSeats((s) => Math.max(1, s - 1))}
                  className="flex h-6 w-6 items-center justify-center rounded-full border border-white/20 text-slate-400 transition hover:border-white/40 hover:text-white"
                >
                  <svg
                    className="h-3 w-3"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2.5"
                  >
                    <line x1="5" y1="12" x2="19" y2="12" />
                  </svg>
                </button>
                <span className="w-5 text-center text-sm font-semibold text-white">{seats}</span>
                <button
                  type="button"
                  onClick={() => setSeats((s) => Math.min(8, s + 1))}
                  className="flex h-6 w-6 items-center justify-center rounded-full border border-white/20 text-slate-400 transition hover:border-white/40 hover:text-white"
                >
                  <svg
                    className="h-3 w-3"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2.5"
                  >
                    <line x1="12" y1="5" x2="12" y2="19" />
                    <line x1="5" y1="12" x2="19" y2="12" />
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <button
            type="submit"
            className="flex items-center justify-center gap-2 rounded-xl bg-blue-500 px-6 py-3.5 text-sm font-semibold text-white shadow-lg shadow-blue-500/20 transition hover:bg-blue-600 active:scale-95 md:rounded-xl"
          >
            <svg
              className="h-4 w-4"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2.5"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
            Search
          </button>
        </form>

        {error && (
          <div className="mt-6 rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-400">
            {error}
          </div>
        )}

        <div className="mt-6 grid gap-4">
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
              Loading rides...
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
              <p className="mt-4 font-semibold text-white">No rides found</p>
              <p className="mt-1 text-sm text-slate-400">
                Try different locations or be the first to offer a ride.
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
              <Link
                key={ride.id}
                to={`/rides/${ride.id}`}
                className="group rounded-2xl border border-white/10 bg-[#0d1b2e] p-6 transition hover:-translate-y-0.5 hover:border-blue-500/40 hover:bg-[#10213a]"
              >
                <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
                  <div className="flex items-center gap-4">
                    <div className="hidden shrink-0 flex-col items-center gap-1 sm:flex">
                      <div className="h-2.5 w-2.5 rounded-full bg-blue-400" />
                      <div className="h-8 w-px bg-white/10" />
                      <div className="h-2.5 w-2.5 rounded-full bg-emerald-400" />
                    </div>
                    <div>
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
                      <p className="mt-1.5 text-sm text-slate-400">
                        {formatDate(ride.departureTime)} · {ride.driverName}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="rounded-full border border-blue-500/20 bg-blue-500/10 px-3.5 py-1.5 text-sm font-semibold text-blue-300">
                      {ride.availableSeats} seats
                    </span>
                    <span className="rounded-full border border-emerald-500/20 bg-emerald-500/10 px-3.5 py-1.5 text-sm font-semibold text-emerald-300">
                      {ride.pricePerSeat} RSD
                    </span>
                    <svg
                      className="h-5 w-5 text-slate-600 transition group-hover:translate-x-0.5 group-hover:text-slate-400"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <polyline points="9 18 15 12 9 6" />
                    </svg>
                  </div>
                </div>
              </Link>
            ))
          )}
        </div>
      </section>
    </main>
  );
}
