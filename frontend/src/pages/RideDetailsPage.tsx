import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { bookingsApi, getApiErrorMessage } from '../api/bookingsApi';
import { ridesApi } from '../api/ridesApi';
import { useAuth } from '../context/useAuth';
import type { Booking } from '../types/booking';
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
      className={`rounded-full border px-3 py-1.5 text-xs font-semibold ${
        styles[status] ?? 'border-white/10 bg-white/5 text-slate-400'
      }`}
    >
      {status}
    </span>
  );
}

export function RideDetailsPage() {
  const { id } = useParams();
  const { token, user } = useAuth();

  const [ride, setRide] = useState<Ride | null>(null);
  const [createdBooking, setCreatedBooking] = useState<Booking | null>(null);
  const [requestedSeats, setRequestedSeats] = useState(1);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isCancelling, setIsCancelling] = useState(false);
  const [isBooking, setIsBooking] = useState(false);
  const [showCancelModal, setShowCancelModal] = useState(false);

  useEffect(() => {
    async function loadRide() {
      if (!token || !id) {
        setIsLoading(false);
        return;
      }

      try {
        const data = await ridesApi.getRide(Number(id), token);
        setRide(data);
      } catch (requestError) {
        setError(getApiErrorMessage(requestError, 'Ride could not be loaded.'));
      } finally {
        setIsLoading(false);
      }
    }

    void loadRide();
  }, [id, token]);

  async function handleCancel() {
    if (!token || !ride) {
      return;
    }

    setIsCancelling(true);
    setError('');
    setSuccessMessage('');

    try {
      const updated = await ridesApi.cancelRide(ride.id, token);
      setRide(updated);
      setSuccessMessage('Your ride has been cancelled.');
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Ride could not be cancelled.'));
    } finally {
      setIsCancelling(false);
    }
  }

  async function handleBookingRequest() {
    if (!token || !ride) {
      return;
    }

    setError('');
    setCreatedBooking(null);
    setIsBooking(true);

    try {
      const booking = await bookingsApi.requestBooking(ride.id, { requestedSeats }, token);

      setCreatedBooking(booking);
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Booking request could not be sent.'));
    } finally {
      setIsBooking(false);
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
              d="M4 12a8 8 0 0 1 8-8v4l3-3-3-3v4a8 8 0 0 0-8 8h4z"
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
        </div>
      </main>
    );
  }

  const isOwner = user?.id === ride.driverId;
  const maxBookableSeats = Math.min(ride.availableSeats, 8);
  const canRequestBooking =
    !isOwner && ride.status === 'ACTIVE' && ride.availableSeats > 0 && !createdBooking;

  return (
    <main className="min-h-[calc(100vh-73px)] bg-[#060e1a] px-6 py-10">
      <section className="mx-auto max-w-6xl">
        <div className="grid gap-6 lg:grid-cols-[1fr_340px]">
          <div className="rounded-3xl border border-white/10 bg-[#0d1b2e] p-7 shadow-2xl md:p-8">
            <div className="flex flex-col justify-between gap-6 md:flex-row md:items-start">
              <div>
                <StatusBadge status={ride.status} />

                <h1 className="mt-5 text-3xl font-bold text-white md:text-4xl">
                  {ride.origin}
                  <span className="mx-3 text-slate-600">→</span>
                  {ride.destination}
                </h1>

                <p className="mt-2 text-slate-400">{formatDate(ride.departureTime)}</p>
              </div>

              <div className="shrink-0 rounded-2xl border border-white/10 bg-white/5 px-6 py-5 md:text-right">
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
                  <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                    To
                  </p>
                  <p className="text-lg font-semibold text-white">{ride.destination}</p>
                </div>
              </div>
            </div>

            <div className="mt-5 grid gap-4 md:grid-cols-3">
              <InfoCard title="Driver" value={ride.driverName} />
              <InfoCard title="Available seats" value={String(ride.availableSeats)} />
              <InfoCard title="Price per seat" value={`${ride.pricePerSeat} RSD`} />
            </div>

            {ride.description && (
              <div className="mt-5 rounded-2xl border border-white/10 bg-white/5 p-5">
                <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">
                  Trip details
                </p>
                <p className="mt-2 leading-7 text-slate-300">{ride.description}</p>
              </div>
            )}

            {error && (
              <div className="mt-6 rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-300">
                {error}
              </div>
            )}

            {successMessage && (
              <div className="mt-6 rounded-xl border border-emerald-500/20 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-300">
                {successMessage}
              </div>
            )}

            {isOwner && (
              <div className="mt-8 flex flex-wrap gap-3">
                {ride.status === 'ACTIVE' && (
                  <button
                    type="button"
                    onClick={() => setShowCancelModal(true)}
                    disabled={isCancelling}
                    className="rounded-xl border border-red-500/20 bg-red-500/10 px-5 py-3 font-semibold text-red-400 transition hover:bg-red-500/20 hover:text-red-300 disabled:opacity-70"
                  >
                    {isCancelling ? 'Cancelling...' : 'Cancel ride'}
                  </button>
                )}
              </div>
            )}
          </div>

          <aside className="h-fit rounded-3xl border border-white/10 bg-[#0d1b2e] p-6 shadow-2xl">
            {isOwner ? (
              <>
                <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-blue-500/10">
                  <svg
                    className="h-6 w-6 text-blue-400"
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
                <p className="mt-2 text-sm font-semibold uppercase tracking-[0.2em] text-blue-400">
                  Your ride
                </p>
                <h2 className="mt-3 text-2xl font-bold text-white">You are driving this trip</h2>
                <p className="mt-3 text-sm leading-6 text-slate-400">
                  Open the booking requests page to approve or reject students who want to join.
                </p>

                <Link
                  to={`/rides/${ride.id}/bookings`}
                  className="mt-6 block rounded-xl bg-blue-500 px-5 py-3 text-center font-semibold text-white transition hover:bg-blue-600"
                >
                  View requests
                </Link>
              </>
            ) : createdBooking ? (
              <>
                <div className="flex h-12 w-12 items-center justify-center rounded-full bg-emerald-500/10">
                  <svg
                    className="h-6 w-6 text-emerald-400"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                </div>

                <h2 className="mt-4 text-2xl font-bold text-white">Request sent</h2>

                <p className="mt-3 text-sm leading-6 text-slate-400">
                  {ride.driverName} will review your request for {createdBooking.requestedSeats}{' '}
                  {createdBooking.requestedSeats === 1 ? 'seat' : 'seats'}.
                </p>

                <Link
                  to="/bookings/my"
                  className="mt-6 block rounded-xl border border-white/10 px-5 py-3 text-center font-semibold text-slate-200 transition hover:bg-white/10 hover:text-white"
                >
                  View my bookings
                </Link>
              </>
            ) : ride.status !== 'ACTIVE' ? (
              <>
                <h2 className="text-2xl font-bold text-white">Booking unavailable</h2>
                <p className="mt-3 text-sm leading-6 text-slate-400">
                  This ride is no longer active and cannot receive new booking requests.
                </p>
              </>
            ) : ride.availableSeats === 0 ? (
              <>
                <h2 className="text-2xl font-bold text-white">Ride is full</h2>
                <p className="mt-3 text-sm leading-6 text-slate-400">
                  All available seats have already been reserved.
                </p>
              </>
            ) : (
              <>
                <p className="text-sm font-semibold uppercase tracking-[0.2em] text-blue-400">
                  Reserve your seat
                </p>

                <h2 className="mt-3 text-2xl font-bold text-white">Join this ride</h2>

                <p className="mt-2 text-sm leading-6 text-slate-400">
                  Choose the number of seats and send a request to the driver.
                </p>

                <label
                  htmlFor="requestedSeats"
                  className="mt-6 block text-sm font-semibold text-slate-300"
                >
                  Number of seats
                </label>

                <div className="mt-2 flex items-center rounded-xl border border-white/10 bg-white/5 p-1">
                  <button
                    type="button"
                    onClick={() => setRequestedSeats((current) => Math.max(1, current - 1))}
                    disabled={requestedSeats === 1}
                    className="flex h-11 w-11 items-center justify-center rounded-lg text-xl text-slate-300 transition hover:bg-white/10 disabled:cursor-not-allowed disabled:opacity-30"
                  >
                    −
                  </button>

                  <input
                    id="requestedSeats"
                    type="number"
                    min={1}
                    max={maxBookableSeats}
                    value={requestedSeats}
                    onChange={(event) => {
                      const value = Number(event.target.value);

                      setRequestedSeats(
                        Math.min(maxBookableSeats, Math.max(1, Number.isNaN(value) ? 1 : value)),
                      );
                    }}
                    className="h-11 min-w-0 flex-1 bg-transparent text-center text-lg font-bold text-white outline-none"
                  />

                  <button
                    type="button"
                    onClick={() =>
                      setRequestedSeats((current) => Math.min(maxBookableSeats, current + 1))
                    }
                    disabled={requestedSeats === maxBookableSeats}
                    className="flex h-11 w-11 items-center justify-center rounded-lg text-xl text-slate-300 transition hover:bg-white/10 disabled:cursor-not-allowed disabled:opacity-30"
                  >
                    +
                  </button>
                </div>

                <div className="mt-5 flex items-center justify-between border-t border-white/10 pt-5">
                  <span className="text-sm text-slate-400">Total price</span>
                  <span className="text-2xl font-bold text-white">
                    {ride.pricePerSeat * requestedSeats} RSD
                  </span>
                </div>

                <button
                  type="button"
                  onClick={() => void handleBookingRequest()}
                  disabled={!canRequestBooking || isBooking}
                  className="mt-5 w-full rounded-xl bg-blue-500 px-5 py-3.5 font-semibold text-white shadow-lg shadow-blue-500/20 transition hover:bg-blue-600 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isBooking ? 'Sending request...' : 'Request booking'}
                </button>

                <p className="mt-3 text-center text-xs leading-5 text-slate-500">
                  Your request must be accepted by the driver before the booking is confirmed.
                </p>
              </>
            )}
          </aside>
        </div>

        {showCancelModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-5 backdrop-blur-sm">
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
                    d="M12 9v4m0 4h.01M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"
                  />
                </svg>
              </div>

              <h2 className="mt-5 text-center text-2xl font-bold text-white">Cancel ride?</h2>

              <p className="mt-3 text-center text-slate-400">
                This action cannot be undone. Your ride will become unavailable for other students.
              </p>

              <div className="mt-8 flex gap-3">
                <button
                  type="button"
                  onClick={() => setShowCancelModal(false)}
                  className="flex-1 rounded-xl border border-white/10 py-3 font-semibold text-slate-300 transition hover:bg-white/10"
                >
                  Keep ride
                </button>

                <button
                  type="button"
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

function InfoCard({ title, value }: { title: string; value: string }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-white/5 p-5">
      <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">{title}</p>
      <p className="mt-2 text-xl font-semibold text-white">{value}</p>
    </div>
  );
}
