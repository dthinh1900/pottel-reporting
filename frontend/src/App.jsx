import { useState, useEffect, useCallback } from 'react'
import './App.css'
import Login from './pages/Login'
import { getToken, apiFetch, logout } from './services/api'
import MainLayout from './components/layout/MainLayout'

// Cấu hình các thẻ thống kê
const summaryConfig = [
  { key: 'total_kit', label: 'Tổng KIT', color: 'blue', icon: 'K' },
  { key: 'unique_kit_msisdn', label: 'MSISDN KIT duy nhất', color: 'purple', icon: 'M' },
  { key: 'total_goi_moi', label: 'Sự kiện GOI_MOI', color: 'green', icon: 'G' },
  { key: 'unique_goi_moi_msisdn', label: 'MSISDN GOI_MOI duy nhất', color: 'cyan', icon: 'U' },
  { key: 'matched_msisdn', label: 'Khớp KIT', color: 'green', icon: '✓' },
  { key: 'orphan_msisdn', label: 'Không tìm thấy KIT', color: 'red', icon: '!' },
  { key: 'missing_registed_date', label: 'Thiếu ngày đăng ký', color: 'orange', icon: 'Đ' },
  { key: 'repeated_goi_moi_msisdn', label: 'MSISDN có nhiều GOI_MOI', color: 'pink', icon: '↻' },
]

// Định dạng số theo kiểu Việt Nam
function formatNumber(value) {
  return new Intl.NumberFormat('vi-VN').format(value ?? 0)
}

