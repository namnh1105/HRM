import { configureStore } from '@reduxjs/toolkit';
import { baseApi } from './api/baseApi';
import { RootState as AppRootState } from '@/types';
import authReducer from './features/authSlice';

export const store = configureStore({
    reducer: {
        [baseApi.reducerPath]: baseApi.reducer,
        auth: authReducer,
    },
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware({
            serializableCheck: false,
        }).concat(baseApi.middleware),
});

export type RootState = AppRootState & ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;