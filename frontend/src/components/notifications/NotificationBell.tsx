import { Bell, CheckCheck, LoaderCircle, RefreshCw } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useNotifications } from '../../context/useNotifications';
import type { AppNotification } from '../../types/notification';
import { notificationDate, notificationPresentation } from './notificationPresentation';

export function NotificationBell() {
  const { notifications, unreadCount, loading, error, refresh, markRead, markAllRead } =
    useNotifications();

  const [open, setOpen] = useState(false);
  const [markingAll, setMarkingAll] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const rootRef = useRef<HTMLDivElement>(null);
  const buttonRef = useRef<HTMLButtonElement>(null);
  const panelRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!open) return;

    panelRef.current?.focus();

    function onOutside(event: PointerEvent) {
      if (!rootRef.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    }

    function onEscape(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setOpen(false);
        buttonRef.current?.focus();
      }
    }

    document.addEventListener('pointerdown', onOutside);
    document.addEventListener('keydown', onEscape);

    return () => {
      document.removeEventListener('pointerdown', onOutside);
      document.removeEventListener('keydown', onEscape);
    };
  }, [open]);

  async function readAll() {
    if (markingAll) return;

    setMarkingAll(true);
    setActionError(null);

    try {
      await markAllRead();
    } catch {
      setActionError('Could not mark notifications as read. Please try again.');
    } finally {
      setMarkingAll(false);
    }
  }

  async function openNotification(item: AppNotification) {
    setActionError(null);

    try {
      if (item.readAt === null) await markRead(item.id);
      setOpen(false);
      navigate(notificationPresentation(item).href);
    } catch {
      setActionError('Could not update this notification. Please try again.');
    }
  }

  return (
    <div className="relative" ref={rootRef}>
      <button
        ref={buttonRef}
        type="button"
        onClick={() => {
          const next = !open;
          setOpen(next);
          if (next) void refresh();
        }}
        aria-label={`Notifications, ${unreadCount} unread`}
        aria-expanded={open}
        aria-controls="notification-panel"
        title="Notifications"
        className="relative flex h-10 w-10 items-center justify-center rounded-xl border border-white/10 text-slate-300 transition hover:border-blue-500/40 hover:bg-blue-500/10 hover:text-blue-300 focus-visible:outline focus-visible:outline-2 focus-visible:outline-blue-400"
      >
        <Bell size={20} strokeWidth={1.8} aria-hidden="true" />
        {unreadCount > 0 && (
          <span className="absolute -right-2 -top-2 flex h-5 min-w-5 items-center justify-center rounded-full border-2 border-[#0d1b2e] bg-blue-500 px-1 text-[10px] font-bold text-white">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div
          id="notification-panel"
          ref={panelRef}
          tabIndex={-1}
          role="dialog"
          aria-label="Notifications"
          className="notification-enter fixed left-4 right-4 top-20 z-50 overflow-hidden rounded-2xl border border-white/10 bg-[#0d1b2e] shadow-[0_20px_60px_rgba(0,0,0,0.45)] outline-none sm:absolute sm:left-auto sm:right-0 sm:top-12 sm:w-[390px]"
        >
          <div className="flex items-center justify-between border-b border-white/10 px-4 py-4">
            <div>
              <h2 className="font-semibold text-white">Notifications</h2>
              <p className="mt-0.5 text-xs text-slate-400">
                {unreadCount > 0 ? `${unreadCount} unread` : 'You’re all caught up'}
              </p>
            </div>

            <div className="flex items-center gap-1">
              <button
                type="button"
                onClick={() => void refresh()}
                aria-label="Refresh notifications"
                className="rounded-lg p-2 text-slate-400 hover:bg-white/5 hover:text-white"
              >
                <RefreshCw size={16} aria-hidden="true" />
              </button>

              <button
                type="button"
                onClick={() => void readAll()}
                disabled={markingAll || unreadCount === 0}
                className="flex items-center gap-1 rounded-lg px-2 py-2 text-xs font-medium text-blue-300 hover:bg-blue-500/10 disabled:cursor-default disabled:opacity-40"
              >
                {markingAll ? (
                  <LoaderCircle size={15} className="animate-spin" />
                ) : (
                  <CheckCheck size={15} />
                )}
                Mark all read
              </button>
            </div>
          </div>

          {(actionError || error) && (
            <p role="alert" className="border-b border-white/10 px-4 py-3 text-xs text-amber-300">
              {actionError || error}
            </p>
          )}

          <div className="max-h-[min(65vh,450px)] overflow-y-auto">
            {loading && notifications.length === 0 ? (
              <div className="flex justify-center py-12">
                <LoaderCircle
                  className="animate-spin text-blue-400"
                  aria-label="Loading notifications"
                />
              </div>
            ) : notifications.length === 0 ? (
              <div className="px-6 py-10 text-center">
                <div className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-blue-500/10 text-blue-300">
                  <Bell size={25} aria-hidden="true" />
                </div>
                <p className="mt-4 text-sm font-medium text-white">
                  {error ? 'Notifications unavailable' : 'Nothing here yet'}
                </p>
                <p className="mt-2 text-xs leading-relaxed text-slate-400">
                  Booking requests and ride updates will appear here.
                </p>
              </div>
            ) : (
              <ul className="divide-y divide-white/5">
                {notifications.map((item) => {
                  const presentation = notificationPresentation(item);
                  const Icon = presentation.icon;

                  return (
                    <li key={item.id}>
                      <button
                        type="button"
                        onClick={() => void openNotification(item)}
                        className={`flex w-full gap-3 px-4 py-4 text-left transition hover:bg-white/5 ${
                          item.readAt === null ? 'bg-blue-500/[0.06]' : ''
                        }`}
                      >
                        <span
                          className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ${presentation.background} ${presentation.color}`}
                        >
                          <Icon size={19} aria-hidden="true" />
                        </span>

                        <span className="min-w-0 flex-1">
                          <span className="flex items-center justify-between gap-2">
                            <span className="text-sm font-medium text-white">
                              {presentation.title}
                            </span>
                            {item.readAt === null && (
                              <span
                                className="h-2 w-2 shrink-0 rounded-full bg-blue-400"
                                aria-label="Unread"
                              />
                            )}
                          </span>
                          <span className="mt-1 block text-xs leading-relaxed text-slate-300">
                            {presentation.description}
                          </span>
                          <span className="mt-1.5 block truncate text-xs text-slate-400">
                            {item.origin} → {item.destination}
                          </span>
                          <time
                            dateTime={item.createdAt}
                            className="mt-2 block text-[11px] text-slate-500"
                          >
                            {notificationDate(item.createdAt)}
                          </time>
                        </span>
                      </button>
                    </li>
                  );
                })}
              </ul>
            )}
          </div>

          <p className="border-t border-white/10 px-4 py-3 text-center text-[11px] text-slate-500">
            Your latest 20 notifications
          </p>
        </div>
      )}
    </div>
  );
}
