import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { useSelector } from 'react-redux';
import { RootState } from '../store';

const Home: React.FC = () => {
  const { user } = useSelector((state: RootState) => state.auth);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Welcome to HRM App</Text>
      
      <View style={styles.userContainer}>
        <Text style={styles.welcomeText}>
          Hello, {user?.name || 'User'}!
        </Text>
        <Text style={styles.subtitle}>
          You are successfully logged in.
        </Text>
      </View>
    </View>
  );
};
const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 20,
    color: '#333',
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
    textAlign: 'center',
    marginBottom: 30,
  },
  userContainer: {
    alignItems: 'center',
  },
  welcomeText: {
    fontSize: 20,
    fontWeight: '600',
    color: '#333',
    marginBottom: 10,
  },
});

export default Home;