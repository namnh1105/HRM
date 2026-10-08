import React from 'react';
import { ActivityIndicator, View, StyleSheet } from 'react-native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { useSelector } from 'react-redux';
import { RootState } from '../store';
import MainTabs from './MainTabs';
import {
  Login,
  ForgotPassword,
  CreateLeaveRequest,
  AttendanceHistory,
  Salary,
  WorkSchedule,
  Contracts,
  ChangePassword,
  Degrees,
  FaceRegistration,
  EmployeeList,
  EmployeeDetail,
  Reports,
} from '../screens';

const Stack = createNativeStackNavigator();

const RootNavigator: React.FC = () => {
  const { isAuthenticated, roles } = useSelector((state: RootState) => state.auth);
  const isManager = roles.includes('MANAGER');
  const [isInitializing, setIsInitializing] = React.useState(true);

  React.useEffect(() => {
    // Give time for auth state to restore from SecureStore
    const timer = setTimeout(() => {
      setIsInitializing(false);
    }, 100);
    return () => clearTimeout(timer);
  }, []);

  if (isInitializing) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#3B82F6" />
      </View>
    );
  }

  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      {isAuthenticated ? (
        <>
          {/* Authenticated — Main app */}
          <Stack.Screen name="MainTabs" component={MainTabs} />
          <Stack.Screen name="CreateLeaveRequest" component={CreateLeaveRequest} />
          <Stack.Screen name="AttendanceHistory" component={AttendanceHistory} />
          <Stack.Screen name="Salary" component={Salary} />
          <Stack.Screen name="WorkSchedule" component={WorkSchedule} />
          <Stack.Screen name="Contracts" component={Contracts} />
          <Stack.Screen name="ChangePassword" component={ChangePassword} />
          <Stack.Screen name="Degrees" component={Degrees} />
          <Stack.Screen name="FaceRegistration" component={FaceRegistration} />
          
          {/* Manager Screens */}
          {isManager && (
            <>
              <Stack.Screen name="EmployeeList" component={EmployeeList} />
              <Stack.Screen name="EmployeeDetail" component={EmployeeDetail} />
              <Stack.Screen name="Reports" component={Reports} />
            </>
          )}
        </>
      ) : (
        <>
          {/* Not authenticated — Auth screens only */}
          <Stack.Screen name="Login" component={Login} />
          <Stack.Screen name="ForgotPassword" component={ForgotPassword} />
        </>
      )}
    </Stack.Navigator>
  );
};

const styles = StyleSheet.create({
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F8FAFC',
  },
});

export default RootNavigator;
