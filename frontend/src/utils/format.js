const VN_TIMEZONE = 'Asia/Ho_Chi_Minh'

// Định dạng số theo kiểu Việt Nam (1.234.567)
export function formatNumber(value) {
  return new Intl.NumberFormat('vi-VN').format(value ?? 0)
}

// Định dạng ngày dd/MM/yyyy theo múi giờ Việt Nam
export function formatDate(value) {
  if (!value) return ''

  // Ngày thuần "yyyy-MM-dd" thì đổi chỗ trực tiếp, tránh lệch múi giờ
  if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value)) {
    const [year, month, day] = value.split('-')
    return `${day}/${month}/${year}`
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)

  return new Intl.DateTimeFormat('vi-VN', {
    timeZone: VN_TIMEZONE,
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(date)
}

// Định dạng ngày giờ dd/MM/yyyy HH:mm theo múi giờ Việt Nam
export function formatDateTime(value) {
  if (!value) return '-'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'

  const parts = Object.fromEntries(
    new Intl.DateTimeFormat('en-GB', {
      timeZone: VN_TIMEZONE,
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hourCycle: 'h23',
    })
      .formatToParts(date)
      .map((part) => [part.type, part.value])
  )

  return `${parts.day}/${parts.month}/${parts.year} ${parts.hour}:${parts.minute}`
}

// Ngày hôm nay (múi giờ Việt Nam) dạng yyyy-MM-dd, dùng cho thuộc tính max của ô ngày
export function todayInVietnam() {
  return new Intl.DateTimeFormat('en-CA', {
    timeZone: VN_TIMEZONE,
  }).format(new Date())
}

// Kiểm tra khoảng ngày lọc. Trả về thông báo lỗi, hoặc '' nếu hợp lệ.
export function validateDateRange(fromDate, toDate) {
  if (fromDate && toDate && fromDate > toDate) {
    return 'Ngày bắt đầu không được lớn hơn ngày kết thúc.'
  }

  const today = todayInVietnam()

  if ((fromDate && fromDate > today) || (toDate && toDate > today)) {
    return 'Ngày lọc không được vượt quá ngày hiện tại.'
  }

  return ''
}
