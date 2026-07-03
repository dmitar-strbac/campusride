import type { FormEvent } from 'react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ridesApi } from '../api/ridesApi';
import { useAuth } from '../context/useAuth';

export function CreateRidePage() {
  const { token } = useAuth();
  const navigate = useNavigate();

  const [origin, setOrigin] = useState('');
  const [destination, setDestination] = useState('');
  const [departureTime, setDepartureTime] = useState('');
  const [availableSeats, setAvailableSeats] = useState(3);
  const [pricePerSeat, setPricePerSeat] = useState(1000);
  const [description, setDescription] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [minDateTime] = useState(() => new Date(Date.now() + 60 * 1000).toISOString().slice(0, 16));

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();

    if (!token) {
      setError('You must be logged in to offer a ride.');
      return;
    }

    setError('');
    setIsSubmitting(true);

    try {
      const ride = await ridesApi.createRide(
        {
          origin,
          destination,
          departureTime,
          availableSeats,
          pricePerSeat,
          description: description || undefined,
        },
        token,
      );

      navigate(`/rides/${ride.id}`);
    } catch {
      setError('Could not create ride. Please check the entered data.');
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="min-h-[calc(100vh-73px)] bg-[#060e1a] px-6 py-10">
      <section className="mx-auto max-w-2xl">
        <div className="mt-1">
          <p className="text-sm font-semibold uppercase tracking-[0.25em] text-blue-400">
            New ride
          </p>
          <h1 className="mt-2 text-3xl font-bold text-white">Offer a ride</h1>
          <p className="mt-2 text-slate-400">
            Fill in the trip details so students can find and book a seat.
          </p>
        </div>

        {error && (
          <div className="mt-6 rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-3 text-sm text-red-400">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="mt-8 space-y-3">
          <div className="rounded-2xl border border-white/10 bg-[#0d1b2e]">
            <div className="px-5 py-4">
              <p className="text-[10px] font-semibold uppercase tracking-widest text-slate-500">
                Route
              </p>
              <div className="mt-4 flex items-start gap-4">
                <div className="flex shrink-0 flex-col items-center gap-1 pt-2">
                  <div className="h-2.5 w-2.5 rounded-full border-2 border-blue-400 bg-blue-400" />
                  <div className="h-14 w-px bg-white/10" />
                  <div className="h-2.5 w-2.5 rounded-full bg-emerald-400" />
                </div>
                <div className="flex-1 space-y-3">
                  <div>
                    <label className="text-[10px] font-semibold uppercase tracking-widest text-slate-500">
                      From
                    </label>
                    <input
                      required
                      value={origin}
                      onChange={(e) => setOrigin(e.target.value)}
                      placeholder="Novi Sad"
                      className="mt-1 w-full bg-transparent text-base font-medium text-white outline-none placeholder:text-slate-700 focus:placeholder:text-slate-600"
                    />
                  </div>
                  <div className="border-t border-white/10 pt-2">
                    <label className="text-[10px] font-semibold uppercase tracking-widest text-slate-500">
                      To
                    </label>
                    <input
                      required
                      value={destination}
                      onChange={(e) => setDestination(e.target.value)}
                      placeholder="Belgrade"
                      className="mt-1 w-full bg-transparent text-base font-medium text-white outline-none placeholder:text-slate-700 focus:placeholder:text-slate-600"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="rounded-2xl border border-white/10 bg-[#0d1b2e] px-5 py-4">
            <label
              className="text-[10px] font-semibold uppercase tracking-widest text-slate-500"
              htmlFor="departureTime"
            >
              Departure
            </label>
            <div className="mt-3 flex items-center gap-3">
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
              <input
                id="departureTime"
                type="datetime-local"
                required
                min={minDateTime}
                value={departureTime}
                onChange={(e) => setDepartureTime(e.target.value)}
                className="w-full bg-transparent text-base font-medium text-white outline-none [color-scheme:dark]"
              />
            </div>
          </div>

          <div className="grid gap-3 md:grid-cols-2">
            <div className="rounded-2xl border border-white/10 bg-[#0d1b2e] px-5 py-4">
              <p className="text-[10px] font-semibold uppercase tracking-widest text-slate-500">
                Available seats
              </p>
              <div className="mt-3 flex items-center justify-between">
                <button
                  type="button"
                  onClick={() => setAvailableSeats((s) => Math.max(1, s - 1))}
                  className="flex h-9 w-9 items-center justify-center rounded-full border border-white/20 text-slate-400 transition hover:border-white/40 hover:text-white active:scale-95"
                >
                  <svg
                    className="h-4 w-4"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2.5"
                  >
                    <line x1="5" y1="12" x2="19" y2="12" />
                  </svg>
                </button>
                <div className="text-center">
                  <span className="text-3xl font-bold text-white">{availableSeats}</span>
                  <p className="mt-0.5 text-xs text-slate-500">
                    {availableSeats === 1 ? 'seat' : 'seats'}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() => setAvailableSeats((s) => Math.min(8, s + 1))}
                  className="flex h-9 w-9 items-center justify-center rounded-full border border-white/20 text-slate-400 transition hover:border-white/40 hover:text-white active:scale-95"
                >
                  <svg
                    className="h-4 w-4"
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

            <div className="rounded-2xl border border-white/10 bg-[#0d1b2e] px-5 py-4">
              <p className="text-[10px] font-semibold uppercase tracking-widest text-slate-500">
                Price per seat
              </p>
              <div className="mt-3 flex items-center gap-3">
                <button
                  type="button"
                  onClick={() => setPricePerSeat((p) => Math.max(0, p - 100))}
                  className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full border border-white/20 text-slate-400 transition hover:border-white/40 hover:text-white active:scale-95"
                >
                  <svg
                    className="h-4 w-4"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2.5"
                  >
                    <line x1="5" y1="12" x2="19" y2="12" />
                  </svg>
                </button>
                <div className="flex flex-1 items-baseline justify-center gap-1">
                  <input
                    type="number"
                    required
                    min={0}
                    step={100}
                    value={pricePerSeat}
                    onChange={(e) => setPricePerSeat(Number(e.target.value))}
                    className="w-24 bg-transparent text-center text-3xl font-bold text-white outline-none [appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none"
                  />
                  <span className="text-sm font-medium text-slate-500">RSD</span>
                </div>
                <button
                  type="button"
                  onClick={() => setPricePerSeat((p) => p + 100)}
                  className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full border border-white/20 text-slate-400 transition hover:border-white/40 hover:text-white active:scale-95"
                >
                  <svg
                    className="h-4 w-4"
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

          <div className="rounded-2xl border border-white/10 bg-[#0d1b2e] px-5 py-4">
            <label
              className="text-[10px] font-semibold uppercase tracking-widest text-slate-500"
              htmlFor="description"
            >
              Notes
              <span className="ml-2 normal-case tracking-normal text-slate-600">(optional)</span>
            </label>
            <textarea
              id="description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Meeting point, luggage info, any extra details..."
              rows={3}
              className="mt-3 w-full resize-none bg-transparent text-sm text-white outline-none placeholder:text-slate-700 focus:placeholder:text-slate-600"
            />
          </div>

          <button
            disabled={isSubmitting}
            className="flex w-full items-center justify-center gap-2 rounded-xl bg-blue-500 px-5 py-3.5 font-semibold text-white shadow-lg shadow-blue-500/20 transition hover:bg-blue-600 active:scale-[0.99] disabled:cursor-not-allowed disabled:opacity-70"
          >
            {isSubmitting ? (
              <>
                <svg className="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
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
                Creating ride...
              </>
            ) : (
              'Publish ride'
            )}
          </button>
        </form>
      </section>
    </main>
  );
}
