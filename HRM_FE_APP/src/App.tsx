import React, { useEffect } from "react";
import { NavigationContainer } from "@react-navigation/native";
import { Provider } from "react-redux";
import { store, initializeAuth } from "./store";
import RootNavigator from "./navigation/RootNavigator";
import { AuthProvider } from "./context/AuthContext";
import Toast from 'react-native-toast-message';

function AppContent() {
  useEffect(() => {
    // Restore auth state on app start
    setTimeout(() => {
      initializeAuth();
    }, 100);
  }, []);

  return (
    <AuthProvider>
      <NavigationContainer>
        <RootNavigator />
      </NavigationContainer>
      <Toast />
    </AuthProvider>
  );
}

export default function App() {
  return (
    <Provider store={store}>
      <AppContent />
    </Provider>
  );
}
