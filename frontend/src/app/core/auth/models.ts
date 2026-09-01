export interface UserResponse {
  id: string;
  fullName: string;
  email: string;
  role: 'ADMIN' | 'USER';
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  user: UserResponse;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
}
