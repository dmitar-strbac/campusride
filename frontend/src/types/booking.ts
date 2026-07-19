import type { RideStatus } from './ride';

export type BookingStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED';

export type Booking = {
  id: number;
  rideId: number;
  driverId: number;
  driverName: string;
  passengerId: number;
  passengerName: string;
  origin: string;
  destination: string;
  departureTime: string;
  requestedSeats: number;
  availableSeats: number;
  pricePerSeat: number;
  rideStatus: RideStatus;
  status: BookingStatus;
  createdAt: string;
};

export type CreateBookingRequest = {
  requestedSeats: number;
};
