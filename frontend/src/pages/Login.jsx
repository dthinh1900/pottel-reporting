import { useState } from 'react'
import './Login.css'
import { login } from '../services/api'

function Login({ onLoginSuccess, message }) {
  const [tenDangNhap, setTenDangNhap] = useState('admin')
  const [matKhau, setMatKhau] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // Xử lý đăng nhập
  async function handleSubmit(event) {
    event.preventDefault()

    setError('')
    setLoading(true)

    try {
      const data = await login(tenDangNhap, matKhau)

      // Thông báo cho App.jsx đăng nhập thành công
      onLoginSuccess(data.nguoiDung)
    } catch (err) {
      setError(err.message || 'Đăng nhập thất bại.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="login-page">
      <form
        className="login-card"
        onSubmit={handleSubmit}
      >
        <div className="login-brand">
          <div className="login-brand-icon">
            <span>P</span>
          </div>

          <div className="login-brand-text">
            <strong>POTTEL</strong>
            <span>REPORTING SYSTEM</span>
          </div>
        </div>

        <div className="login-heading">
          <span className="login-eyebrow">
            WELCOME BACK
          </span>

          <h1>Đăng nhập</h1>

          <p>
            Đăng nhập để tiếp tục sử dụng hệ thống báo cáo Pottel.
          </p>
        </div>

        {/* Thông báo thành công hoặc thông báo từ App.jsx */}
        {message && !error && (
          <div
            className="login-success"
            role="status"
          >
            <span>{message}</span>
          </div>
        )}

        {/* Thông báo lỗi đăng nhập */}
        {error && (
          <div
            className="login-error"
            role="alert"
          >
            <span className="login-error-icon">!</span>
            <span>{error}</span>
          </div>
        )}

        <div className="login-form-fields">
          <label className="login-field">
            <span>Tên đăng nhập</span>

            <input
              type="text"
              value={tenDangNhap}
              onChange={(event) =>
                setTenDangNhap(event.target.value)
              }
              placeholder="Nhập tên đăng nhập"
              autoComplete="username"
              required
            />
          </label>

          <label className="login-field">
            <span>Mật khẩu</span>

            <input
              type="password"
              value={matKhau}
              onChange={(event) =>
                setMatKhau(event.target.value)
              }
              placeholder="Nhập mật khẩu"
              autoComplete="current-password"
              required
            />
          </label>
        </div>

        <button
          className="login-submit"
          type="submit"
          disabled={loading}
        >
          <span>
            {loading
              ? 'Đang đăng nhập...'
              : 'Đăng nhập'}
          </span>

          {!loading && (
            <span className="login-submit-arrow">
              →
            </span>
          )}
        </button>

        <div className="login-footer">
          <span className="login-footer-line" />
          <span>POTTEL REPORTING</span>
          <span className="login-footer-line" />
        </div>
      </form>

      <div className="login-copyright">
        © 2026 Pottel Reporting System
      </div>
    </main>
  )
}

export default Login