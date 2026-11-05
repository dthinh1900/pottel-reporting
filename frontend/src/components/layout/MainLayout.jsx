import Sidebar from './Sidebar'
import Header from './Header'
import './MainLayout.css'

function MainLayout({
  currentPage,
  currentUser,
  onNavigate,
  onLogout,
  children,
}) {
  return (
    <div className="main-layout">
      <Sidebar
        currentPage={currentPage}
        onNavigate={onNavigate}
      />

      <div className="main-content">
        <Header
          currentUser={currentUser}
          onLogout={onLogout}
        />

        <main className="page-content">
          {children}
        </main>
      </div>
    </div>
  )
}

export default MainLayout