import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { bookingsApi, getApiErrorMessage } from '../api/bookingsApi';
import { ridesApi } from '../api/ridesApi';
import { useAuth } from '../context/useAuth';
import type { Booking, BookingStatus } from '../types/booking';
import type { Ride } from '../types/ride';

function formatDate(value: string) {
  return new Intl.DateTimeFormat('en', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

function StatusBadge({ status }: { status: BookingStatus }) {
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

export function RideBookingRequestsPage() {
  const { rideId } = useParams();
  const { token } = useAuth();

  const [bookings, setBookings] = useState<Booking[]>([]);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [processingBookingId, setProcessingBookingId] = useState<number | null>(null);
  const [ride, setRide] = useState<Ride | null>(null);

  useEffect(() => {
    async function loadRequests() {
      if (!token || !rideId) {
        setIsLoading(false);
        return;
      }

      try {
        const numericRideId = Number(rideId);

        const [rideData, bookingData] = await Promise.all([
          ridesApi.getRide(numericRideId, token),
          bookingsApi.getRideBookingRequests(numericRideId, token),
        ]);

        setRide(rideData);
        setBookings(bookingData);
      } catch (requestError) {
        setError(
          getApiErrorMessage(requestError, 'Could not load booking requests for this ride.'),
        );
      } finally {
        setIsLoading(false);
      }
    }

    void loadRequests();
  }, [rideId, token]);

  const pendingCount = useMemo(
    () => bookings.filter((booking) => booking.status === 'PENDING').length,
    [bookings],
  );

  async function handleAction(bookingId: number, action: 'accept' | 'reject') {
    if (!token) {
      return;
    }

    setProcessingBookingId(bookingId);
    setError('');
    setSuccessMessage('');

    try {
      const updated =
        action === 'accept'
          ? await bookingsApi.acceptBooking(bookingId, token)
          : await bookingsApi.rejectBooking(bookingId, token);

      setBookings((current) =>
        current.map((booking) => {
          if (booking.id === bookingId) {
            return updated;
          }

          if (action === 'accept') {
            return {
              ...booking,
              availableSeats: updated.availableSeats,
            };
          }

          return booking;
        }),
      );

      setSuccessMessage(
        action === 'accept'
          ? `${updated.passengerName}'s booking has been accepted.`
          : `${updated.passengerName}'s booking has been rejected.`,
      );
    } catch (requestError) {
      setError(
        getApiErrorMessage(
          requestError,
          action === 'accept' ? 'Booking could not be accepted.' : 'Booking could not be rejected.',
        ),
      );
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
              Driver dashboard
            </p>
            <h1 className="mt-3 text-4xl font-bold text-white">Booking requests</h1>

            {ride ? (
              <p className="mt-3 text-slate-400">
                {ride.origin} → {ride.destination} · {formatDate(ride.departureTime)}
              </p>
            ) : (
              <p className="mt-3 text-slate-400">
                Review students who requested a seat on your ride.
              </p>
            )}
          </div>

          <div className="flex gap-4">
            <div className="rounded-2xl border border-amber-500/20 bg-amber-500/10 px-5 py-4">
              <p className="text-xs font-semibold uppercase tracking-wider text-amber-400/80">
                Awaiting decision
              </p>
              <p className="mt-1 text-3xl font-bold text-amber-300">{pendingCount}</p>
            </div>

            <div className="rounded-2xl border border-blue-500/20 bg-blue-500/10 px-5 py-4">
              <p className="text-xs font-semibold uppercase tracking-wider text-blue-400/80">
                Seats remaining
              </p>
              <p className="mt-1 text-3xl font-bold text-blue-300">{ride?.availableSeats ?? 0}</p>
            </div>
          </div>
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
              Loading booking requests...
            </div>
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
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                  <circle cx="9" cy="7" r="4" />
                  <path d="M23 21v-2a4 4 0 0 0-3-3.9" />
                  <path d="M16 3.1a4 4 0 0 1 0 7.8" />
                </svg>
              </div>

              <h2 className="mt-5 text-xl font-bold text-white">No booking requests yet</h2>

              <p className="mt-2 text-sm text-slate-400">
                New requests from students will appear here.
              </p>
            </div>
          ) : (
            bookings.map((booking) => (
              <article
                key={booking.id}
                className="rounded-3xl border border-white/10 bg-[#0d1b2e] p-6 shadow-xl shadow-black/10 md:p-7"
              >
                <div className="flex flex-col justify-between gap-6 md:flex-row md:items-center">
                  <div className="flex items-start gap-4">
                    <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-blue-500/15 text-sm font-bold text-blue-300">
                      {booking.passengerName
                        .split(' ')
                        .map((part) => part.charAt(0))
                        .slice(0, 2)
                        .join('')
                        .toUpperCase()}
                    </div>

                    <div>
                      <div className="flex flex-wrap items-center gap-3">
                        <h2 className="text-xl font-bold text-white">{booking.passengerName}</h2>
                        <StatusBadge status={booking.status} />
                      </div>

                      <p className="mt-1.5 text-sm text-slate-400">
                        Requested {booking.requestedSeats}{' '}
                        {booking.requestedSeats === 1 ? 'seat' : 'seats'}
                      </p>

                      <p className="mt-1 text-xs text-slate-500">
                        Sent {formatDate(booking.createdAt)}
                      </p>
                    </div>
                  </div>

                  <div className="flex flex-wrap items-center gap-3">
                    <div className="mr-2">
                      <p className="text-xs uppercase tracking-wider text-slate-500">
                        Booking value
                      </p>
                      <p className="mt-1 font-bold text-white">
                        {booking.pricePerSeat * booking.requestedSeats} RSD
                      </p>
                    </div>

                    {booking.status === 'PENDING' && (
                      <>
                        <button
                          type="button"
                          onClick={() => void handleAction(booking.id, 'reject')}
                          disabled={processingBookingId !== null}
                          className="rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-2.5 text-sm font-semibold text-red-400 transition hover:bg-red-500/20 hover:text-red-300 disabled:cursor-not-allowed disabled:opacity-50"
                        >
                          {processingBookingId === booking.id ? 'Processing...' : 'Reject'}
                        </button>

                        <button
                          type="button"
                          onClick={() => void handleAction(booking.id, 'accept')}
                          disabled={processingBookingId !== null}
                          className="rounded-xl bg-emerald-500 px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-emerald-500/20 transition hover:bg-emerald-600 disabled:cursor-not-allowed disabled:opacity-50"
                        >
                          {processingBookingId === booking.id ? 'Processing...' : 'Accept'}
                        </button>
                      </>
                    )}
                  </div>
                </div>
              </article>
            ))
          )}
        </div>
      </section>
    </main>
  );
}
