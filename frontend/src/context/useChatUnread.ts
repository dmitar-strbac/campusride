import { useContext } from 'react';
import { ChatUnreadContext } from './chatUnreadContext';

export function useChatUnread() {
  const context = useContext(ChatUnreadContext);
  if (!context) {
    throw new Error('useChatUnread requires ChatUnreadProvider');
  }
  return context;
}
