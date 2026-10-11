import { createContext } from 'react';
import type { AppNotification } from '../types/notification';

export type NotificationsContextValue = {
  notifications: AppNotification[];
  unreadCount: number;
  loading: boolean;
  error: string | null;
  toasts: AppNotification[];
  refresh: () => Promise<void>;
  markRead: (id: number) => Promise<void>;
  markAllRead: () => Promise<void>;
  dismissToast: (id: number) => void;
};

export const NotificationsContext = createContext<NotificationsContextValue | undefined>(undefined);
