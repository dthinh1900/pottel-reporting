import './Home.css'
import { REPORT_CATALOG } from '../config/reports'

// Trang chủ - danh mục báo cáo
function Home({ onNavigate }) {
  const total = String(REPORT_CATALOG.length).padStart(2, '0')

  return (
    <main className="home-page">
      <section className="home-heading">
        <div>
          <span className="home-eyebrow">POTTEL BSS / REPORTING SYSTEM</span>
          <h1>Hệ thống báo cáo Pottel BSS</h1>
          <p>
            Hệ thống tổng hợp dữ liệu, đối soát và phân tích hoạt động kinh
            doanh.
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
            <p>Lựa chọn chức năng báo cáo để xem và khai thác dữ liệu.</p>
          </div>

          <span className="report-count">{total} báo cáo</span>
        </div>

        <div className="report-catalog-grid">
          {REPORT_CATALOG.map((report) => {
            const active = report.status === 'active'

            return (
              <button
                key={report.code}
                type="button"
                className={`report-catalog-card ${active ? '' : 'is-disabled'}`}
                disabled={!active}
                onClick={() => active && onNavigate(report.path)}
              >
                <div className="catalog-card-top">
                  <span className="catalog-report-code">
                    REPORT / {report.code}
                  </span>
                  <span className="catalog-card-icon">{report.icon}</span>
                </div>

                <div className="catalog-card-body">
                  <h3>{report.title}</h3>
                  <p>{report.description}</p>
                </div>

                <div className="catalog-card-bottom">
                  <span>{report.category}</span>

                  {active ? (
                    <span className="catalog-arrow">→</span>
                  ) : (
                    <span>Đang xây dựng</span>
                  )}
                </div>
              </button>
            )
          })}
        </div>
      </section>
    </main>
  )
}

export default Home
