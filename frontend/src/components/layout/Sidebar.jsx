import logo from '../../assets/pottel-logo.png'
import { REPORT_CATALOG } from '../../config/reports'

// Chỉ lấy những báo cáo đang hoạt động
const ACTIVE_REPORTS = REPORT_CATALOG.filter(
  (report) => report.status === 'active'
)

function Sidebar({ currentPath, isAdmin, onNavigate }) {
  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <img
          src={logo}
          alt="Pottel Reporting"
          className="sidebar-brand-image"
        />
      </div>

      <nav className="sidebar-menu">
        {/* Tổng quan */}
        <p className="sidebar-heading">TỔNG QUAN</p>

        <button
          type="button"
          className={`sidebar-item ${
            currentPath === '/' ? 'active' : ''
          }`}
          onClick={() => onNavigate('/')}
        >
          <span className="sidebar-icon">⌂</span>
          <span>Trang chủ</span>
        </button>

        {/* Danh sách báo cáo */}
        <p className="sidebar-heading">BÁO CÁO</p>

        {ACTIVE_REPORTS.map((report) => (
          <button
            key={report.code}
            type="button"
            className={`sidebar-item ${
              currentPath === report.path ? 'active' : ''
            }`}
            onClick={() => onNavigate(report.path)}
          >
            <span className="sidebar-icon">▤</span>
            <span>{report.menuLabel}</span>
          </button>
        ))}

        {/* Chức năng quản trị dành cho ADMIN */}
        {isAdmin && (
          <>
            <p className="sidebar-heading">QUẢN TRỊ</p>

            <button
              type="button"
              className={`sidebar-item ${
                currentPath === '/admin/accounts' ? 'active' : ''
              }`}
              onClick={() => onNavigate('/admin/accounts')}
            >
              <span className="sidebar-icon">☺</span>
              <span>Quản lý tài khoản</span>
            </button>
          </>
        )}
      </nav>

      <div className="sidebar-footer">
        <strong>Pottel Reporting</strong>
        <span>Hệ thống báo cáo</span>
      </div>
    </aside>
  )
}

export default Sidebar