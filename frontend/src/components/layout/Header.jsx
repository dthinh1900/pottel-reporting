<<<<<<< HEAD
function Header({ currentUser, isAdmin, onLogout }) {
=======
function Header({ currentUser, onLogout }) {
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
  const userName =
    typeof currentUser?.tenDangNhap === 'string' &&
    currentUser.tenDangNhap.trim()
      ? currentUser.tenDangNhap.trim()
      : 'Người dùng'

  const avatar = userName.charAt(0).toUpperCase()

  return (
    <header className="main-header">
      <div className="header-brand">
        <span className="header-brand-name">POTTEL</span>
        <span className="header-brand-divider">/</span>
        <span className="header-brand-description">
          Reporting System
        </span>
      </div>

      <div className="header-user">
        <div className="user-avatar" title={userName}>
          {avatar}
        </div>

        <span className="user-name" title={userName}>
          {userName}
<<<<<<< HEAD
          {isAdmin ? ' (ADMIN)' : ''}
=======
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
        </span>

        <button
          type="button"
          className="header-logout"
          onClick={onLogout}
          aria-label="Đăng xuất khỏi hệ thống"
        >
          Đăng xuất
        </button>
      </div>
    </header>
  )
}

export default Header