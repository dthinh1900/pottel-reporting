
import { useEffect, useState } from 'react'
import { fetchBcDt01 } from '../../../services/reportService'
import {
  formatDate,
  formatNumber,
  todayInVietnam,
  validateDateRange,
} from '../../../utils/format'
import './BcDt01.css'

function getDefaultDates() {
  const today = todayInVietnam()

  return {
    fromDate: `${today.slice(0, 7)}-01`,
    toDate: today,
  }
}

function formatMoney(value) {
  return new Intl.NumberFormat('vi-VN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(Number(value ?? 0))
}

function BcDt01Page() {
  const [filters, setFilters] = useState(getDefaultDates)
  const [draft, setDraft] = useState(getDefaultDates)
  const [report, setReport] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  const dateError = validateDateRange(
    draft.fromDate,
    draft.toDate
  )

  useEffect(() => {
    const controller = new AbortController()

    async function loadReport() {
      const filterError = validateDateRange(
        filters.fromDate,
        filters.toDate
      )

      if (filterError) {
        setError(filterError)
        return
      }

      setLoading(true)
      setError('')

      try {
        const result = await fetchBcDt01(
          filters,
          controller.signal
        )

        setReport(result.dailyRevenue || [])
      } catch (err) {
        if (err.name === 'AbortError') return

        setError(
          err.message || 'Không thể tải dữ liệu báo cáo doanh thu.'
        )
        setReport([])
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    loadReport()

    return () => controller.abort()
  }, [filters, reloadKey])

  function handleSubmit(event) {
    event.preventDefault()

    if (dateError) return

    setFilters({ ...draft })
  }

  function handleReset() {
    const defaults = getDefaultDates()
    setDraft(defaults)
    setFilters(defaults)
  }

  const totalSubscribers = report.reduce(
    (sum, row) => sum + Number(row.so_thue_bao ?? 0),
    0
  )

  const totalBeforeVat = report.reduce(
    (sum, row) => sum + Number(row.doanh_thu_chua_vat ?? 0),
    0
  )

  const totalVat = report.reduce(
    (sum, row) => sum + Number(row.tien_vat ?? 0),
    0
  )

  const totalRevenue = report.reduce(
    (sum, row) => sum + Number(row.tong_doanh_thu ?? 0),
    0
  )

  return (
    <main className="main-container bc-dt-page">
      <div className="report-heading">
        <span className="report-label">REPORT / BC-DT-01</span>
        <h1>Tổng hợp doanh thu theo ngày</h1>
        <p>
          Thống kê số lượng thuê bao và doanh thu phân bổ theo từng ngày,
          bao gồm doanh thu chưa VAT, tiền VAT và tổng doanh thu.
        </p>
      </div>

      <section className="filter-panel">
        <div className="panel-heading">
          <span className="panel-icon">₫</span>
          Bộ lọc thời gian
        </div>

        <form className="filter-content" onSubmit={handleSubmit}>
          {dateError && (
            <p className="field-error" role="alert">
              {dateError}
            </p>
          )}

          <div className="filter-fields">
            <label>
              Từ ngày
              <input
                type="date"
                value={draft.fromDate}
                max={todayInVietnam()}
                onChange={(event) =>
                  setDraft((current) => ({
                    ...current,
                    fromDate: event.target.value,
                  }))
                }
              />
            </label>

            <label>
              Đến ngày
              <input
                type="date"
                value={draft.toDate}
                max={todayInVietnam()}
                onChange={(event) =>
                  setDraft((current) => ({
                    ...current,
                    toDate: event.target.value,
                  }))
                }
              />
            </label>
          </div>

          <div className="filter-actions">
            <button
              type="submit"
              className="primary-button"
              disabled={loading || Boolean(dateError)}
            >
              ⌕ Lọc dữ liệu
            </button>

            <button
              type="button"
              className="secondary-button"
              disabled={loading}
              onClick={handleReset}
            >
              ↻ Tháng hiện tại
            </button>
          </div>
        </form>
      </section>

      {error && (
        <div className="bc-dt-error" role="alert">
          <span>{error}</span>
          <button
            type="button"
            onClick={() => setReloadKey((key) => key + 1)}
          >
            Thử lại
          </button>
        </div>
      )}

      <section className="bc-dt-summary">
        <div className="bc-dt-card">
          <span>Số thuê bao</span>
          <strong>{formatNumber(totalSubscribers)}</strong>
        </div>

        <div className="bc-dt-card">
          <span>Doanh thu chưa VAT</span>
          <strong>{formatMoney(totalBeforeVat)} đ</strong>
        </div>

        <div className="bc-dt-card">
          <span>Tiền VAT</span>
          <strong>{formatMoney(totalVat)} đ</strong>
        </div>

        <div className="bc-dt-card highlight">
          <span>Tổng doanh thu</span>
          <strong>{formatMoney(totalRevenue)} đ</strong>
        </div>
      </section>

      <section className="table-panel">
        <div className="table-title">
          <strong>Chi tiết doanh thu theo ngày</strong>
          <p>
            Số liệu doanh thu được phân bổ theo chu kỳ sử dụng của từng thuê bao.
          </p>
        </div>

        <div className={`table-scroll ${loading ? 'is-loading' : ''}`}>
          <table>
            <thead>
              <tr>
                <th>NGÀY BÁO CÁO</th>
                <th>SỐ THUÊ BAO</th>
                <th>DOANH THU CHƯA VAT</th>
                <th>TIỀN VAT</th>
                <th>TỔNG DOANH THU</th>
              </tr>
            </thead>

            <tbody>
              {report.length === 0 ? (
                <tr>
                  <td colSpan="5" className="empty-row">
                    {loading
                      ? 'Đang tải dữ liệu...'
                      : 'Không có dữ liệu trong khoảng thời gian đã chọn.'}
                  </td>
                </tr>
              ) : (
                report.map((row, index) => (
                  <tr key={`${row.ngay_bao_cao}-${index}`}>
                    <td>{formatDate(row.ngay_bao_cao)}</td>
                    <td>{formatNumber(row.so_thue_bao)}</td>
                    <td>{formatMoney(row.doanh_thu_chua_vat)} đ</td>
                    <td>{formatMoney(row.tien_vat)} đ</td>
                    <td>
                      <strong>
                        {formatMoney(row.tong_doanh_thu)} đ
                      </strong>
                    </td>
                  </tr>
                ))
              )}
            </tbody>

            {report.length > 0 && (
              <tfoot>
                <tr>
                  <th>TỔNG CỘNG</th>
                  <th>{formatNumber(totalSubscribers)}</th>
                  <th>{formatMoney(totalBeforeVat)} đ</th>
                  <th>{formatMoney(totalVat)} đ</th>
                  <th>{formatMoney(totalRevenue)} đ</th>
                </tr>
              </tfoot>
            )}
          </table>
        </div>
      </section>
    </main>
  )
}

export default BcDt01Page
