import { Client, type IMessage } from '@stomp/stompjs';
import axios from 'axios';
import { ArrowLeft, CarFront, MessageCircle, SendHorizontal } from 'lucide-react';
import { useEffect, useRef, useState, type FormEvent } from 'react';
import { Link, useParams } from 'react-router-dom';
import { chatApi } from '../api/chatApi';
import { ridesApi } from '../api/ridesApi';
import { WS_BASE_URL } from '../config/api';
import { useAuth } from '../context/useAuth';
import { useChatUnread } from '../context/useChatUnread';
import type { ChatMessage } from '../types/chat';
import type { Ride } from '../types/ride';

const PAGE_SIZE = 30;
const MAX_LENGTH = 1000;

function chronological(messages: ChatMessage[]): ChatMessage[] {
  const unique = new Map<number, ChatMessage>();

  for (const message of messages) {
    unique.set(message.id, message);
  }

  return [...unique.values()].sort((a, b) => a.id - b.id);
}

function formatDeparture(value: string): string {
  return new Intl.DateTimeFormat('en', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

function formatMessageTime(value: string): string {
  return new Intl.DateTimeFormat('en', {
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

function initials(name: string): string {
  return name
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part.charAt(0))
    .join('')
    .toUpperCase();
}

type TypingEvent = {
  rideId: number;
  userId: number;
  userName: string;
  typing: boolean;
};

export function RideChatPage() {
  const { rideId: rideIdParam } = useParams();
  const rideId = Number(rideIdParam);
  const invalidRideId = !Number.isSafeInteger(rideId) || rideId < 1;
  const { token, user } = useAuth();
  const { markRead } = useChatUnread();

  const [ride, setRide] = useState<Ride | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [draft, setDraft] = useState('');
  const [loading, setLoading] = useState(true);
  const [loadingOlder, setLoadingOlder] = useState(false);
  const [hasMore, setHasMore] = useState(false);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState('');
  const [sendError, setSendError] = useState('');

  const clientRef = useRef<Client | null>(null);
  const listRef = useRef<HTMLDivElement | null>(null);
  const bottomRef = useRef<HTMLDivElement | null>(null);
  const shouldScrollToBottomRef = useRef(false);

  const [typingUsers, setTypingUsers] = useState<Record<number, string>>({});

  const typingTimersRef = useRef<Map<number, ReturnType<typeof setTimeout>>>(new Map());

  const localTypingTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const lastTypingSentRef = useRef(0);

  useEffect(() => {
    if (!token || invalidRideId) return;

    let active = true;
    const client = new Client({
      webSocketFactory: () => new WebSocket(WS_BASE_URL),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        if (!active) return;

        setConnected(true);

        client.subscribe(`/user/queue/rides/${rideId}/chat`, (frame: IMessage) => {
          if (!active) return;

          try {
            const incoming = JSON.parse(frame.body) as ChatMessage;

            if (incoming.rideId !== rideId) return;

            const container = listRef.current;
            shouldScrollToBottomRef.current =
              incoming.senderId === user?.id ||
              !container ||
              container.scrollHeight - container.scrollTop - container.clientHeight < 120;

            setMessages((current) => chronological([...current, incoming]));

            if (document.visibilityState === 'visible') {
              void markRead(rideId, incoming.id).catch(() => {});
            }
          } catch {
            setSendError('A message could not be displayed. Reload the chat.');
          }
        });

        client.subscribe(`/user/queue/rides/${rideId}/typing`, (frame: IMessage) => {
          if (!active) return;

          try {
            const event = JSON.parse(frame.body) as TypingEvent;
            if (event.rideId !== rideId || event.userId === user?.id) {
              return;
            }

            const previousTimer = typingTimersRef.current.get(event.userId);
            if (previousTimer) clearTimeout(previousTimer);

            if (!event.typing) {
              typingTimersRef.current.delete(event.userId);
              setTypingUsers((current) => {
                const next = { ...current };
                delete next[event.userId];
                return next;
              });
              return;
            }

            setTypingUsers((current) => ({
              ...current,
              [event.userId]: event.userName,
            }));

            const timer = setTimeout(() => {
              typingTimersRef.current.delete(event.userId);
              setTypingUsers((current) => {
                const next = { ...current };
                delete next[event.userId];
                return next;
              });
            }, 4000);

            typingTimersRef.current.set(event.userId, timer);
          } catch {
            // An invalid transient typing event should not interrupt messages.
          }
        });

        void chatApi
          .getMessages(rideId, token)
          .then((recent) => {
            if (!active) return;
            setMessages((current) => chronological([...current, ...recent]));
          })
          .catch((requestError: unknown) => {
            if (!active) return;
            if (axios.isAxiosError(requestError) && requestError.response?.status === 403) {
              setError('You no longer have access to this ride chat.');
              if (localTypingTimerRef.current) {
                clearTimeout(localTypingTimerRef.current);
              }
              for (const timer of typingTimersRef.current.values()) {
                clearTimeout(timer);
              }
              typingTimersRef.current.clear();
              void client.deactivate();
            }
          });
      },
      onWebSocketClose: () => {
        if (active) setConnected(false);
      },
      onStompError: () => {
        if (active) {
          setConnected(false);
          setSendError('Chat connection was rejected. Refresh the page.');
        }
      },
    });

    async function initialize() {
      setLoading(true);
      setError('');
      setSendError('');
      setMessages([]);
      setRide(null);
      setConnected(false);
      setHasMore(false);

      try {
        const [rideData, recent] = await Promise.all([
          ridesApi.getRide(rideId, token!),
          chatApi.getMessages(rideId, token!),
        ]);

        if (!active) return;

        setRide(rideData);
        setMessages(chronological(recent));
        if (recent.length > 0) {
          void markRead(rideId, recent[0].id).catch(() => {});
        }
        setHasMore(recent.length === PAGE_SIZE);
        shouldScrollToBottomRef.current = true;
        clientRef.current = client;
        client.activate();
      } catch (requestError) {
        if (!active) return;

        if (axios.isAxiosError(requestError) && requestError.response?.status === 403) {
          setError('Only the driver and passengers with accepted bookings can open this chat.');
        } else {
          setError('Could not load this ride chat. Please try again.');
        }
      } finally {
        if (active) setLoading(false);
      }
    }

    void initialize();

    return () => {
      active = false;
      clientRef.current = null;
      void client.deactivate();
    };
  }, [rideId, token, user?.id, invalidRideId, markRead]);

  useEffect(() => {
    if (shouldScrollToBottomRef.current) {
      bottomRef.current?.scrollIntoView({ block: 'end' });
      shouldScrollToBottomRef.current = false;
    }
  }, [messages]);

  async function loadOlder() {
    if (!token || !hasMore || loadingOlder || messages.length === 0) {
      return;
    }

    const container = listRef.current;
    const previousHeight = container?.scrollHeight ?? 0;
    const previousTop = container?.scrollTop ?? 0;

    setLoadingOlder(true);
    setSendError('');

    try {
      const older = await chatApi.getMessages(rideId, token, messages[0].id);

      setMessages((current) => chronological([...older, ...current]));
      setHasMore(older.length === PAGE_SIZE);

      requestAnimationFrame(() => {
        if (container) {
          container.scrollTop = container.scrollHeight - previousHeight + previousTop;
        }
      });
    } catch (requestError) {
      if (axios.isAxiosError(requestError) && requestError.response?.status === 403) {
        setError('You no longer have access to this ride chat.');
        void clientRef.current?.deactivate();
      } else {
        setSendError('Older messages could not be loaded. Try again.');
      }
    } finally {
      setLoadingOlder(false);
    }
  }

  function publishTyping(typing: boolean) {
    const client = clientRef.current;
    if (!client?.connected) return;

    client.publish({
      destination: `/app/rides/${rideId}/typing`,
      body: JSON.stringify({ typing }),
      headers: { 'content-type': 'application/json' },
    });
  }

  function handleDraftChange(value: string) {
    setDraft(value);

    if (localTypingTimerRef.current) {
      clearTimeout(localTypingTimerRef.current);
    }

    if (!value.trim()) {
      publishTyping(false);
      return;
    }

    const now = Date.now();
    if (now - lastTypingSentRef.current > 2000) {
      publishTyping(true);
      lastTypingSentRef.current = now;
    }

    localTypingTimerRef.current = setTimeout(() => {
      publishTyping(false);
      lastTypingSentRef.current = 0;
    }, 1800);
  }

  function handleSend(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const content = draft.trim();
    const client = clientRef.current;

    if (!content || content.length > MAX_LENGTH || !client?.connected) {
      return;
    }

    try {
      client.publish({
        destination: `/app/rides/${rideId}/chat`,
        body: JSON.stringify({ content }),
        headers: { 'content-type': 'application/json' },
      });

      if (localTypingTimerRef.current) {
        clearTimeout(localTypingTimerRef.current);
      }
      publishTyping(false);
      lastTypingSentRef.current = 0;

      setDraft('');
      setSendError('');
    } catch {
      setSendError('Message could not be sent. Your text is still here.');
    }
  }

  if (invalidRideId) {
    return (
      <main className="flex min-h-[calc(100vh-73px)] items-center justify-center bg-[#060e1a] px-6">
        <div className="rounded-3xl border border-white/10 bg-[#0d1b2e] p-8 text-center">
          <h1 className="text-xl font-bold text-white">Invalid ride link</h1>
          <Link
            to="/bookings/my"
            className="mt-4 inline-block text-sm font-semibold text-blue-400 hover:text-blue-300"
          >
            Go to my bookings
          </Link>
        </div>
      </main>
    );
  }

  if (loading) {
    return (
      <main className="flex min-h-[calc(100vh-73px)] items-center justify-center bg-[#060e1a] px-6 text-slate-400">
        Loading ride chat...
      </main>
    );
  }

  if (error || !ride) {
    return (
      <main className="flex min-h-[calc(100vh-73px)] items-center justify-center bg-[#060e1a] px-6">
        <div className="w-full max-w-md rounded-3xl border border-white/10 bg-[#0d1b2e] p-8 text-center">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl border border-blue-500/20 bg-blue-500/10">
            <MessageCircle size={25} strokeWidth={1.7} className="text-blue-400" />
          </div>
          <h1 className="mt-5 text-xl font-bold text-white">Chat unavailable</h1>
          <p className="mt-2 text-sm leading-6 text-slate-400">{error || 'Ride not found.'}</p>
          <Link
            to="/bookings/my"
            className="mt-6 inline-block rounded-xl bg-blue-500 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-blue-600"
          >
            My bookings
          </Link>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-[calc(100vh-73px)] bg-[#060e1a] px-4 py-5 sm:px-6 sm:py-8">
      <section className="mx-auto max-w-5xl">
        <div className="mb-5">
          <Link
            to="/chats"
            className="inline-flex items-center gap-2 text-sm font-medium text-slate-400 transition hover:text-white"
          >
            <ArrowLeft size={17} />
            Messages
          </Link>
          <h1 className="mt-2 text-2xl font-bold text-white sm:text-3xl">
            {ride.origin}
            <span className="mx-3 text-slate-500">→</span>
            {ride.destination}
          </h1>
          <p className="mt-2 text-sm text-slate-400">
            {formatDeparture(ride.departureTime)} · Driven by {ride.driverName}
          </p>
        </div>

        <div className="overflow-hidden rounded-3xl border border-white/10 bg-[#0d1b2e] shadow-2xl shadow-black/30">
          <div className="flex items-center justify-between gap-3 border-b border-white/10 px-4 py-4 sm:px-6">
            <div>
              <h2 className="flex items-center gap-2 font-semibold text-white">
                <CarFront size={18} strokeWidth={1.8} className="text-blue-400" />
                Ride chat
              </h2>
              <p className="mt-0.5 text-xs text-slate-400">Driver and accepted passengers</p>
            </div>

            <div className="flex items-center gap-3">
              <span
                className={`flex items-center gap-2 rounded-full border px-3 py-1.5 text-xs ${
                  connected
                    ? 'border-emerald-500/20 bg-emerald-500/10 text-emerald-300'
                    : 'border-amber-500/20 bg-amber-500/10 text-amber-300'
                }`}
                role="status"
              >
                <span
                  className={`h-2 w-2 rounded-full ${
                    connected ? 'bg-emerald-400' : 'bg-amber-400'
                  }`}
                />
                {connected ? 'Live' : 'Connecting...'}
              </span>

              <Link
                to={`/rides/${ride.id}`}
                className="hidden rounded-xl border border-white/10 px-3 py-2 text-xs font-semibold text-slate-300 transition hover:bg-white/10 hover:text-white sm:inline-block"
              >
                Ride details
              </Link>
            </div>
          </div>

          <div
            ref={listRef}
            className="h-[min(62vh,600px)] min-h-[360px] space-y-5 overflow-y-auto bg-[#091525] px-4 py-6 sm:px-8"
            aria-label="Ride messages"
          >
            {hasMore && (
              <div className="text-center">
                <button
                  type="button"
                  onClick={() => void loadOlder()}
                  disabled={loadingOlder}
                  className="rounded-full border border-white/10 bg-white/5 px-4 py-2 text-xs font-semibold text-blue-300 transition hover:bg-white/10 disabled:opacity-50"
                >
                  {loadingOlder ? 'Loading...' : 'Load earlier messages'}
                </button>
              </div>
            )}

            {messages.length === 0 ? (
              <div className="flex h-full flex-col items-center justify-center text-center">
                <div className="flex h-14 w-14 items-center justify-center rounded-2xl border border-blue-500/20 bg-blue-500/10">
                  <MessageCircle size={25} strokeWidth={1.7} className="text-blue-400" />
                </div>
                <h3 className="mt-4 font-semibold text-white">Start the conversation</h3>
                <p className="mt-2 max-w-xs text-sm leading-6 text-slate-400">
                  Confirm the meeting point, luggage, or anything else your group should know.
                </p>
              </div>
            ) : (
              messages.map((message) => {
                const mine = message.senderId === user?.id;

                return (
                  <div
                    key={message.id}
                    className={`flex items-end gap-2.5 ${mine ? 'justify-end' : 'justify-start'}`}
                  >
                    {!mine && (
                      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-slate-700 text-[10px] font-bold text-white">
                        {initials(message.senderName)}
                      </div>
                    )}

                    <div className="max-w-[82%] sm:max-w-[68%]">
                      {!mine && (
                        <p className="mb-1 ml-1 text-xs font-medium text-blue-300">
                          {message.senderName}
                        </p>
                      )}

                      <div
                        className={`rounded-2xl px-4 py-2.5 shadow-sm ${
                          mine
                            ? 'rounded-br-md bg-blue-500 text-white'
                            : 'rounded-bl-md border border-white/10 bg-[#16263d] text-slate-100'
                        }`}
                      >
                        <p className="whitespace-pre-wrap break-words text-sm leading-6">
                          {message.content}
                        </p>
                      </div>

                      <p
                        className={`mt-1 text-[11px] text-slate-500 ${
                          mine ? 'text-right' : 'ml-1'
                        }`}
                      >
                        {mine ? 'You · ' : ''}
                        {formatMessageTime(message.createdAt)}
                      </p>
                    </div>
                  </div>
                );
              })
            )}

            {Object.entries(typingUsers).length > 0 && (
              <div className="flex items-end gap-2.5" aria-live="polite">
                <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-slate-700 text-[10px] font-bold text-white">
                  {Object.values(typingUsers).length === 1
                    ? initials(Object.values(typingUsers)[0])
                    : '···'}
                </div>

                <div>
                  <p className="mb-1 text-xs text-slate-400">
                    {Object.values(typingUsers).length === 1
                      ? `${Object.values(typingUsers)[0]} is typing`
                      : `${Object.values(typingUsers).length} people are typing`}
                  </p>
                  <div className="flex items-center gap-1 rounded-2xl rounded-bl-md border border-white/10 bg-[#16263d] px-4 py-3">
                    <span className="h-1.5 w-1.5 animate-bounce rounded-full bg-slate-400" />
                    <span className="h-1.5 w-1.5 animate-bounce rounded-full bg-slate-400 [animation-delay:150ms]" />
                    <span className="h-1.5 w-1.5 animate-bounce rounded-full bg-slate-400 [animation-delay:300ms]" />
                  </div>
                </div>
              </div>
            )}

            <div ref={bottomRef} />
          </div>

          <div className="border-t border-white/10 bg-[#0d1b2e] px-4 py-4 sm:px-6">
            {sendError && (
              <p
                role="alert"
                className="mb-3 rounded-xl border border-red-500/20 bg-red-500/10 px-4 py-2.5 text-xs text-red-300"
              >
                {sendError}
              </p>
            )}

            <form onSubmit={handleSend} className="flex items-end gap-3">
              <div className="flex-1">
                <label htmlFor="chat-message" className="sr-only">
                  Message
                </label>
                <textarea
                  id="chat-message"
                  rows={1}
                  maxLength={MAX_LENGTH}
                  value={draft}
                  onChange={(event) => handleDraftChange(event.target.value)}
                  onKeyDown={(event) => {
                    if (
                      event.key === 'Enter' &&
                      !event.shiftKey &&
                      !event.nativeEvent.isComposing
                    ) {
                      event.preventDefault();
                      event.currentTarget.form?.requestSubmit();
                    }
                  }}
                  onBlur={() => {
                    if (localTypingTimerRef.current) {
                      clearTimeout(localTypingTimerRef.current);
                    }
                    publishTyping(false);
                    lastTypingSentRef.current = 0;
                  }}
                  placeholder={connected ? 'Message the group...' : 'Waiting for connection...'}
                  className="block h-12 max-h-32 w-full resize-none rounded-2xl border border-white/10 bg-[#091525] px-4 py-3 text-sm text-white outline-none placeholder:text-slate-500 focus:border-blue-500/60 focus:ring-2 focus:ring-blue-500/10"
                />
              </div>

              <button
                type="submit"
                disabled={!connected || !draft.trim()}
                className="flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl bg-blue-500 text-white transition hover:bg-blue-600 disabled:cursor-not-allowed disabled:bg-slate-700 disabled:text-slate-400"
                aria-label="Send message"
                title="Send message"
              >
                <SendHorizontal size={20} strokeWidth={2} />
              </button>
            </form>

            <p className="mt-2 text-right text-[11px] text-slate-500">
              Enter to send · Shift+Enter for a new line · {draft.length}/{MAX_LENGTH}
            </p>
          </div>
        </div>
      </section>
    </main>
  );
}
