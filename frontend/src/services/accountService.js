import { apiFetch, apiPost } from './api'

// Danh sách tài khoản (chỉ ADMIN)
export function fetchAccounts() {
  return apiFetch('/admin/accounts')
}

// Tạo tài khoản mới (vai trò mặc định USER)
export function createAccount(tenDangNhap, matKhau) {
  return apiPost('/admin/accounts', { tenDangNhap, matKhau })
}

// Khóa hoặc mở khóa tài khoản
export function toggleAccount(id) {
  return apiPost(`/admin/accounts/${id}/toggle`)
}
