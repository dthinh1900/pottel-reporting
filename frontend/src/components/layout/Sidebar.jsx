import logo from '../../assets/pottel-logo.png'
<<<<<<< HEAD
import { REPORT_CATALOG } from '../../config/reports'

const ACTIVE_REPORTS = REPORT_CATALOG.filter(
  (report) => report.status === 'active'
)

function Sidebar({ currentPath, isAdmin, onNavigate }) {
=======

function Sidebar({ currentPage, onNavigate }) {
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
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
        <p className="sidebar-heading">TỔNG QUAN</p>

        <button
          type="button"
<<<<<<< HEAD
          className={`sidebar-item ${currentPath === '/' ? 'active' : ''}`}
          onClick={() => onNavigate('/')}
=======
          className={`sidebar-item ${currentPage === 'home' ? 'active' : ''}`}
          onClick={() => onNavigate('home')}
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
        >
          <span className="sidebar-icon">⌂</span>
          <span>Trang chủ</span>
        </button>

        <p className="sidebar-heading">BÁO CÁO</p>

<<<<<<< HEAD
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
=======
        <button
          type="button"
          className={`sidebar-item ${currentPage === 'report' ? 'active' : ''}`}
          onClick={() => onNavigate('report')}
        >
          <span className="sidebar-icon">▤</span>
          <span>Đối soát KIT và GOI_MOI</span>
        </button>
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
      </nav>

      <div className="sidebar-footer">
        <strong>Pottel Reporting</strong>
        <span>Hệ thống báo cáo</span>
      </div>
    </aside>
  )
}

<<<<<<< HEAD
export default Sidebar
=======
export default Sidebar
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
