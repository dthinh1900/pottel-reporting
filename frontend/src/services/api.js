// Địa chỉ Backend. Có thể đổi bằng biến VITE_API_URL trong file .env của frontend.
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const TOKEN_KEY = 'access_token'

// Sự kiện phát ra khi Backend báo JWT hết hạn/không hợp lệ (HTTP 401)
export const AUTH_EXPIRED_EVENT = 'auth-expired'

// Lỗi từ API, có thêm mã trạng thái HTTP
export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

// Lấy JWT đã lưu
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

// Xóa JWT
export function logout() {
  localStorage.removeItem(TOKEN_KEY)
}

// Đọc thông báo lỗi {"message": "..."} mà Backend trả về
async function readErrorMessage(response, fallback) {
  try {
    const data = await response.json()
    if (data && typeof data.message === 'string' && data.message) {
      return data.message
    }
  } catch {
    // Phản hồi không phải JSON, dùng thông báo mặc định
  }

  return fallback
}

// Gọi API Backend (tự gắn JWT)
export async function apiFetch(path, options = {}) {
  const token = getToken()

  let response

  try {
    response = await fetch(`${API_URL}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers,
      },
    })
  } catch (error) {
    // Hủy request chủ động thì để nguyên lỗi cho nơi gọi xử lý
    if (error.name === 'AbortError') throw error

    throw new ApiError(
      'Không thể kết nối đến máy chủ. Vui lòng kiểm tra Backend đã chạy chưa.',
      0
    )
  }

  if (response.status === 401) {
    logout()
    window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT))
    throw new ApiError(
      'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
      401
    )
  }

  if (!response.ok) {
    throw new ApiError(
      await readErrorMessage(response, `Lỗi API: ${response.status}`),
      response.status
    )
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

// Gọi API bằng phương thức POST với dữ liệu JSON
export function apiPost(path, body) {
  return apiFetch(path, {
    method: 'POST',
    body: body === undefined ? undefined : JSON.stringify(body),
  })
}

// Đăng nhập
export async function login(tenDangNhap, matKhau) {
  let response

  try {
    response = await fetch(`${API_URL}/auth/dang-nhap`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        tenDangNhap,
        matKhau,
      }),
    })
  } catch {
    throw new Error(
      'Không thể kết nối đến máy chủ. Vui lòng kiểm tra Backend đã chạy chưa.'
    )
  }

  if (!response.ok) {
    throw new Error(
      await readErrorMessage(
        response,
        'Tên đăng nhập hoặc mật khẩu không chính xác.'
      )
    )
  }

  const data = await response.json()

  if (!data.token) {
    throw new Error('Phản hồi đăng nhập không có mã xác thực.')
  }

  localStorage.setItem(TOKEN_KEY, data.token)

  return data
}
