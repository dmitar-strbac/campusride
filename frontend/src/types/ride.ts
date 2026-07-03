export type RideStatus = 'ACTIVE' | 'CANCELLED' | 'COMPLETED';

export type Ride = {
  id: number;
  driverId: number;
  driverName: string;
  origin: string;
  destination: string;
  departureTime: string;
  availableSeats: number;
  pricePerSeat: number;
  description?: string;
  status: RideStatus;
};

export type CreateRideRequest = {
  origin: string;
  destination: string;
  departureTime: string;
  availableSeats: number;
  pricePerSeat: number;
  description?: string;
};
