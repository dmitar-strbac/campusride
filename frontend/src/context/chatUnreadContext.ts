import { createContext } from 'react';

export type ChatUnreadValue = {
  counts: Record<number, number>;
  total: number;
  refresh: () => Promise<void>;
  markRead: (rideId: number, messageId: number) => Promise<void>;
};

export const ChatUnreadContext = createContext<ChatUnreadValue | null>(null);
