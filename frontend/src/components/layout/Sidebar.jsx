import logo from '../../assets/pottel-logo.png'

function Sidebar({ currentPage, onNavigate }) {
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
          className={`sidebar-item ${currentPage === 'home' ? 'active' : ''}`}
          onClick={() => onNavigate('home')}
        >
          <span className="sidebar-icon">⌂</span>
          <span>Trang chủ</span>
        </button>

        <p className="sidebar-heading">BÁO CÁO</p>

        <button
          type="button"
          className={`sidebar-item ${currentPage === 'report' ? 'active' : ''}`}
          onClick={() => onNavigate('report')}
        >
          <span className="sidebar-icon">▤</span>
          <span>Đối soát KIT và GOI_MOI</span>
        </button>
      </nav>

      <div className="sidebar-footer">
        <strong>Pottel Reporting</strong>
        <span>Hệ thống báo cáo</span>
      </div>
    </aside>
  )
}

export default Sidebar