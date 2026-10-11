import { CalendarX2, CheckCheck, CircleX, TicketCheck, UserMinus } from 'lucide-react';
import type { AppNotification } from '../../types/notification';

export function notificationPresentation(item: AppNotification) {
  const actor = item.actorName || 'Someone';

  switch (item.type) {
    case 'BOOKING_REQUESTED':
      return {
        title: 'New booking request',
        description: `${actor} requested ${item.requestedSeats ?? 1} seat(s).`,
        action: 'Review request',
        href: `/rides/${item.rideId}/bookings`,
        icon: TicketCheck,
        color: 'text-blue-300',
        background: 'bg-blue-500/15',
      };

    case 'BOOKING_ACCEPTED':
      return {
        title: 'You’re going!',
        description: 'Your booking request has been accepted.',
        action: 'View booking',
        href: '/bookings/my',
        icon: CheckCheck,
        color: 'text-emerald-300',
        background: 'bg-emerald-500/15',
      };

    case 'BOOKING_REJECTED':
      return {
        title: 'Booking update',
        description: 'Your booking request has been declined.',
        action: 'View booking',
        href: '/bookings/my',
        icon: CircleX,
        color: 'text-amber-300',
        background: 'bg-amber-500/15',
      };

    case 'BOOKING_CANCELLED':
      return {
        title: 'Passenger cancelled',
        description: `${actor} cancelled their booking.`,
        action: 'View requests',
        href: `/rides/${item.rideId}/bookings`,
        icon: UserMinus,
        color: 'text-amber-300',
        background: 'bg-amber-500/15',
      };

    case 'RIDE_CANCELLED':
      return {
        title: 'Ride cancelled',
        description: 'The driver cancelled this ride.',
        action: 'View ride',
        href: `/rides/${item.rideId}`,
        icon: CalendarX2,
        color: 'text-rose-300',
        background: 'bg-rose-500/15',
      };
  }
}

export function notificationDate(value: string) {
  const date = new Date(value);
  if (!Number.isFinite(date.getTime())) return '';

  return new Intl.DateTimeFormat('en-GB', {
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}
