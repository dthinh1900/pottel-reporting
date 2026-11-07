import { useCallback, useEffect, useState } from 'react'
import './AccountsPage.css'
import Alert from '../../components/common/Alert'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import {
  createAccount,
  fetchAccounts,
  toggleAccount,
} from '../../services/accountService'
import { formatDateTime } from '../../utils/format'

const MIN_PASSWORD_LENGTH = 8

// Trang Quản lý tài khoản (chỉ ADMIN)
function AccountsPage({ currentUsername }) {
  const [accounts, setAccounts] = useState([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')

  const [message, setMessage] = useState(null) // { type, text }

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [creating, setCreating] = useState(false)

  const [pendingToggle, setPendingToggle] = useState(null) // tài khoản chờ xác nhận
  const [toggling, setToggling] = useState(false)

  const loadAccounts = useCallback(async () => {
    setLoading(true)
    setLoadError('')

    try {
      setAccounts(await fetchAccounts())
    } catch (err) {
      setLoadError(err.message || 'Không thể tải danh sách tài khoản.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadAccounts()
  }, [loadAccounts])

  async function handleCreate(event) {
    event.preventDefault()
    setMessage(null)

    const cleanUsername = username.trim()

    if (!cleanUsername || !password.trim()) {
      setMessage({
        type: 'error',
        text: 'Tên đăng nhập và mật khẩu không được để trống.',
      })
      return
    }

    if (password.length < MIN_PASSWORD_LENGTH) {
      setMessage({
        type: 'error',
        text: `Mật khẩu phải có ít nhất ${MIN_PASSWORD_LENGTH} ký tự.`,
      })
      return
    }

    setCreating(true)

    try {
      await createAccount(cleanUsername, password)
      setUsername('')
      setPassword('')
      setMessage({ type: 'success', text: 'Tạo tài khoản thành công.' })
      await loadAccounts()
    } catch (err) {
      setMessage({ type: 'error', text: err.message })
    } finally {
      setCreating(false)
    }
  }

  async function handleConfirmToggle() {
    if (!pendingToggle) return

    setToggling(true)
    setMessage(null)

    try {
      const updated = await toggleAccount(pendingToggle.id)

      setMessage({
        type: 'success',
        text: updated.hoatDong
          ? 'Đã mở khóa tài khoản.'
          : 'Đã khóa tài khoản.',
      })
      await loadAccounts()
    } catch (err) {
      setMessage({ type: 'error', text: err.message })
    } finally {
      setToggling(false)
      setPendingToggle(null)
    }
  }

  return (
    <main className="main-container">
      <div className="report-heading">
        <span className="report-label">ADMIN / ACCOUNTS</span>
        <h1>Quản lý tài khoản</h1>
        <p>Danh sách tài khoản sử dụng Pottel Reporting</p>
      </div>

      {message && (
        <Alert type={message.type} onClose={() => setMessage(null)}>
          {message.text}
        </Alert>
      )}

      <section className="table-panel">
        <div className="table-title">
          <strong>Tạo tài khoản mới</strong>
          <p>Nhập thông tin để tạo tài khoản sử dụng hệ thống (vai trò USER).</p>
        </div>

        <form className="filter-content account-form" onSubmit={handleCreate}>
          <label>
            Tên đăng nhập
            <input
              type="text"
              value={username}
              maxLength={100}
              placeholder="Nhập tên đăng nhập"
              autoComplete="off"
              required
              onChange={(event) => setUsername(event.target.value)}
            />
          </label>

          <label>
            Mật khẩu
            <input
              type="password"
              value={password}
              minLength={MIN_PASSWORD_LENGTH}
              placeholder="Tối thiểu 8 ký tự"
              autoComplete="new-password"
              required
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>

          <button type="submit" className="primary-button" disabled={creating}>
            {creating ? 'Đang tạo...' : '+ Tạo tài khoản'}
          </button>
        </form>
      </section>

      <section className="table-panel">
        <div className="table-title">
          <strong>Danh sách tài khoản</strong>
          <p>Thông tin các tài khoản trong hệ thống</p>
        </div>

        {loadError && (
          <div style={{ padding: '16px 20px 0' }}>
            <Alert
              type="error"
              actions={
                <button type="button" onClick={loadAccounts}>
                  Thử lại
                </button>
              }
            >
              {loadError}
            </Alert>
          </div>
        )}

        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>TÊN ĐĂNG NHẬP</th>
                <th>VAI TRÒ</th>
                <th>TRẠNG THÁI</th>
                <th>NGÀY TẠO</th>
                <th>THAO TÁC</th>
              </tr>
            </thead>

            <tbody>
              {accounts.length === 0 ? (
                <tr>
                  <td colSpan="6" className="empty-row">
                    {loading ? 'Đang tải dữ liệu...' : 'Chưa có tài khoản nào.'}
                  </td>
                </tr>
              ) : (
                accounts.map((account) => {
                  const isCurrent = account.tenDangNhap === currentUsername

                  return (
                    <tr key={account.id}>
                      <td>{account.id}</td>
                      <td className="account-name">{account.tenDangNhap}</td>
                      <td>
                        <span
                          className={`badge ${
                            account.vaiTro === 'ADMIN'
                              ? 'badge-warning'
                              : 'badge-neutral'
                          }`}
                        >
                          {account.vaiTro}
                        </span>
                      </td>
                      <td>
                        <span
                          className={`badge ${
                            account.hoatDong ? 'badge-success' : 'badge-danger'
                          }`}
                        >
                          {account.hoatDong ? 'Đang hoạt động' : 'Đã khóa'}
                        </span>
                      </td>
                      <td>{formatDateTime(account.ngayTao)}</td>
                      <td>
                        {isCurrent ? (
                          <span className="current-account">
                            Tài khoản hiện tại
                          </span>
                        ) : (
                          <button
                            type="button"
                            className={`table-action-button ${
                              account.hoatDong ? 'lock' : 'unlock'
                            }`}
                            disabled={toggling}
                            onClick={() => setPendingToggle(account)}
                          >
                            {account.hoatDong ? 'Khóa' : 'Mở khóa'}
                          </button>
                        )}
                      </td>
                    </tr>
                  )
                })
              )}
            </tbody>
          </table>
        </div>
      </section>

      {pendingToggle && (
        <ConfirmDialog
          title={pendingToggle.hoatDong ? 'Khóa tài khoản' : 'Mở khóa tài khoản'}
          message={`Bạn có chắc chắn muốn ${
            pendingToggle.hoatDong ? 'khóa' : 'mở khóa'
          } tài khoản "${pendingToggle.tenDangNhap}"?`}
          confirmLabel={pendingToggle.hoatDong ? 'Khóa' : 'Mở khóa'}
          danger={pendingToggle.hoatDong}
          busy={toggling}
          onConfirm={handleConfirmToggle}
          onCancel={() => setPendingToggle(null)}
        />
      )}
    </main>
  )
}

export default AccountsPage
