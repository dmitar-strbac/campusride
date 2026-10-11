import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs';
import { WS_BASE_URL } from '../config/api';

type Registration = {
  destination: string;
  onMessage: (message: IMessage) => void;
  onConnect: () => void;
  subscription?: StompSubscription;
};

type Session = {
  token: string;
  client: Client;
  registrations: Map<symbol, Registration>;
};

let session: Session | undefined;

export function subscribeRealtime(
  token: string,
  destination: string,
  onMessage: (message: IMessage) => void,
  onConnect: () => void,
): () => void {
  if (!session || session.token !== token) {
    if (session) void session.client.deactivate();

    const client = new Client({
      webSocketFactory: () => new WebSocket(WS_BASE_URL),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    const next: Session = {
      token,
      client,
      registrations: new Map(),
    };

    client.onConnect = () => {
      for (const registration of next.registrations.values()) {
        registration.subscription = client.subscribe(
          registration.destination,
          registration.onMessage,
        );
      }

      for (const registration of next.registrations.values()) {
        registration.onConnect();
      }
    };

    session = next;
    client.activate();
  }

  const current = session;
  const id = Symbol(destination);
  const registration: Registration = {
    destination,
    onMessage,
    onConnect,
  };

  current.registrations.set(id, registration);

  if (current.client.connected) {
    registration.subscription = current.client.subscribe(destination, onMessage);
    onConnect();
  }

  return () => {
    if (current.client.connected) {
      registration.subscription?.unsubscribe();
    }

    current.registrations.delete(id);

    if (current.registrations.size === 0) {
      if (session === current) session = undefined;
      void current.client.deactivate();
    }
  };
}
