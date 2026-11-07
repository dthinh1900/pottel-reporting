import Sidebar from './Sidebar'
import Header from './Header'
import Footer from './Footer'
import './MainLayout.css'

function MainLayout({
  currentPath,
  currentUser,
  isAdmin,
  onNavigate,
  onLogout,
  children,
}) {
  return (
    <div className="main-layout">
      <Sidebar
        currentPath={currentPath}
        isAdmin={isAdmin}
        onNavigate={onNavigate}
      />

      <div className="main-content">
        <Header
          currentUser={currentUser}
          isAdmin={isAdmin}
          onLogout={onLogout}
        />

        <main className="page-content">
          {children}
        </main>

        <Footer />
      </div>
    </div>
  )
}

export default MainLayout