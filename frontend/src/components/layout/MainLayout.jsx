
import { useState } from 'react'
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
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false)

  return (
    <div className={`main-layout ${sidebarCollapsed ? 'sidebar-collapsed' : ''}`}>
      <Sidebar
        currentPath={currentPath}
        isAdmin={isAdmin}
        onNavigate={onNavigate}
        collapsed={sidebarCollapsed}
        onToggle={() => setSidebarCollapsed(current => !current)}
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
