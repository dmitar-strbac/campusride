import type { ChatMessage } from '../types/chat';
import { apiClient } from './apiClient';

export type UnreadCount = {
  rideId: number;
  unreadCount: number;
};

export const chatApi = {
  async getMessages(rideId: number, token: string, beforeId?: number): Promise<ChatMessage[]> {
    const response = await apiClient.get<ChatMessage[]>(`/rides/${rideId}/messages`, {
      headers: { Authorization: `Bearer ${token}` },
      params: {
        limit: 30,
        beforeId,
      },
    });

    return response.data;
  },

  async getUnreadCounts(token: string): Promise<UnreadCount[]> {
    const response = await apiClient.get<UnreadCount[]>('/chats/unread', {
      headers: { Authorization: `Bearer ${token}` },
    });
    return response.data;
  },

  async markRead(rideId: number, lastReadMessageId: number, token: string): Promise<UnreadCount> {
    const response = await apiClient.patch<UnreadCount>(
      `/rides/${rideId}/messages/read`,
      { lastReadMessageId },
      { headers: { Authorization: `Bearer ${token}` } },
    );
    return response.data;
  },
};
