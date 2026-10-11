import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { notificationsApi } from '../api/notificationsApi';
import { NotificationToasts } from '../components/notifications/NotificationToasts';
import { subscribeRealtime } from '../lib/realtime';
import type { AppNotification } from '../types/notification';
import { NotificationsContext } from './notificationsContext';
import { useAuth } from './useAuth';

const TYPES = new Set([
  'BOOKING_REQUESTED',
  'BOOKING_ACCEPTED',
  'BOOKING_REJECTED',
  'BOOKING_CANCELLED',
  'RIDE_CANCELLED',
]);

function isNotification(value: unknown): value is AppNotification {
  if (!value || typeof value !== 'object') return false;

  const item = value as Record<string, unknown>;

  return (
    typeof item.id === 'number' &&
    Number.isSafeInteger(item.id) &&
    item.id > 0 &&
    typeof item.rideId === 'number' &&
    Number.isSafeInteger(item.rideId) &&
    item.rideId > 0 &&
    typeof item.type === 'string' &&
    TYPES.has(item.type) &&
    typeof item.origin === 'string' &&
    typeof item.destination === 'string' &&
    typeof item.createdAt === 'string' &&
    Number.isFinite(new Date(item.createdAt).getTime()) &&
    (item.readAt === null || typeof item.readAt === 'string') &&
    (item.actorName === null || typeof item.actorName === 'string') &&
    (item.bookingId === null || typeof item.bookingId === 'number') &&
    (item.requestedSeats === null || typeof item.requestedSeats === 'number')
  );
}

export function NotificationsProvider({ children }: { children: ReactNode }) {
  const { token, user } = useAuth();

  if (!token || !user) {
    return <>{children}</>;
  }

  return (
    <NotificationSession key={token} token={token}>
      {children}
    </NotificationSession>
  );
}

function NotificationSession({ token, children }: { token: string; children: ReactNode }) {
  const [notifications, setNotifications] = useState<AppNotification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [toasts, setToasts] = useState<AppNotification[]>([]);

  const active = useRef(true);
  const revision = useRef(0);
  const refreshing = useRef(false);
  const refreshAgain = useRef(false);
  const seenEvents = useRef(new Set<number>());

  const refresh = useCallback(async () => {
    if (!active.current) return;

    if (refreshing.current) {
      refreshAgain.current = true;
      return;
    }

    refreshing.current = true;

    try {
      do {
        refreshAgain.current = false;
        const startingRevision = revision.current;

        const [page, count] = await Promise.all([
          notificationsApi.getRecent(token),
          notificationsApi.getUnreadCount(token),
        ]);

        if (!active.current) return;

        if (startingRevision !== revision.current) {
          refreshAgain.current = true;
          continue;
        }

        setNotifications(page.content);
        setUnreadCount(count.count);
        setError(null);
      } while (refreshAgain.current && active.current);
    } catch {
      if (active.current) {
        setError('Notifications could not be refreshed. Please try again.');
      }
    } finally {
      refreshing.current = false;
      if (active.current) setLoading(false);
    }
  }, [token]);

  const dismissToast = useCallback((id: number) => {
    setToasts((current) => current.filter((item) => item.id !== id));
  }, []);

  const markRead = useCallback(
    async (id: number) => {
      const updated = await notificationsApi.markRead(id, token);
      if (!active.current) return;

      revision.current += 1;
      setNotifications((current) => current.map((item) => (item.id === id ? updated : item)));
      setToasts((current) => current.filter((item) => item.id !== id));
      await refresh();
    },
    [token, refresh],
  );

  const markAllRead = useCallback(async () => {
    await notificationsApi.markAllRead(token);
    if (!active.current) return;

    revision.current += 1;
    await refresh();
  }, [token, refresh]);

  useEffect(() => {
    active.current = true;

    const initialRefreshTimer = window.setTimeout(() => {
      void refresh();
    }, 0);

    const unsubscribe = subscribeRealtime(
      token,
      '/user/queue/notifications',
      (frame) => {
        if (!active.current) return;

        try {
          const item: unknown = JSON.parse(frame.body);
          if (!isNotification(item)) return;
          if (seenEvents.current.has(item.id)) return;

          seenEvents.current.add(item.id);

          if (seenEvents.current.size > 500) {
            const oldest = seenEvents.current.values().next().value;
            if (oldest !== undefined) seenEvents.current.delete(oldest);
          }

          revision.current += 1;
          setNotifications((current) =>
            [item, ...current.filter((existing) => existing.id !== item.id)].slice(0, 20),
          );

          if (item.readAt === null) {
            setToasts((current) => [...current, item].slice(-3));
          }

          void refresh();
        } catch {
          // Ignore invalid messages.
        }
      },
      () => void refresh(),
    );

    const onFocus = () => void refresh();
    window.addEventListener('focus', onFocus);

    return () => {
      active.current = false;
      window.clearTimeout(initialRefreshTimer);
      window.removeEventListener('focus', onFocus);
      unsubscribe();
    };
  }, [token, refresh]);

  const value = useMemo(
    () => ({
      notifications,
      unreadCount,
      loading,
      error,
      toasts,
      refresh,
      markRead,
      markAllRead,
      dismissToast,
    }),
    [
      notifications,
      unreadCount,
      loading,
      error,
      toasts,
      refresh,
      markRead,
      markAllRead,
      dismissToast,
    ],
  );

  return (
    <NotificationsContext.Provider value={value}>
      {children}
      <NotificationToasts />
    </NotificationsContext.Provider>
  );
}
