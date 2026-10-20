import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/authContext';
import { errorMessage } from './format';

/**
 * Runs `loader` (a function that returns a promise) when the screen opens and whenever the
 * loader function changes, so wrap it in useCallback. Gives { data, loading, error, reload }.
 * If the backend says the login is missing or expired (401 or 403) the admin is sent to the login screen.
 */
export function useLoad(loader) {
  const [state, setState] = useState({ loading: true, data: null, error: '' });
  const [tick, setTick] = useState(0);
  const { logout } = useAuth();
  const navigate = useNavigate();

  const authRef = useRef({ logout, navigate });
  useEffect(() => {
    authRef.current = { logout, navigate };
  });

  useEffect(() => {
    let cancelled = false;
    // keep showing the old data while a reload is running
    setState((previous) => ({ loading: true, data: previous.data, error: '' }));
    loader()
      .then((data) => {
        if (!cancelled) setState({ loading: false, data, error: '' });
      })
      .catch((error) => {
        if (cancelled) return;
        if (error && (error.status === 401 || error.status === 403)) {
          authRef.current.logout();
          authRef.current.navigate('/admin/login');
          return;
        }
        setState({ loading: false, data: null, error: errorMessage(error) });
      });
    return () => {
      cancelled = true;
    };
  }, [loader, tick]);

  const reload = useCallback(() => setTick((value) => value + 1), []);
  return { ...state, reload };
}
