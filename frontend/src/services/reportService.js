
import { apiFetch } from './api'

// =====================================================
// API BÁO CÁO DÙNG CHUNG - ĐỌC THEO METADATA BACKEND
// =====================================================

// Lấy danh sách báo cáo và metadata bộ lọc.
export function fetchReportCatalog(signal) {
  return apiFetch('/reports', { signal })
}

// Chạy báo cáo theo mã và các tham số lọc.
export function fetchReportByCode(maBaoCao, thamSo = {}, signal) {
  const params = new URLSearchParams()

  Object.entries(thamSo).forEach(([ten, giaTri]) => {
    if (giaTri !== null && giaTri !== undefined && giaTri !== '') {
      params.set(ten, String(giaTri))
    }
  })

  const query = params.toString()

  return apiFetch(
    `/reports/${encodeURIComponent(maBaoCao)}${query ? `?${query}` : ''}`,
    { signal }
  )
}
