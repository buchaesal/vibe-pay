// 인증 관련 유틸리티

// 로그인 상태 확인
export function isAuthenticated(): boolean {
  if (typeof window === 'undefined') return false;

  const session = sessionStorage.getItem('user');
  return !!session;
}

// 로그아웃
export function logout(): void {
  if (typeof window !== 'undefined') {
    sessionStorage.removeItem('user');
  }
}

// 사용자 정보 저장
export function setUser(user: any): void {
  if (typeof window !== 'undefined') {
    sessionStorage.setItem('user', JSON.stringify(user));
  }
}

// 사용자 정보 조회
export function getUser(): any {
  if (typeof window === 'undefined') return null;

  const session = sessionStorage.getItem('user');
  return session ? JSON.parse(session) : null;
}

// 인증 API
export const authApi = {
  login: async (credentials: { username: string; password: string }) => {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      throw new Error('로그인에 실패했습니다.');
    }

    const data = await response.json();

    if (data.success) {
      setUser(data.user);
    }

    return data;
  },

  logout: async () => {
    try {
      await fetch('/api/auth/logout', {
        method: 'POST',
      });
    } catch (error) {
      console.error('로그아웃 API 호출 실패:', error);
    } finally {
      logout();
    }
  },
};