import { apiFetch } from './api'

// Gọi API báo cáo BC-DS-01.
// - bc: '1B' hoặc '1C'
// - includeMeta: true thì Backend trả thêm bảng 1A và danh sách lọc
export function fetchBcDs01(query, signal) {
  const {
    bc,
    fromDate,
    toDate,
    msisdn,
    kitStatus,
    reconciliationStatus,
    goiMoiStatus,
    page,
    pageSize,
    includeMeta,
  } = query

  const params = new URLSearchParams({
    bc,
    page: String(page),
    pageSize: String(pageSize),
  })

  function setIfPresent(name, value) {
    const text = (value ?? '').trim()
    if (text) params.set(name, text)
  }

  // Bảng 1B chỉ lọc theo ngày. Bảng 1C có thêm 4 bộ lọc.
  setIfPresent('fromDate', fromDate)
  setIfPresent('toDate', toDate)

  if (bc === '1C') {
    setIfPresent('msisdn', msisdn)
    setIfPresent('kitStatus', kitStatus)
    setIfPresent('reconciliationStatus', reconciliationStatus)
    setIfPresent('goiMoiStatus', goiMoiStatus)
  }

  if (!includeMeta) params.set('includeMeta', 'false')

  return apiFetch(`/reports/bc-ds-01?${params.toString()}`, { signal })
}
