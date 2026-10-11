import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { chatApi, type UnreadCount } from '../api/chatApi';
import { subscribeRealtime } from '../lib/realtime';
import { ChatUnreadContext } from './chatUnreadContext';
import { useAuth } from './useAuth';

export function ChatUnreadProvider({ children }: { children: ReactNode }) {
  const { token } = useAuth();

  const [counts, setCounts] = useState<Record<number, number>>({});

  const active = useRef(true);
  const revision = useRef(0);
  const refreshing = useRef(false);
  const refreshAgain = useRef(false);

  const refresh = useCallback(async () => {
    if (!token || !active.current) return;

    if (refreshing.current) {
      refreshAgain.current = true;
      return;
    }

    refreshing.current = true;

    try {
      do {
        refreshAgain.current = false;
        const startingRevision = revision.current;

        const items = await chatApi.getUnreadCounts(token);

        if (!active.current) return;

        if (startingRevision !== revision.current) {
          refreshAgain.current = true;
          continue;
        }

        setCounts(Object.fromEntries(items.map((item) => [item.rideId, item.unreadCount])));
      } while (refreshAgain.current && active.current);
    } finally {
      refreshing.current = false;
    }
  }, [token]);

  const markRead = useCallback(
    async (rideId: number, messageId: number) => {
      if (!token || !active.current) return;

      await chatApi.markRead(rideId, messageId, token);

      if (!active.current) return;

      revision.current += 1;
      await refresh();
    },
    [token, refresh],
  );

  useEffect(() => {
    active.current = true;
    if (!token) return;

    const safeRefresh = () => {
      void refresh().catch(() => {
        // A temporary API error must not interrupt the connection.
      });
    };

    safeRefresh();

    const unsubscribe = subscribeRealtime(
      token,
      '/user/queue/chats/unread',
      (frame) => {
        if (!active.current) return;

        try {
          const item = JSON.parse(frame.body) as UnreadCount;

          if (!Number.isSafeInteger(item.rideId) || item.rideId < 1) return;
          if (!Number.isSafeInteger(item.unreadCount) || item.unreadCount < 0) return;

          revision.current += 1;

          setCounts((current) => ({
            ...current,
            [item.rideId]: item.unreadCount,
          }));
        } catch {
          // Ignore invalid transient messages.
        }
      },
      safeRefresh,
    );

    window.addEventListener('focus', safeRefresh);

    return () => {
      active.current = false;
      revision.current += 1;
      window.removeEventListener('focus', safeRefresh);
      unsubscribe();
    };
  }, [token, refresh]);

  const value = useMemo(
    () => ({
      counts,
      total: Object.values(counts).reduce((sum, count) => sum + count, 0),
      refresh,
      markRead,
    }),
    [counts, refresh, markRead],
  );

  return <ChatUnreadContext.Provider value={value}>{children}</ChatUnreadContext.Provider>;
}
