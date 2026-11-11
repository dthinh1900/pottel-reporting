
import { useEffect, useState } from 'react'

import Login from './pages/Login'
import Home from './pages/Home'
import AccountsPage from './pages/admin/AccountsPage'
import MainLayout from './components/layout/MainLayout'
import GenericReportPage from './pages/reports/GenericReportPage'

import {
  AUTH_EXPIRED_EVENT,
  apiFetch,
  getToken,
  logout,
} from './services/api'

import { navigate, useRoute } from './router'

const REPORT_PATHS = {
  '/reports/bc-ds-01': 'BC-DS-01',
  '/reports/bc-ds-02': 'BC-DS-02',
  '/reports/bc-dt-01': 'BC-DT-01',
  '/reports/bc-dt-02': 'BC-DT-02',
  '/reports/generic': null,
}

const KNOWN_PATHS = [
  '/',
  ...Object.keys(REPORT_PATHS),
  '/admin/accounts',
]

function hasAdminRole(user) {
  return (
    Array.isArray(user?.vaiTro) &&
    user.vaiTro.includes('ROLE_ADMIN')
  )
}

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

function App() {
  const route = useRoute()

  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [checkingAuth, setCheckingAuth] = useState(true)
  const [currentUser, setCurrentUser] = useState(null)
  const [loginMessage, setLoginMessage] = useState('')

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

  useEffect(() => {
    function handleExpired() {
      setCurrentUser(null)
      setIsAuthenticated(false)
      setLoginMessage(
        'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'
      )
    }

    window.addEventListener(AUTH_EXPIRED_EVENT, handleExpired)

    return () => {
      window.removeEventListener(AUTH_EXPIRED_EVENT, handleExpired)
    }
  }, [])

  useEffect(() => {
    if (
      isAuthenticated &&
      !KNOWN_PATHS.includes(route.path)
    ) {
      navigate('/', { replace: true })
    }
  }, [isAuthenticated, route.path])

  function handleLoginSuccess(user) {
    setCurrentUser(user)
    setIsAuthenticated(true)
    setLoginMessage('')
  }

  function handleLogout() {
    logout()

    setCurrentUser(null)
    setIsAuthenticated(false)
    setLoginMessage('Bạn đã đăng xuất thành công.')

    navigate('/', { replace: true })
  }

  if (checkingAuth) {
    return (
      <div className="app">
        Đang kiểm tra đăng nhập...
      </div>
    )
  }

  if (!isAuthenticated) {
    return (
      <Login
        onLoginSuccess={handleLoginSuccess}
        message={loginMessage}
      />
    )
  }

  const isAdmin = hasAdminRole(currentUser)

  let page

  if (route.path in REPORT_PATHS) {
    page = (
      <GenericReportPage
        initialReportCode={REPORT_PATHS[route.path]}
      />
    )
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
