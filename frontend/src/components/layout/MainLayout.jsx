import Sidebar from './Sidebar'
import Header from './Header'
<<<<<<< HEAD
import Footer from './Footer'
import './MainLayout.css'

function MainLayout({
  currentPath,
  currentUser,
  isAdmin,
=======
import './MainLayout.css'

function MainLayout({
  currentPage,
  currentUser,
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
  onNavigate,
  onLogout,
  children,
}) {
  return (
    <div className="main-layout">
      <Sidebar
<<<<<<< HEAD
        currentPath={currentPath}
        isAdmin={isAdmin}
=======
        currentPage={currentPage}
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
        onNavigate={onNavigate}
      />

      <div className="main-content">
        <Header
          currentUser={currentUser}
<<<<<<< HEAD
          isAdmin={isAdmin}
=======
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
          onLogout={onLogout}
        />

        <main className="page-content">
          {children}
        </main>
<<<<<<< HEAD

        <Footer />
=======
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
      </div>
    </div>
  )
}

<<<<<<< HEAD
export default MainLayout
=======
export default MainLayout
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
