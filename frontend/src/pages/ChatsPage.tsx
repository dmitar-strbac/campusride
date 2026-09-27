import { ArrowRight, CarFront, MessageCircle, Search } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { bookingsApi } from '../api/bookingsApi';
import { chatApi } from '../api/chatApi';
import { ridesApi } from '../api/ridesApi';
import { useAuth } from '../context/useAuth';
import { useChatUnread } from '../context/useChatUnread';
import type { ChatMessage } from '../types/chat';

type Conversation = {
  rideId: number;
  origin: string;
  destination: string;
  departureTime: string;
  driverName: string;
  role: 'Driver' | 'Passenger';
  lastMessage: ChatMessage | null;
};

function formatDate(value: string) {
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(value));
}

export function ChatsPage() {
  const { token, user } = useAuth();
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { counts } = useChatUnread();

  useEffect(() => {
    if (!token || !user) return;

    let active = true;

    async function load() {
      try {
        const [rides, bookings] = await Promise.all([
          ridesApi.getMyOfferedRides(token!),
          bookingsApi.getMyBookings(token!),
        ]);

        const byRide = new Map<number, Conversation>();

        for (const ride of rides) {
          byRide.set(ride.id, {
            rideId: ride.id,
            origin: ride.origin,
            destination: ride.destination,
            departureTime: ride.departureTime,
            driverName: ride.driverName,
            role: 'Driver',
            lastMessage: null,
          });
        }

        for (const booking of bookings) {
          if (booking.status !== 'ACCEPTED') continue;

          byRide.set(booking.rideId, {
            rideId: booking.rideId,
            origin: booking.origin,
            destination: booking.destination,
            departureTime: booking.departureTime,
            driverName: booking.driverName,
            role: 'Passenger',
            lastMessage: null,
          });
        }

        const items = [...byRide.values()];

        const withPreviews = await Promise.all(
          items.map(async (item) => {
            try {
              const latest = await chatApi.getMessages(item.rideId, token!);
              return {
                ...item,
                lastMessage: latest[0] ?? null,
              };
            } catch {
              // One inaccessible ride must not hide the other conversations.
              return item;
            }
          }),
        );

        if (active) setConversations(withPreviews);
      } catch {
        if (active) setError('Conversations could not be loaded.');
      } finally {
        if (active) setLoading(false);
      }
    }

    void load();

    return () => {
      active = false;
    };
  }, [token, user]);

  const visible = useMemo(() => {
    const query = search.trim().toLowerCase();

    return conversations
      .filter((item) =>
        `${item.origin} ${item.destination} ${item.driverName}`.toLowerCase().includes(query),
      )
      .sort((a, b) => {
        const aTime = a.lastMessage?.createdAt ?? a.departureTime;
        const bTime = b.lastMessage?.createdAt ?? b.departureTime;
        return new Date(bTime).getTime() - new Date(aTime).getTime();
      });
  }, [conversations, search]);

  return (
    <main className="min-h-[calc(100vh-73px)] bg-[#060e1a] px-6 py-10">
      <section className="mx-auto max-w-6xl">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.25em] text-blue-400">
            My messages
          </p>
          <h1 className="mt-3 text-4xl font-bold text-white">Your messages</h1>
          <p className="mt-3 max-w-2xl text-slate-400">All your ride conversations in one place.</p>
        </div>

        <div className="mt-7 overflow-hidden rounded-3xl border border-white/10 bg-[#0d1b2e] shadow-2xl">
          <div className="border-b border-white/10 p-4 sm:p-5">
            <div className="flex items-center gap-3 rounded-2xl border border-white/10 bg-[#091525] px-4 focus-within:border-blue-500/50">
              <Search size={18} className="text-slate-500" />
              <input
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Search conversations"
                aria-label="Search conversations"
                className="w-full bg-transparent py-3 text-sm text-white outline-none placeholder:text-slate-500"
              />
            </div>
          </div>

          {loading ? (
            <p className="p-10 text-center text-sm text-slate-400">Loading conversations...</p>
          ) : error ? (
            <div className="p-10 text-center">
              <p className="text-sm text-red-300">{error}</p>
              <button
                type="button"
                onClick={() => window.location.reload()}
                className="mt-3 text-sm font-semibold text-blue-400 hover:text-blue-300"
              >
                Try again
              </button>
            </div>
          ) : visible.length === 0 ? (
            <div className="px-6 py-16 text-center">
              <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl border border-blue-500/20 bg-blue-500/10">
                <MessageCircle size={24} className="text-blue-400" />
              </div>
              <h2 className="mt-4 font-semibold text-white">
                {search ? 'No matching conversations' : 'No conversations yet'}
              </h2>
              <p className="mt-2 text-sm text-slate-400">
                {search
                  ? 'Try searching for a different city or driver.'
                  : 'Your offered rides and accepted bookings will appear here.'}
              </p>
              {!search && (
                <Link
                  to="/rides"
                  className="mt-5 inline-block text-sm font-semibold text-blue-400 hover:text-blue-300"
                >
                  Browse rides
                </Link>
              )}
            </div>
          ) : (
            <div className="divide-y divide-white/5">
              {visible.map((item) => (
                <Link
                  key={item.rideId}
                  to={`/rides/${item.rideId}/chat`}
                  className="group flex items-center gap-4 px-5 py-5 transition hover:bg-white/[0.04] sm:px-6"
                >
                  <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl border border-blue-500/20 bg-blue-500/10">
                    <CarFront size={23} className="text-blue-400" />
                  </div>

                  <div className="min-w-0 flex-1">
                    <div className="flex min-w-0 items-center gap-2">
                      <h2 className="truncate font-semibold text-white">
                        {item.origin} → {item.destination}
                      </h2>
                      {(counts[item.rideId] ?? 0) > 0 && (
                        <span className="flex h-5 min-w-5 shrink-0 items-center justify-center rounded-full bg-blue-500 px-1 text-[10px] font-bold text-white">
                          {(counts[item.rideId] ?? 0) > 99 ? '99+' : counts[item.rideId]}
                        </span>
                      )}
                      <span className="shrink-0 text-xs text-slate-500">
                        {formatDate(item.lastMessage?.createdAt ?? item.departureTime)}
                      </span>
                    </div>

                    <p className="mt-1 truncate text-sm text-slate-400">
                      {item.lastMessage
                        ? `${item.lastMessage.senderId === user?.id ? 'You' : item.lastMessage.senderName}: ${item.lastMessage.content}`
                        : `${item.role === 'Driver' ? 'Your ride' : `Driver: ${item.driverName}`} · No messages yet`}
                    </p>

                    <p className="mt-1.5 text-xs text-slate-500">
                      {item.role} · {formatDate(item.departureTime)} departure
                    </p>
                  </div>

                  <ArrowRight
                    size={17}
                    className="hidden shrink-0 text-slate-600 transition group-hover:translate-x-1 group-hover:text-blue-400 sm:block"
                  />
                </Link>
              ))}
            </div>
          )}
        </div>
      </section>
    </main>
  );
}
