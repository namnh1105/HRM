import React, { createContext, useContext, useState, ReactNode } from 'react';
import { useSelector } from 'react-redux';
import { performCompleteLogout } from '../store';
import { RootState } from '../store';

interface AuthContextType {
  loading: boolean;
  signOut: () => Promise<void>;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const reduxAuth = useSelector((state: RootState) => state.auth);
  const [loading, setLoading] = useState(false);

  const handleSignOut = async (): Promise<void> => {
    try {
      console.log('[AuthContext] Starting sign out...');
      setLoading(true);
      
      // Perform complete logout (clears SecureStore + AsyncStorage + Redux)
      await performCompleteLogout();
      
      console.log('[AuthContext] Sign out completed successfully');
    } catch (error) {
      console.error('[AuthContext] Sign out error:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  };

  const value: AuthContextType = {
    loading,
    signOut: handleSignOut,
    isAuthenticated: reduxAuth.isAuthenticated,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuthContext = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuthContext must be used within an AuthProvider');
  }
  return context;
};
