import axios from 'axios';
import type { Booking, CreateBookingRequest } from '../types/booking';
import { apiClient } from './apiClient';

function authHeader(token: string) {
  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };
}

export function getApiErrorMessage(error: unknown, fallback: string): string {
  if (
    axios.isAxiosError(error) &&
    error.response?.data &&
    typeof error.response.data === 'object' &&
    'message' in error.response.data &&
    typeof error.response.data.message === 'string'
  ) {
    return error.response.data.message;
  }

  return fallback;
}

export const bookingsApi = {
  async requestBooking(
    rideId: number,
    data: CreateBookingRequest,
    token: string,
  ): Promise<Booking> {
    const response = await apiClient.post<Booking>(
      `/rides/${rideId}/bookings`,
      data,
      authHeader(token),
    );

    return response.data;
  },

  async getMyBookings(token: string): Promise<Booking[]> {
    const response = await apiClient.get<Booking[]>('/bookings/my', authHeader(token));
    return response.data;
  },

  async getRideBookingRequests(rideId: number, token: string): Promise<Booking[]> {
    const response = await apiClient.get<Booking[]>(`/rides/${rideId}/bookings`, authHeader(token));

    return response.data;
  },

  async acceptBooking(bookingId: number, token: string): Promise<Booking> {
    const response = await apiClient.patch<Booking>(
      `/bookings/${bookingId}/accept`,
      null,
      authHeader(token),
    );

    return response.data;
  },

  async rejectBooking(bookingId: number, token: string): Promise<Booking> {
    const response = await apiClient.patch<Booking>(
      `/bookings/${bookingId}/reject`,
      null,
      authHeader(token),
    );

    return response.data;
  },

  async cancelBooking(bookingId: number, token: string): Promise<Booking> {
    const response = await apiClient.patch<Booking>(
      `/bookings/${bookingId}/cancel`,
      null,
      authHeader(token),
    );

    return response.data;
  },
};
