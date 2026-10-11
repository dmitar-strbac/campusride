import type {
  AppNotification,
  NotificationPage,
  UnreadNotificationCount,
} from '../types/notification';
import { apiClient } from './apiClient';

const auth = (token: string) => ({
  headers: { Authorization: `Bearer ${token}` },
});

export const notificationsApi = {
  async getRecent(token: string): Promise<NotificationPage> {
    const response = await apiClient.get<NotificationPage>('/notifications', {
      ...auth(token),
      params: { page: 0, size: 20 },
    });
    return response.data;
  },

  async getUnreadCount(token: string): Promise<UnreadNotificationCount> {
    const response = await apiClient.get<UnreadNotificationCount>(
      '/notifications/unread-count',
      auth(token),
    );
    return response.data;
  },

  async markRead(id: number, token: string): Promise<AppNotification> {
    const response = await apiClient.patch<AppNotification>(
      `/notifications/${id}/read`,
      null,
      auth(token),
    );
    return response.data;
  },

  async markAllRead(token: string): Promise<UnreadNotificationCount> {
    const response = await apiClient.patch<UnreadNotificationCount>(
      '/notifications/read-all',
      null,
      auth(token),
    );
    return response.data;
  },
};
