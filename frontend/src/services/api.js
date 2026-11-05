const API_URL = 'http://localhost:8080/api'

// Lấy JWT đã lưu
export function getToken() {
  return localStorage.getItem('access_token')
}

// Xóa JWT
export function logout() {
  localStorage.removeItem('access_token')
}

// Gọi API Backend
export async function apiFetch(path, options = {}) {
  const token = getToken()

  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })

  if (response.status === 401) {
    logout()
    throw new Error('Phiên đăng nhập đã hết hạn.')
  }

  if (!response.ok) {
    throw new Error(`Lỗi API: ${response.status}`)
  }

  if (response.status === 204) {
    return null
  }

  const contentType = response.headers.get('content-type')

  if (contentType?.includes('application/json')) {
    return response.json()
  }

  return response.text()
}

// Đăng nhập
export async function login(tenDangNhap, matKhau) {
  const response = await fetch(`${API_URL}/auth/dang-nhap`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      tenDangNhap,
      matKhau,
    }),
  })

  if (!response.ok) {
    throw new Error('Tên đăng nhập hoặc mật khẩu không chính xác.')
  }

  const data = await response.json()

  if (!data.token) {
    throw new Error('Phản hồi đăng nhập không có mã xác thực.')
  }

  localStorage.setItem('access_token', data.token)

  return data
}