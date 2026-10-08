import { UserInfo } from './user';

export interface AuthState {
    accessToken: string | null;
    refreshToken: string | null;
    user: UserInfo | null;
    isAuthenticated: boolean;
    roles: string[];
    permissions: string[];
}

export interface RootState {
    auth?: AuthState;
}
