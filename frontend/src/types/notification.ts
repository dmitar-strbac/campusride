export type NotificationType =
  | 'BOOKING_REQUESTED'
  | 'BOOKING_ACCEPTED'
  | 'BOOKING_REJECTED'
  | 'BOOKING_CANCELLED'
  | 'RIDE_CANCELLED';

export type AppNotification = {
  id: number;
  type: NotificationType;
  rideId: number;
  bookingId: number | null;
  actorName: string | null;
  origin: string;
  destination: string;
  requestedSeats: number | null;
  createdAt: string;
  readAt: string | null;
};

export type NotificationPage = {
  content: AppNotification[];
  totalElements: number;
  totalPages: number;
  number: number;
  last: boolean;
};

export type UnreadNotificationCount = {
  count: number;
};
