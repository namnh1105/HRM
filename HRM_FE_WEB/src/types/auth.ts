import { UserInfo } from './user'

export type LoginDTO = {
    email : string;
    password : string;
}

export type RefreshTokenDTO = {
    refreshToken : string;
}

export type RegisterDTO = {
    email : string;
    givenName : string;
    familyName : string;
    password : string;
}

export type AuthResponseDTO = {
    accessToken: string;
    refreshToken: string;
    user: UserInfo;
}

