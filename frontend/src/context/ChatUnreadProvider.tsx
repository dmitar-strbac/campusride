import { Client } from '@stomp/stompjs';
import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import { chatApi, type UnreadCount } from '../api/chatApi';
import { WS_BASE_URL } from '../config/api';
import { ChatUnreadContext } from './chatUnreadContext';
import { useAuth } from './useAuth';

export function ChatUnreadProvider({ children }: { children: ReactNode }) {
  const { token } = useAuth();
  const [counts, setCounts] = useState<Record<number, number>>({});

  const refresh = useCallback(async () => {
    if (!token) return;

    const items = await chatApi.getUnreadCounts(token);
    setCounts(Object.fromEntries(items.map((item) => [item.rideId, item.unreadCount])));
  }, [token]);

  const markRead = useCallback(
    async (rideId: number, messageId: number) => {
      if (!token) return;

      const item = await chatApi.markRead(rideId, messageId, token);
      setCounts((current) => ({
        ...current,
        [item.rideId]: item.unreadCount,
      }));
    },
    [token],
  );

  useEffect(() => {
    if (!token) return;

    let active = true;

    void chatApi
      .getUnreadCounts(token)
      .then((items) => {
        if (active) {
          setCounts(Object.fromEntries(items.map((item) => [item.rideId, item.unreadCount])));
        }
      })
      .catch(() => {
        // Navbar stays usable if the unread endpoint is temporarily down.
      });

    const client = new Client({
      webSocketFactory: () => new WebSocket(WS_BASE_URL),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        if (!active) return;

        client.subscribe('/user/queue/chats/unread', (frame) => {
          if (!active) return;

          try {
            const item = JSON.parse(frame.body) as UnreadCount;
            setCounts((current) => ({
              ...current,
              [item.rideId]: item.unreadCount,
            }));
          } catch {
            // Ignore malformed transient events.
          }
        });

        void chatApi
          .getUnreadCounts(token)
          .then((items) => {
            if (active) {
              setCounts(Object.fromEntries(items.map((item) => [item.rideId, item.unreadCount])));
            }
          })
          .catch(() => {});
      },
    });

    client.activate();

    const onFocus = () => {
      void chatApi
        .getUnreadCounts(token)
        .then((items) => {
          if (active) {
            setCounts(Object.fromEntries(items.map((item) => [item.rideId, item.unreadCount])));
          }
        })
        .catch(() => {});
    };

    window.addEventListener('focus', onFocus);

    return () => {
      active = false;
      window.removeEventListener('focus', onFocus);
      void client.deactivate();
    };
  }, [token]);

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
