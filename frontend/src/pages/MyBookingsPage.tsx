import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { bookingsApi, getApiErrorMessage } from '../api/bookingsApi';
import { useAuth } from '../context/useAuth';
import type { Booking, BookingStatus } from '../types/booking';

function formatDate(value: string) {
  return new Intl.DateTimeFormat('en', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

function BookingStatusBadge({ status }: { status: BookingStatus }) {
  const styles: Record<BookingStatus, string> = {
    PENDING: 'border-amber-500/20 bg-amber-500/10 text-amber-300',
    ACCEPTED: 'border-emerald-500/20 bg-emerald-500/10 text-emerald-300',
    REJECTED: 'border-red-500/20 bg-red-500/10 text-red-300',
    CANCELLED: 'border-slate-500/20 bg-slate-500/10 text-slate-300',
  };

  const labels: Record<BookingStatus, string> = {
    PENDING: 'Pending approval',
    ACCEPTED: 'Accepted',
    REJECTED: 'Rejected',
    CANCELLED: 'Cancelled',
  };

  return (
    <span className={`rounded-full border px-3 py-1 text-xs font-semibold ${styles[status]}`}>
      {labels[status]}
    </span>
  );
}

export function MyBookingsPage() {
  const { token } = useAuth();

  const [bookings, setBookings] = useState<Booking[]>([]);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [processingBookingId, setProcessingBookingId] = useState<number | null>(null);
  const [bookingToCancel, setBookingToCancel] = useState<Booking | null>(null);
  const [currentTime] = useState(() => Date.now());

  useEffect(() => {
    async function loadBookings() {
      if (!token) {
        setIsLoading(false);
        return;
      }

      try {
        const data = await bookingsApi.getMyBookings(token);
        setBookings(data);
      } catch (requestError) {
        setError(getApiErrorMessage(requestError, 'Could not load your bookings.'));
      } finally {
        setIsLoading(false);
      }
    }

    void loadBookings();
  }, [token]);

  const stats = useMemo(
    () => ({
      total: bookings.length,
      pending: bookings.filter((booking) => booking.status === 'PENDING').length,
      accepted: bookings.filter((booking) => booking.status === 'ACCEPTED').length,
    }),
    [bookings],
  );

  async function handleCancelBooking() {
    if (!token || !bookingToCancel) {
      return;
    }

    const bookingId = bookingToCancel.id;

    setProcessingBookingId(bookingId);
    setError('');
    setSuccessMessage('');

    try {
      const updated = await bookingsApi.cancelBooking(bookingId, token);

      setBookings((current) =>
        current.map((booking) => (booking.id === bookingId ? updated : booking)),
      );

      setSuccessMessage('Your booking has been cancelled.');
      setBookingToCancel(null);
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Booking could not be cancelled.'));
    } finally {
      setProcessingBookingId(null);
    }
  }

  return (
    <main className="min-h-[calc(100vh-73px)] bg-[#060e1a] px-6 py-10">
      <section className="mx-auto max-w-6xl">
        <div className="flex flex-col justify-between gap-5 md:flex-row md:items-end">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.25em] text-blue-400">
              My bookings
            </p>
            <h1 className="mt-3 text-4xl font-bold text-white">Your ride bookings</h1>
            <p className="mt-3 max-w-2xl text-slate-400">
              Follow your booking requests and manage the rides you plan to join.
            </p>
          </div>

          <Link
            to="/rides"
            className="flex items-center gap-2 rounded-xl border border-blue-500/40 px-5 py-3 text-sm font-semibold text-blue-400 transition hover:border-blue-400 hover:text-blue-300"
          >
            Find another ride
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

        <div className="mt-8 grid gap-4 sm:grid-cols-3">
          <StatCard label="Total bookings" value={stats.total} />
          <StatCard label="Awaiting approval" value={stats.pending} />
          <StatCard label="Confirmed trips" value={stats.accepted} />
        </div>

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

        <div className="mt-8 space-y-4">
          {isLoading ? (
            <LoadingState text="Loading your bookings..." />
          ) : bookings.length === 0 ? (
            <div className="rounded-3xl border border-white/10 bg-[#0d1b2e] px-6 py-14 text-center">
              <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-blue-500/10">
                <svg
                  className="h-8 w-8 text-blue-400"
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

              <h2 className="mt-5 text-xl font-bold text-white">No bookings yet</h2>
              <p className="mt-2 text-sm text-slate-400">
                Search available rides and send your first booking request.
              </p>

              <Link
                to="/rides"
                className="mt-6 inline-block rounded-xl bg-blue-500 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-blue-600"
              >
                Browse rides
              </Link>
            </div>
          ) : (
            bookings.map((booking) => {
              const canCancel =
                (booking.status === 'PENDING' || booking.status === 'ACCEPTED') &&
                new Date(booking.departureTime).getTime() > currentTime;

              return (
                <article
                  key={booking.id}
                  className="overflow-hidden rounded-3xl border border-white/10 bg-[#0d1b2e] shadow-xl shadow-black/10 transition hover:border-white/15"
                >
                  <div className="p-6 md:p-7">
                    <div className="flex flex-col justify-between gap-5 md:flex-row md:items-start">
                      <div>
                        <div className="flex flex-wrap items-center gap-3">
                          <BookingStatusBadge status={booking.status} />
                          <span className="text-xs text-slate-500">
                            Requested {formatDate(booking.createdAt)}
                          </span>
                        </div>

                        <div className="mt-5 flex flex-wrap items-center gap-3">
                          <h2 className="text-2xl font-bold text-white">{booking.origin}</h2>

                          <svg
                            className="h-5 w-5 text-blue-400"
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

                          <h2 className="text-2xl font-bold text-white">{booking.destination}</h2>
                        </div>

                        <p className="mt-2 text-sm text-slate-400">
                          {formatDate(booking.departureTime)}
                        </p>
                      </div>

                      <div className="rounded-2xl border border-white/10 bg-white/5 px-5 py-4 md:text-right">
                        <p className="text-2xl font-bold text-white">
                          {booking.pricePerSeat * booking.requestedSeats} RSD
                        </p>
                        <p className="mt-1 text-xs text-slate-400">
                          {booking.requestedSeats} {booking.requestedSeats === 1 ? 'seat' : 'seats'}{' '}
                          · {booking.pricePerSeat} RSD each
                        </p>
                      </div>
                    </div>

                    <div className="mt-6 grid gap-4 border-t border-white/10 pt-6 sm:grid-cols-3">
                      <DetailItem label="Driver" value={booking.driverName} />
                      <DetailItem label="Requested seats" value={String(booking.requestedSeats)} />
                      <DetailItem label="Ride status" value={booking.rideStatus.toLowerCase()} />
                    </div>

                    <div className="mt-6 flex flex-wrap gap-3">
                      <Link
                        to={`/rides/${booking.rideId}`}
                        className="rounded-xl border border-white/10 px-4 py-2.5 text-sm font-semibold text-slate-300 transition hover:bg-white/10 hover:text-white"
                      >
                        View ride
                      </Link>

                      {canCancel && (
                        <button
                          type="button"
                          onClick={() => {
                            setError('');
                            setSuccessMessage('');
                            setBookingToCancel(booking);
                          }}
                          disabled={processingBookingId === booking.id}
                          className="rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-2.5 text-sm font-semibold text-red-400 transition hover:bg-red-500/20 hover:text-red-300 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                          {processingBookingId === booking.id ? 'Cancelling...' : 'Cancel booking'}
                        </button>
                      )}
                    </div>
                  </div>
                </article>
              );
            })
          )}
        </div>
      </section>

      {bookingToCancel && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-5 backdrop-blur-sm">
          <div className="w-full max-w-md rounded-3xl border border-white/10 bg-[#0d1b2e] p-7 shadow-2xl">
            <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-red-500/10">
              <svg
                className="h-7 w-7 text-red-400"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M12 9v4" />
                <path d="M12 17h.01" />
                <path d="M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z" />
              </svg>
            </div>

            <h2 className="mt-5 text-center text-2xl font-bold text-white">Cancel booking?</h2>

            <p className="mt-3 text-center text-sm leading-6 text-slate-400">
              Your request for {bookingToCancel.requestedSeats}{' '}
              {bookingToCancel.requestedSeats === 1 ? 'seat' : 'seats'} from{' '}
              {bookingToCancel.origin} to {bookingToCancel.destination} will be cancelled.
            </p>

            <div className="mt-7 flex gap-3">
              <button
                type="button"
                onClick={() => setBookingToCancel(null)}
                className="flex-1 rounded-xl border border-white/10 py-3 font-semibold text-slate-300 transition hover:bg-white/10 hover:text-white"
              >
                Keep booking
              </button>

              <button
                type="button"
                onClick={() => void handleCancelBooking()}
                disabled={processingBookingId !== null}
                className="flex-1 rounded-xl bg-red-500 py-3 font-semibold text-white transition hover:bg-red-600 disabled:cursor-not-allowed disabled:opacity-60"
              >
                Confirm cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}

function StatCard({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-[#0d1b2e] p-5">
      <p className="text-sm text-slate-400">{label}</p>
      <p className="mt-2 text-3xl font-bold text-white">{value}</p>
    </div>
  );
}

function DetailItem({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">{label}</p>
      <p className="mt-1.5 font-medium capitalize text-slate-200">{value}</p>
    </div>
  );
}

function LoadingState({ text }: { text: string }) {
  return (
    <div className="flex items-center gap-3 py-10 text-slate-400">
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
      {text}
    </div>
  );
}
