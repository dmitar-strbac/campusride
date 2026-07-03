import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ridesApi } from '../api/ridesApi';
import { useAuth } from '../context/useAuth';
import type { Ride } from '../types/ride';

function formatDate(value: string) {
  return new Intl.DateTimeFormat('en', {
    dateStyle: 'full',
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
      className={`rounded-full border px-4 py-1.5 text-sm font-semibold ${styles[status] ?? 'border-white/10 bg-white/5 text-slate-400'}`}
    >
      {status}
    </span>
  );
}

export function RideDetailsPage() {
  const { id } = useParams();
  const { token, user } = useAuth();

  const [ride, setRide] = useState<Ride | null>(null);
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isCancelling, setIsCancelling] = useState(false);
  const [showCancelModal, setShowCancelModal] = useState(false);

  useEffect(() => {
    async function loadRide() {
      if (!token || !id) return;
      try {
        const data = await ridesApi.getRide(Number(id), token);
        setRide(data);
      } catch {
        setError('Ride could not be loaded.');
      } finally {
        setIsLoading(false);
      }
    }
    void loadRide();
  }, [id, token]);

  async function handleCancel() {
    if (!token || !ride) return;
    setIsCancelling(true);
    setError('');
    try {
      const updated = await ridesApi.cancelRide(ride.id, token);
      setRide(updated);
    } catch {
      setError('Ride could not be cancelled.');
    } finally {
      setIsCancelling(false);
    }
  }

  if (isLoading) {
    return (
      <main className="flex min-h-[calc(100vh-73px)] items-center justify-center bg-[#060e1a]">
        <div className="flex items-center gap-3 text-slate-400">
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
          Loading ride...
        </div>
      </main>
    );
  }

  if (!ride) {
    return (
      <main className="flex min-h-[calc(100vh-73px)] items-center justify-center bg-[#060e1a]">
        <div className="text-center">
          <p className="text-red-400">{error || 'Ride not found.'}</p>
          <Link
            to="/rides"
            className="mt-4 inline-block text-sm font-semibold text-blue-400 hover:text-blue-300"
          >
            ← Back to rides
          </Link>
        </div>
      </main>
    );
  }

  const isOwner = user?.id === ride.driverId;

  return (
    <main className="min-h-[calc(100vh-73px)] bg-[#060e1a] px-6 py-10">
      <section className="mx-auto max-w-4xl">
        <div className="rounded-3xl border border-white/10 bg-[#0d1b2e] p-8 shadow-2xl">
          <div className="flex flex-col justify-between gap-6 md:flex-row md:items-start">
            <div>
              <StatusBadge status={ride.status} />
              <h1 className="mt-5 text-4xl font-bold text-white">
                {ride.origin}
                <span className="mx-3 text-slate-600">→</span>
                {ride.destination}
              </h1>
              <p className="mt-2 text-slate-400">{formatDate(ride.departureTime)}</p>
            </div>

            <div className="shrink-0 rounded-2xl border border-white/10 bg-white/5 px-6 py-5 text-right">
              <p className="text-3xl font-bold text-white">{ride.pricePerSeat} RSD</p>
              <p className="mt-1 text-sm text-slate-400">per seat</p>
            </div>
          </div>

          <div className="mt-8 flex items-center gap-4 rounded-2xl border border-white/10 bg-white/5 px-6 py-5">
            <div className="flex flex-col items-center gap-1 pt-4">
              <div className="h-3 w-3 rounded-full bg-blue-400" />
              <div className="h-10 w-px border-l border-dashed border-white/20" />
              <div className="h-3 w-3 rounded-full bg-emerald-400" />
            </div>
            <div className="space-y-4">
              <div>
                <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                  From
                </p>
                <p className="text-lg font-semibold text-white">{ride.origin}</p>
              </div>
              <div>
                <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">To</p>
                <p className="text-lg font-semibold text-white">{ride.destination}</p>
              </div>
            </div>
          </div>

          <div className="mt-5 grid gap-4 md:grid-cols-3">
            <InfoCard
              icon={
                <svg
                  className="h-5 w-5 text-blue-400"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
              }
              title="Driver"
              value={ride.driverName}
            />
            <InfoCard
              icon={
                <svg
                  className="h-5 w-5 text-emerald-400"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                  <circle cx="9" cy="7" r="4" />
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                  <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                </svg>
              }
              title="Available seats"
              value={String(ride.availableSeats)}
            />
            <InfoCard
              icon={
                <svg
                  className="h-5 w-5 text-yellow-400"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <line x1="12" y1="1" x2="12" y2="23" />
                  <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
                </svg>
              }
              title="Price per seat"
              value={`${ride.pricePerSeat} RSD`}
            />
          </div>

          {ride.description && (
            <div className="mt-5 rounded-2xl border border-white/10 bg-white/5 p-5">
              <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                Details
              </p>
              <p className="mt-2 text-slate-300">{ride.description}</p>
            </div>
          )}

          {error && (
            <div className="mt-6 rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-400">
              {error}
            </div>
          )}

          <div className="mt-8 flex flex-wrap gap-3">
            {isOwner && ride.status === 'ACTIVE' && (
              <button
                onClick={() => setShowCancelModal(true)}
                disabled={isCancelling}
                className="rounded-xl border border-red-500/20 bg-red-500/10 px-5 py-3 font-semibold text-red-400 transition hover:bg-red-500/20 hover:text-red-300 disabled:opacity-70"
              >
                {isCancelling ? 'Cancelling...' : 'Cancel ride'}
              </button>
            )}

            {!isOwner && ride.status === 'ACTIVE' && (
              <button className="rounded-xl bg-blue-500 px-5 py-3 font-semibold text-white shadow-lg shadow-blue-500/20 transition hover:bg-blue-600">
                Request booking
              </button>
            )}

            <Link
              to="/rides"
              className="rounded-xl border border-white/10 px-5 py-3 text-center font-semibold text-slate-300 transition hover:bg-white/10 hover:text-white"
            >
              Browse rides
            </Link>
          </div>
        </div>
        {showCancelModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm">
            <div className="w-full max-w-md rounded-3xl border border-white/10 bg-[#0d1b2e] p-8 shadow-2xl">
              <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-red-500/10">
                <svg
                  className="h-7 w-7 text-red-400"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M12 9v4m0 4h.01M10.29 3.86 1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"
                  />
                </svg>
              </div>

              <h2 className="mt-5 text-center text-2xl font-bold text-white">Cancel ride?</h2>

              <p className="mt-3 text-center text-slate-400">
                This action cannot be undone. Your ride will become unavailable for other students.
              </p>

              <div className="mt-8 flex gap-3">
                <button
                  onClick={() => setShowCancelModal(false)}
                  className="flex-1 rounded-xl border border-white/10 py-3 font-semibold text-slate-300 transition hover:bg-white/10"
                >
                  Keep ride
                </button>

                <button
                  onClick={async () => {
                    setShowCancelModal(false);
                    await handleCancel();
                  }}
                  className="flex-1 rounded-xl bg-red-500 py-3 font-semibold text-white transition hover:bg-red-600"
                >
                  Yes, cancel
                </button>
              </div>
            </div>
          </div>
        )}
      </section>
    </main>
  );
}

function InfoCard({ icon, title, value }: { icon: React.ReactNode; title: string; value: string }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-white/5 p-5">
      <div className="flex items-center gap-2">
        {icon}
        <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">{title}</p>
      </div>
      <p className="mt-2 text-xl font-semibold text-white">{value}</p>
    </div>
  );
}