// Định dạng ngày theo múi giờ Việt Nam
function formatDate(value) {
  if (!value) return ''

  return new Intl.DateTimeFormat('vi-VN', {
    timeZone: 'Asia/Ho_Chi_Minh',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(new Date(value))
}

// Trang chủ - danh mục báo cáo
function ReportCatalog({ onOpenReport }) {
  return (
    <main className="home-page">
      <section className="home-heading">
        <div>
          <span className="home-eyebrow">POTTEL REPORTING</span>
          <h1>Trung tâm báo cáo</h1>
          <p>
            Quản lý và tra cứu các báo cáo dữ liệu trong hệ thống.
          </p>
        </div>

        <div className="home-heading-badge">
          <span className="home-status-dot" />
          Hệ thống hoạt động
        </div>
      </section>

      <section className="home-section">
        <div className="home-section-heading">
          <div>
            <h2>Danh mục báo cáo</h2>
            <p>Chọn một báo cáo để bắt đầu tra cứu dữ liệu.</p>
          </div>

          <span className="report-count">01 báo cáo</span>
        </div>

        <div className="report-catalog-grid">
          <button
            type="button"
            className="report-catalog-card"
            onClick={() => onOpenReport('BC-DS-01')}
          >
            <div className="catalog-card-top">
              <span className="catalog-report-code">BC-DS-01</span>
              <span className="catalog-card-icon">▤</span>
            </div>

            <div className="catalog-card-body">
              <h3>Đối soát KIT và GOI_MOI</h3>
              <p>
                Theo dõi và đối chiếu dữ liệu thuê bao KIT với
                các sự kiện đăng ký gói GOI_MOI.
              </p>
            </div>

            <div className="catalog-card-bottom">
              <span>Xem báo cáo</span>
              <span className="catalog-arrow">→</span>
            </div>
          </button>
        </div>
      </section>
    </main>
  )
}

// Các thẻ thống kê - lấy dữ liệu từ Backend
function SummaryCards({ summary }) {
  return (
    <div className="summary-grid">
      {summaryConfig.map((item) => (
        <div
          className={`summary-card ${item.color}`}
          key={item.key}
        >
          <div className="card-top">
            <span>{item.label}</span>
            <span className="card-icon">{item.icon}</span>
          </div>

          <strong>
            {formatNumber(summary?.[item.key])}
          </strong>
        </div>
      ))}
    </div>
  )
}

// Bộ lọc ngày
function ReportFilters({ onFilter, loading }) {
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')

  function resetFilters() {
    setFromDate('')
    setToDate('')
    onFilter('', '')
  }

  return (
    <section className="filter-panel">
      <div className="panel-heading">
        <span className="panel-icon">▤</span>
        Bộ lọc dữ liệu
      </div>

      <div className="filter-content">
        <div className="filter-fields">
          <label>
            Từ ngày GOI_MOI
            <input
              type="date"
              value={fromDate}
              onChange={(event) => setFromDate(event.target.value)}
            />
          </label>

          <label>
            Đến ngày GOI_MOI
            <input
              type="date"
              value={toDate}
              onChange={(event) => setToDate(event.target.value)}
            />
          </label>
        </div>

        <div className="filter-actions">
          <button
            type="button"
            className="primary-button"
            disabled={loading}
            onClick={() => onFilter(fromDate, toDate)}
          >
            ⌕ Lọc dữ liệu
          </button>

          <button
            type="button"
            className="secondary-button"
            disabled={loading}
            onClick={resetFilters}
          >
            ↻ Đặt lại
          </button>
        </div>
      </div>
    </section>
  )
}

// Bảng dữ liệu báo cáo
function ReportTable({
  rows,
  page,
  pageSize,
  totalRows,
  totalPages,
  loading,
  onPageChange,
  onPageSizeChange,
}) {
  const startRecord = totalRows === 0
    ? 0
    : (page - 1) * pageSize + 1

  const endRecord = Math.min(
    page * pageSize,
    totalRows
  )

  return (
    <section className="table-panel">
      <div className="table-title">
        <strong>Bảng 1B - Tổng hợp GOI_MOI theo ngày</strong>
        <p>
          Số liệu được tổng hợp theo ngày ghi nhận GOI_MOI trên CDR.
        </p>
      </div>

      <div className="table-scroll">
        <table>
          <thead>
            <tr>
              <th>NGÀY GHI NHẬN</th>
              <th>SỰ KIỆN GOI_MOI</th>
              <th>MSISDN DUY NHẤT</th>
              <th>SỰ KIỆN KHỚP KIT</th>
              <th>SỰ KIỆN KHÔNG TÌM THẤY KIT</th>
              <th>SỰ KIỆN THIẾU NGÀY ĐĂNG KÝ</th>
            </tr>
          </thead>

          <tbody>
            {loading ? (
              <tr>
                <td colSpan="6" className="empty-row">
                  Đang tải dữ liệu...
                </td>
              </tr>
            ) : rows.length > 0 ? (
              rows.map((row, index) => (
                <tr key={`${row.event_date}-${index}`}>
                  <td>{formatDate(row.event_date)}</td>
                  <td>{formatNumber(row.event_count)}</td>
                  <td>{formatNumber(row.unique_msisdn)}</td>
                  <td>{formatNumber(row.matched_events)}</td>
                  <td>{formatNumber(row.orphan_events)}</td>
                  <td>{formatNumber(row.missing_registed_date_events)}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="6" className="empty-row">
                  Không có dữ liệu phù hợp.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="pagination">
        <div className="pagination-info">
          <span>
            Hiển thị {startRecord} - {endRecord}
            {' '}trong tổng số {formatNumber(totalRows)} bản ghi
          </span>

          <label>
            Số dòng:
            <select
              value={pageSize}
              disabled={loading}
              onChange={(event) =>
                onPageSizeChange(Number(event.target.value))
              }
            >
              <option value={5}>5</option>
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={50}>50</option>
            </select>
          </label>
        </div>

        <div className="page-controls">
          <button
            type="button"
            disabled={loading || page === 1}
            onClick={() => onPageChange(1)}
          >
            «
          </button>

          <button
            type="button"
            disabled={loading || page === 1}
            onClick={() => onPageChange(page - 1)}
          >
            Trước
          </button>

          <span>
            Trang {page} / {Math.max(1, totalPages)}
          </span>

          <button
            type="button"
            disabled={loading || page >= totalPages}
            onClick={() => onPageChange(page + 1)}
          >
            Sau
          </button>

          <button
            type="button"
            disabled={loading || page >= totalPages}
            onClick={() => onPageChange(totalPages)}
          >
            »
          </button>
        </div>
      </div>
    </section>
  )
}
// Bảng 1C - Chi tiết thuê bao
function SubscriberTable({
  rows,
  page,
  pageSize,
  totalRows,
  totalPages,
  loading,
  onPageChange,
  onPageSizeChange,
}) {
  const startRecord = totalRows === 0
    ? 0
    : (page - 1) * pageSize + 1

  const endRecord = Math.min(page * pageSize, totalRows)

  return (
    <section className="table-panel">
      <div className="table-title">
        <strong>Bảng 1C - Chi tiết thuê bao</strong>
        <p>Chi tiết đối soát dữ liệu KIT và sự kiện GOI_MOI theo MSISDN.</p>
      </div>

      <div className="table-scroll">
        <table>
          <thead>
            <tr>
              <th>STT</th>
              <th>MSISDN</th>
              <th>TRẠNG THÁI KIT</th>
              <th>NGÀY ĐĂNG KÝ</th>
              <th>SỐ DÒNG KIT</th>
              <th>SỐ SỰ KIỆN GOI_MOI</th>
              <th>NGÀY GOI_MOI ĐẦU TIÊN</th>
              <th>NGÀY GOI_MOI CUỐI CÙNG</th>
              <th>TÌNH TRẠNG ĐỐI SOÁT</th>
            </tr>
          </thead>

          <tbody>
            {loading ? (
              <tr>
                <td colSpan="9" className="empty-row">
                  Đang tải dữ liệu...
                </td>
              </tr>
            ) : rows.length > 0 ? (
              rows.map((row, index) => (
                <tr key={`${row.msisdn}-${index}`}>
                  <td>{startRecord + index}</td>
                  <td>{row.msisdn}</td>
                  <td>{row.kit_status || '-'}</td>
                  <td>{row.registed_date ? formatDate(row.registed_date) : '-'}</td>
                  <td>{formatNumber(row.kit_row_count)}</td>
                  <td>{formatNumber(row.event_count)}</td>
                  <td>{row.first_event_date ? formatDate(row.first_event_date) : '-'}</td>
                  <td>{row.last_event_date ? formatDate(row.last_event_date) : '-'}</td>
                  <td>{row.reconciliation_status || '-'}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="9" className="empty-row">
                  Không có dữ liệu phù hợp.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="pagination">
        <div className="pagination-info">
          <span>
            Hiển thị {startRecord} - {endRecord}
            {' '}trong tổng số {formatNumber(totalRows)} bản ghi
          </span>

          <label>
            Số dòng:
            <select
              value={pageSize}
              disabled={loading}
              onChange={(event) => onPageSizeChange(Number(event.target.value))}
            >
              <option value={5}>5</option>
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={50}>50</option>
            </select>
          </label>
        </div>

        <div className="page-controls">
          <button
            type="button"
            disabled={loading || page === 1}
            onClick={() => onPageChange(1)}
          >
            «
          </button>

          <button
            type="button"
            disabled={loading || page === 1}
            onClick={() => onPageChange(page - 1)}
          >
            Trước
          </button>

          <span>Trang {page} / {Math.max(1, totalPages)}</span>

          <button
            type="button"
            disabled={loading || page >= totalPages}
            onClick={() => onPageChange(page + 1)}
          >
            Sau
          </button>

          <button
            type="button"
            disabled={loading || page >= totalPages}
            onClick={() => onPageChange(totalPages)}
          >
            »
          </button>
        </div>
      </div>
    </section>
  )
}
// Trang chi tiết báo cáo
function ReportPage() {
  const [activeTab, setActiveTab] = useState('BC1B')
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')

  const [reportData, setReportData] = useState(null)
  const [page, setPage] = useState(1)
  const [pageSize, setPageSize] = useState(5)

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // Gọi API lấy dữ liệu báo cáo
  const loadReport = useCallback(async () => {
    setLoading(true)
    setError('')

    try {
      const params = new URLSearchParams({
        bc: activeTab === 'BC1B' ? '1B' : '1C',
        page: String(page),
        pageSize: String(pageSize),
      })

      if (fromDate) params.set('fromDate', fromDate)
      if (toDate) params.set('toDate', toDate)

      const data = await apiFetch(
        `/reports/bc-ds-01?${params.toString()}`
      )

      setReportData(data)
    } catch (err) {
      setError(err.message || 'Không thể tải dữ liệu báo cáo.')
    } finally {
      setLoading(false)
    }
  }, [activeTab, fromDate, toDate, page, pageSize])

  // Tự tải lại khi thay đổi tab, bộ lọc hoặc trang
  useEffect(() => {
    loadReport()
  }, [loadReport])

  function handleFilter(from, to) {
    setFromDate(from)
    setToDate(to)
    setPage(1)
  }

  function handlePageSizeChange(size) {
    setPageSize(size)
    setPage(1)
  }

  return (
    <main className="main-container">
      <div className="report-heading">
        <span className="report-label">REPORT / BC-DS-01</span>
        <h1>Đối soát KIT và GOI_MOI</h1>
        <p>Tổng quan dữ liệu KIT và sự kiện GOI_MOI.</p>
      </div>

      <div className="notice">
        <span>ⓘ</span>
        Dữ liệu báo cáo được truy vấn trực tiếp từ Backend và PostgreSQL.
      </div>

      <section className="overview">
        <h2>Tổng quan dữ liệu</h2>
        <p>Các chỉ số thống kê từ cơ sở dữ liệu</p>

        {reportData ? (
          <SummaryCards summary={reportData.summary} />
        ) : (
          <p>{loading ? 'Đang tải số liệu thống kê...' : 'Chưa có dữ liệu thống kê.'}</p>
        )}
      </section>

      <section className="report-section">
        <div className="section-heading">
          <div>
            <h2>Chi tiết báo cáo</h2>
            <p>Lựa chọn loại báo cáo và điều kiện tra cứu</p>
          </div>

          <div className="tabs">
            <button
              type="button"
              className={activeTab === 'BC1B' ? 'active' : ''}
              onClick={() => {
                setActiveTab('BC1B')
                setPage(1)
              }}
            >
              BC1B - Theo ngày
            </button>

            <button
              type="button"
              className={activeTab === 'BC1C' ? 'active' : ''}
              onClick={() => {
                setActiveTab('BC1C')
                setPage(1)
              }}
            >
              BC1C - Chi tiết thuê bao
            </button>
          </div>
        </div>

        <ReportFilters
          onFilter={handleFilter}
          loading={loading}
        />

        {error && (
          <div className="notice">
            <span>⚠</span>
            {error}
            <button type="button" onClick={loadReport}>
              Thử lại
            </button>
          </div>
        )}

        {activeTab === 'BC1B' ? (
          <ReportTable
            rows={reportData?.dailySummary || []}
            page={page}
            pageSize={pageSize}
            totalRows={reportData?.totalRows || 0}
            totalPages={reportData?.totalPages || 1}
            loading={loading}
            onPageChange={setPage}
            onPageSizeChange={handlePageSizeChange}
          />
        ) : (
          <SubscriberTable
            rows={reportData?.subscriberDetails || []}
            page={page}
            pageSize={pageSize}
            totalRows={reportData?.totalRows || 0}
            totalPages={reportData?.totalPages || 1}
            loading={loading}
            onPageChange={setPage}
            onPageSizeChange={handlePageSizeChange}
          />
        )}
      </section>
    </main>
  )
}

// Component chính
function App() {
  const [currentPage, setCurrentPage] = useState('home')
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [checkingAuth, setCheckingAuth] = useState(true)
  const [currentUser, setCurrentUser] = useState(null)

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
        setIsAuthenticated(false)
      } finally {
        setCheckingAuth(false)
      }
    }

    checkLogin()
  }, [])

  // Xử lý đăng nhập thành công
  function handleLoginSuccess(user) {
    setCurrentUser(user)
    setIsAuthenticated(true)
    setCurrentPage('home')
  }

  // Xử lý đăng xuất
  function handleLogout() {
    logout()
    setCurrentUser(null)
    setIsAuthenticated(false)
    setCurrentPage('home')
  }

  if (checkingAuth) {
    return (
      <div className="app">
        Đang kiểm tra đăng nhập...
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Login onLoginSuccess={handleLoginSuccess} />
  }

  return (
    <div className="app">
      <MainLayout
        currentPage={currentPage}
        currentUser={currentUser}
        onNavigate={setCurrentPage}
        onLogout={handleLogout}
      >
        {currentPage === 'home' ? (
          <ReportCatalog
            onOpenReport={() => setCurrentPage('report')}
          />
        ) : (
          <ReportPage />
        )}
      </MainLayout>
    </div>
  )
}

export default App