import { useEffect, useState } from 'react'

import Login from './pages/Login'
import Home from './pages/Home'
import Bc01Page from './pages/reports/Bc01/Bc01Page'
import AccountsPage from './pages/admin/AccountsPage'
import MainLayout from './components/layout/MainLayout'
import Bc01DetailPage from './pages/reports/Bc01/Bc01DetailPage'

import {
  AUTH_EXPIRED_EVENT,
  apiFetch,
  getToken,
  logout,
} from './services/api'

import { navigate, useRoute } from './router'

const KNOWN_PATHS = [
  '/',
  '/reports/bc-ds-01',
  '/reports/bc-ds-01-detail',
  '/admin/accounts',
]

// Kiểm tra tài khoản có quyền ADMIN hay không
function hasAdminRole(user) {
  return (
    Array.isArray(user?.vaiTro) &&
    user.vaiTro.includes('ROLE_ADMIN')
  )
}

// Trang hiển thị khi người dùng không đủ quyền
function Forbidden() {
  return (
    <main className="main-container">
      <div className="report-heading">
        <span className="report-label">403</span>
        <h1>Không có quyền truy cập</h1>
        <p>
          Chỉ tài khoản ADMIN mới được sử dụng chức năng này.
        </p>
      </div>
    </main>
  )
}

// Component chính
function App() {
  const route = useRoute()

  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [checkingAuth, setCheckingAuth] = useState(true)
  const [currentUser, setCurrentUser] = useState(null)
  const [loginMessage, setLoginMessage] = useState('')

  // Kiểm tra phiên đăng nhập khi mở ứng dụng
  useEffect(() => {
    async function checkLogin() {
      if (!getToken()) {
        setCheckingAuth(false)
        return
      }

      try {
        const user = await apiFetch('/me')

        setCurrentUser(user)
        setIsAuthenticated(true)
      } catch {
        logout()
        setCurrentUser(null)
        setIsAuthenticated(false)
      } finally {
        setCheckingAuth(false)
      }
    }

    checkLogin()
  }, [])

  // Khi JWT hết hạn thì quay về màn hình đăng nhập
  useEffect(() => {
    function handleExpired() {
      setCurrentUser(null)
      setIsAuthenticated(false)
      setLoginMessage(
        'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'
      )
    }

    window.addEventListener(
      AUTH_EXPIRED_EVENT,
      handleExpired
    )

    return () => {
      window.removeEventListener(
        AUTH_EXPIRED_EVENT,
        handleExpired
      )
    }
  }, [])

  // Đường dẫn không tồn tại thì chuyển về trang chủ
  useEffect(() => {
    if (
      isAuthenticated &&
      !KNOWN_PATHS.includes(route.path)
    ) {
      navigate('/', { replace: true })
    }
  }, [isAuthenticated, route.path])

  // Xử lý đăng nhập thành công
  function handleLoginSuccess(user) {
    setCurrentUser(user)
    setIsAuthenticated(true)
    setLoginMessage('')
  }

  // Xử lý đăng xuất
  function handleLogout() {
    logout()

    setCurrentUser(null)
    setIsAuthenticated(false)
    setLoginMessage('Bạn đã đăng xuất thành công.')

    navigate('/', { replace: true })
  }

  // Đang kiểm tra trạng thái đăng nhập
  if (checkingAuth) {
    return (
      <div className="app">
        Đang kiểm tra đăng nhập...
      </div>
    )
  }

  // Chưa đăng nhập
  if (!isAuthenticated) {
    return (
      <Login
        onLoginSuccess={handleLoginSuccess}
        message={loginMessage}
      />
    )
  }

  const isAdmin = hasAdminRole(currentUser)

  // Chọn trang theo đường dẫn
  // Chọn trang theo đường dẫn
  let page

  if (route.path === '/reports/bc-ds-01') {
    page = <Bc01Page />
  } else if (route.path === '/reports/bc-ds-01-detail') {
    page = <Bc01DetailPage />
  } else if (route.path === '/admin/accounts') {
    page = isAdmin ? (
      <AccountsPage
        currentUsername={currentUser?.tenDangNhap}
      />
    ) : (
      <Forbidden />
    )
  } else {
    page = <Home onNavigate={navigate} />
  }

  return (
    <div className="app">
      <MainLayout
        currentPath={route.path}
        currentUser={currentUser}
        isAdmin={isAdmin}
        onNavigate={navigate}
        onLogout={handleLogout}
      >
        {page}
      </MainLayout>
    </div>
  )
}

export default App