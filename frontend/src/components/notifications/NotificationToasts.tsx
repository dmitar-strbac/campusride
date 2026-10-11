import { ArrowUpRight, X } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotifications } from '../../context/useNotifications';
import type { AppNotification } from '../../types/notification';
import { notificationPresentation } from './notificationPresentation';

export function NotificationToasts() {
  const { toasts } = useNotifications();

  return (
    <div
      className="pointer-events-none fixed right-4 top-24 z-[100] flex w-[calc(100%-2rem)] max-w-sm flex-col gap-3"
      aria-label="New notifications"
    >
      {toasts.map((notification) => (
        <NotificationToast key={notification.id} notification={notification} />
      ))}
    </div>
  );
}

function NotificationToast({ notification }: { notification: AppNotification }) {
  const { dismissToast, markRead } = useNotifications();
  const navigate = useNavigate();
  const [hovered, setHovered] = useState(false);
  const [focused, setFocused] = useState(false);
  const presentation = notificationPresentation(notification);
  const Icon = presentation.icon;

  useEffect(() => {
    if (hovered || focused) return;

    const timeout = window.setTimeout(() => {
      dismissToast(notification.id);
    }, 8000);

    return () => window.clearTimeout(timeout);
  }, [hovered, focused, notification.id, dismissToast]);

  function openNotification() {
    dismissToast(notification.id);
    void markRead(notification.id).catch(() => {});
    navigate(presentation.href);
  }

  return (
    <article
      className="notification-enter pointer-events-auto relative overflow-hidden rounded-2xl border border-white/15 bg-[#102038]/95 p-4 text-white shadow-[0_16px_50px_rgba(0,0,0,0.4)] backdrop-blur-xl"
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      onFocusCapture={() => setFocused(true)}
      onBlurCapture={(event) => {
        if (!event.currentTarget.contains(event.relatedTarget)) {
          setFocused(false);
        }
      }}
    >
      <div className="flex items-start gap-3">
        <div
          className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl ${presentation.background} ${presentation.color}`}
        >
          <Icon size={22} aria-hidden="true" />
        </div>

        <div className="min-w-0 flex-1">
          <div role="status" aria-live="polite" aria-atomic="true">
            <p className="pr-5 text-sm font-semibold">{presentation.title}</p>
            <p className="mt-1 text-sm leading-relaxed text-slate-300">
              {presentation.description}
            </p>
            <p className="mt-2 truncate text-xs text-slate-400">
              {notification.origin} → {notification.destination}
            </p>
          </div>

          <button
            type="button"
            onClick={openNotification}
            className={`mt-3 inline-flex items-center gap-1 text-xs font-semibold ${presentation.color} hover:underline focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-4`}
          >
            {presentation.action}
            <ArrowUpRight size={14} aria-hidden="true" />
          </button>
        </div>

        <button
          type="button"
          onClick={() => dismissToast(notification.id)}
          aria-label="Dismiss notification"
          className="absolute right-2 top-2 rounded-lg p-2 text-slate-400 transition hover:bg-white/10 hover:text-white focus-visible:outline focus-visible:outline-2 focus-visible:outline-blue-400"
        >
          <X size={15} aria-hidden="true" />
        </button>
      </div>
    </article>
  );
}
