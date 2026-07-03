import type { CreateRideRequest, Ride } from '../types/ride';
import { apiClient } from './apiClient';

function authHeader(token: string) {
  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };
}

export const ridesApi = {
  async createRide(data: CreateRideRequest, token: string): Promise<Ride> {
    const response = await apiClient.post<Ride>('/rides', data, authHeader(token));
    return response.data;
  },

  async searchRides(origin?: string, destination?: string, token?: string): Promise<Ride[]> {
    const response = await apiClient.get<Ride[]>('/rides', {
      headers: token ? { Authorization: `Bearer ${token}` } : undefined,
      params: {
        origin: origin || undefined,
        destination: destination || undefined,
      },
    });

    return response.data;
  },

  async getRide(id: number, token: string): Promise<Ride> {
    const response = await apiClient.get<Ride>(`/rides/${id}`, authHeader(token));
    return response.data;
  },

  async getMyOfferedRides(token: string): Promise<Ride[]> {
    const response = await apiClient.get<Ride[]>('/rides/my', authHeader(token));
    return response.data;
  },

  async cancelRide(id: number, token: string): Promise<Ride> {
    const response = await apiClient.patch<Ride>(`/rides/${id}/cancel`, null, authHeader(token));
    return response.data;
  },
};
