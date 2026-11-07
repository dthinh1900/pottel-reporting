import { useEffect, useState } from 'react'

// Router tối giản dựa trên History API (không cần cài thêm thư viện).
// Đường dẫn giống Thymeleaf cũ: /, /reports/bc-ds-01, /admin/accounts

const NAVIGATE_EVENT = 'app-navigate'

function readLocation() {
  return {
    path: window.location.pathname.replace(/\/+$/, '') || '/',
    search: window.location.search,
  }
}

// Chuyển trang và thông báo cho ứng dụng cập nhật giao diện
export function navigate(to, { replace = false } = {}) {
  if (replace) {
    window.history.replaceState({}, '', to)
  } else {
    window.history.pushState({}, '', to)
  }

  window.dispatchEvent(new Event(NAVIGATE_EVENT))
}

// Chỉ cập nhật thanh địa chỉ, không làm ứng dụng vẽ lại
// (dùng để đồng bộ bộ lọc lên URL).
export function replaceUrl(to) {
  window.history.replaceState({}, '', to)
}

export function useRoute() {
  const [route, setRoute] = useState(readLocation)

  useEffect(() => {
    const update = () => setRoute(readLocation())

    window.addEventListener('popstate', update)
    window.addEventListener(NAVIGATE_EVENT, update)

    return () => {
      window.removeEventListener('popstate', update)
      window.removeEventListener(NAVIGATE_EVENT, update)
    }
  }, [])

  return route
}
