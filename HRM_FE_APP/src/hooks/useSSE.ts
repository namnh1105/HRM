import { useEffect, useState, useRef } from 'react';
import { getAccessToken, restoreAccessToken } from '../services/tokenStorage';
import { API_BASE_URL } from '../utils/constants';
import Toast from 'react-native-toast-message';
import { useNotifications } from './useNotifications';


import EventSource from "react-native-sse";

export const useSSE = () => {
  const [isConnected, setIsConnected] = useState(false);
  const eventSourceRef = useRef<any>(null);
  const { refetch } = useNotifications();

  useEffect(() => {
    let reconnectTimeout: NodeJS.Timeout;

    const connect = async () => {
      try {
        let token = getAccessToken();
        if (!token) {
          token = await restoreAccessToken();
        }
        if (!token) return;

        // API_BASE_URL usually includes '/api/v1'
        const url = `${API_BASE_URL}/notifications/stream`;

        const es = new EventSource(url, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });
        eventSourceRef.current = es;

        es.onopen = () => {
          setIsConnected(true);
          console.log('[SSE] Connected to', url);
        };

        es.addEventListener('INIT', (event: any) => {
          console.log('[SSE] Init:', event.data);
        });

        es.addEventListener('NOTIFICATION', (event: any) => {
          try {
            const data = JSON.parse(event.data);

            // Show toast
            Toast.show({
              type: 'info',
              text1: data.title,
              text2: data.message,
              position: 'top',
              visibilityTime: 4000,
            });

            // Refetch notifications to update badge and list
            refetch();
          } catch (e) {
            console.error('[SSE] Error parsing notification', e);
          }
        });

        es.onerror = (error: any) => {
          console.error('[SSE] Error:', error);
          setIsConnected(false);
          es.close();

          // Reconnect after 5 seconds
          reconnectTimeout = setTimeout(connect, 5000);
        };
      } catch (error) {
        console.error('[SSE] Connection setup failed', error);
      }
    };

    connect();

    return () => {
      clearTimeout(reconnectTimeout);
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
      }
    };
  }, [refetch]);

  return { isConnected };
};

