
import { useState } from 'react'
import logo from '../../assets/pottel-logo.png'
import { REPORT_CATALOG } from '../../config/reports'

const REPORT_GROUPS = [
  {
    id: 'overview',
    title: 'Tổng quan dữ liệu',
    category: 'Tổng quan dữ liệu',
    icon: '▤',
  },
  {
    id: 'revenue',
    title: 'Thống kê doanh thu',
    category: 'Thống kê doanh thu',
    icon: '₫',
  },
]

function Sidebar({
  currentPath,
  isAdmin,
  onNavigate,
  collapsed,
  onToggle,
}) {
  const activeReports = REPORT_CATALOG.filter(
    report => report.status === 'active'
  )

  function getActiveGroup() {
    const activeReport = activeReports.find(
      report => report.path === currentPath
    )

    return REPORT_GROUPS.find(
      group => group.category === activeReport?.category
    )?.id || 'overview'
  }

  const [openGroups, setOpenGroups] = useState(() => ({
    [getActiveGroup()]: true,
  }))

  // Bấm vào vùng Sidebar để mở rộng hoặc thu gọn
  function handleSidebarClick() {
    onToggle()
  }

  // Mở hoặc đóng nhóm báo cáo
  function handleGroupClick(groupId) {
    // Nếu Sidebar đang thu gọn thì mở rộng và mở nhóm được chọn
    if (collapsed) {
      onToggle()

      setOpenGroups(current => ({
        ...current,
        [groupId]: true,
      }))

      return
    }

    setOpenGroups(current => ({
      ...current,
      [groupId]: !current[groupId],
    }))
  }

  // Điều hướng đến màn hình
  function handleNavigate(path) {
    onNavigate(path)
  }

  return (
    <aside
      className={`sidebar ${collapsed ? 'collapsed' : ''}`}
      onClick={handleSidebarClick}
    >
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
          title="Trang chủ"
          className={`sidebar-item ${
            currentPath === '/' ? 'active' : ''
          }`}
          onClick={event => {
            event.stopPropagation()
            handleNavigate('/')
          }}
        >
          <span className="sidebar-icon">⌂</span>
          <span className="sidebar-item-label">Trang chủ</span>
        </button>

        <p className="sidebar-heading">BÁO CÁO</p>

        {REPORT_GROUPS.map(group => {
          const reports = activeReports.filter(
            report => report.category === group.category
          )

          const isOpen = Boolean(openGroups[group.id])

          const hasActiveReport = reports.some(
            report => report.path === currentPath
          )

          if (reports.length === 0) return null

          return (
            <div
              className="sidebar-report-group"
              key={group.id}
            >
              <button
                type="button"
                title={collapsed ? group.title : undefined}
                className={`sidebar-group-toggle ${
                  hasActiveReport ? 'has-active' : ''
                }`}
                aria-expanded={isOpen}
                onClick={event => {
                  event.stopPropagation()
                  handleGroupClick(group.id)
                }}
              >
                <span className="sidebar-group-icon">
                  {group.icon}
                </span>

                <span className="sidebar-group-label">
                  {group.title}
                </span>

                <span
                  className={`sidebar-group-arrow ${
                    isOpen ? 'open' : ''
                  }`}
                >
                  ›
                </span>
              </button>

              {isOpen && (
                <div className="sidebar-group-items">
                  {reports.map(report => (
                    <button
                      key={report.code}
                      type="button"
                      title={
                        collapsed
                          ? report.menuLabel || report.title
                          : undefined
                      }
                      className={`sidebar-item sidebar-subitem ${
                        currentPath === report.path ? 'active' : ''
                      }`}
                      onClick={event => {
                        event.stopPropagation()
                        handleNavigate(report.path)
                      }}
                    >
                      <span className="sidebar-item-label">
                        {report.menuLabel || report.title}
                      </span>
                    </button>
                  ))}
                </div>
              )}
            </div>
          )
        })}

        {isAdmin && (
          <>
            <p className="sidebar-heading">QUẢN TRỊ</p>

            <button
              type="button"
              title="Quản lý tài khoản"
              className={`sidebar-item ${
                currentPath === '/admin/accounts' ? 'active' : ''
              }`}
              onClick={event => {
                event.stopPropagation()
                handleNavigate('/admin/accounts')
              }}
            >
              <span className="sidebar-icon">☺</span>
              <span className="sidebar-item-label">
                Quản lý tài khoản
              </span>
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
