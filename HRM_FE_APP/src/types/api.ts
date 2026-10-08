// API Types
export interface User {
  id: string;
  username: string;
  givenName: string;
  familyName: string;
  avatarUrl: string | null;
  followersCount?: number;
  followingCount?: number;
  isFollowing?: boolean;
}

export interface VideoStats {
  views: number;
  likes: number;
  comments: number;
  shares: number;
  saves: number;
}

export interface Video {
  id: string;
  user: User;
  videoUrl: string;
  thumbnailUrl: string;
  caption: string;
  hashtags: string[];
  duration: number;
  stats: VideoStats;
  createdAt: string;
}

export interface VideoListData {
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
  videos: Video[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

// Chat Types
export interface ChatRoom {
  id: string;
  name?: string | null;
  type: 'private' | 'group';
  users: User[];
  lastMessageAt?: string;
  unreadCount?: number;
  lastMessage?: {
    content: string;
    senderId: string;
    createdAt: string;
  };
  createdAt: string;
  updatedAt: string;
}

export interface ChatMessage {
  id: string;
  content: string;
  messageType: 'text' | 'image' | 'file';
  senderId: string;
  sender: User;
  roomId: string;
  isEdited: boolean;
  editedAt?: string;
  createdAt: string;
  updatedAt: string;
  readBy?: string[];
}

export interface SendMessageDto {
  content: string;
  recipientId: string;
  roomId?: string;
  messageType?: 'text' | 'image' | 'file';
}

export interface CreateRoomDto {
  userIds: string[];
  name?: string;
  type?: 'private' | 'group';
}

export interface GetMessagesDto {
  limit?: string;
  offset?: string;
}

export interface SearchUsersDto {
  query: string;
  limit?: string;
}

export interface MarkAsReadDto {
  roomId: string;
}

export interface UserVideosResponse {
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
  videos: Video[];
}

export interface FollowingListData {
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
  following: User[];
}

export interface FollowersListData {
  page: number;
  size: number;
  totalPages: number;
  totalElements: number;
  followers: User[];
}

export interface DraftVideo {
  id: string;
  videoUri: string;
  thumbnailUri: string;
  caption?: string;
  hashtags?: string[];
  createdAt: string;
}
