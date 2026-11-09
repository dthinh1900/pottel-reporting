
import { useEffect, useState } from 'react'
import { fetchBcDt02 } from '../../../services/reportService'
import {
  formatDate,
  formatNumber,
  todayInVietnam,
} from '../../../utils/format'
import './BcDt02.css'

function formatMoney(value) {
  return new Intl.NumberFormat('vi-VN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(Number(value ?? 0))
}

function BcDt02Page() {
  const today = todayInVietnam()

  const [draftDate, setDraftDate] = useState(today)
  const [selectedDate, setSelectedDate] = useState(today)
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  useEffect(() => {
    if (!selectedDate) return

    const controller = new AbortController()

    async function loadReport() {
      setLoading(true)
      setError('')

      try {
        const result = await fetchBcDt02(
          selectedDate,
          controller.signal
        )

        setRows(result.subscriberDetails || [])
      } catch (err) {
        if (err.name === 'AbortError') return

        setRows([])
        setError(
          err.message || 'Không thể tải chi tiết doanh thu.'
        )
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    loadReport()

    return () => controller.abort()
  }, [selectedDate, reloadKey])

  function handleSubmit(event) {
    event.preventDefault()

    if (!draftDate) {
      setError('Vui lòng chọn ngày xem báo cáo.')
      return
    }

    if (draftDate > todayInVietnam()) {
      setError('Ngày xem báo cáo không được vượt quá ngày hiện tại.')
      return
    }

    setSelectedDate(draftDate)
  }

  const totalBeforeVat = rows.reduce(
    (sum, row) => sum + Number(row.doanh_thu_chua_vat ?? 0),
    0
  )

  const totalVat = rows.reduce(
    (sum, row) => sum + Number(row.tien_vat ?? 0),
    0
  )

  const totalRevenue = rows.reduce(
    (sum, row) => sum + Number(row.tong_doanh_thu ?? 0),
    0
  )

  return (
    <main className="main-container bc-dt02-page">
      <div className="report-heading">
        <span className="report-label">REPORT / BC-DT-02</span>
        <h1>Chi tiết doanh thu theo thuê bao</h1>
        <p>
          Tra cứu doanh thu được phân bổ của từng thuê bao trong ngày
          được chọn.
        </p>
      </div>

      <section className="filter-panel">
        <div className="panel-heading">
          <span className="panel-icon">₫</span>
          Chọn ngày báo cáo
        </div>

        <form className="filter-content" onSubmit={handleSubmit}>
          <div className="filter-fields">
            <label>
              Ngày xem báo cáo
              <input
                type="date"
                value={draftDate}
                max={today}
                onChange={(event) => setDraftDate(event.target.value)}
              />
            </label>
          </div>

          <div className="filter-actions">
            <button
              type="submit"
              className="primary-button"
              disabled={loading || !draftDate}
            >
              ⌕ Xem dữ liệu
            </button>

            <button
              type="button"
              className="secondary-button"
              disabled={loading}
              onClick={() => {
                setDraftDate(today)
                setSelectedDate(today)
              }}
            >
              ↻ Hôm nay
            </button>
          </div>
        </form>
      </section>

      {error && (
        <div className="bc-dt02-error" role="alert">
          <span>{error}</span>
          <button
            type="button"
            onClick={() => setReloadKey((key) => key + 1)}
          >
            Thử lại
          </button>
        </div>
      )}

      <section className="bc-dt02-summary">
        <div className="bc-dt02-card">
          <span>Ngày báo cáo</span>
          <strong>{formatDate(selectedDate)}</strong>
        </div>

        <div className="bc-dt02-card">
          <span>Số thuê bao / KIT</span>
          <strong>{formatNumber(rows.length)}</strong>
        </div>

        <div className="bc-dt02-card">
          <span>Doanh thu chưa VAT</span>
          <strong>{formatMoney(totalBeforeVat)} đ</strong>
        </div>

        <div className="bc-dt02-card highlight">
          <span>Tổng doanh thu</span>
          <strong>{formatMoney(totalRevenue)} đ</strong>
        </div>
      </section>

      <section className="table-panel">
        <div className="bc-dt02-table-heading">
          <div>
            <strong>Danh sách doanh thu từng thuê bao</strong>
            <p>
              Ngày phân bổ: {formatDate(selectedDate)}
            </p>
          </div>

          <span>{formatNumber(rows.length)} dòng dữ liệu</span>
        </div>

        <div className={`table-scroll ${loading ? 'is-loading' : ''}`}>
          <table>
            <thead>
              <tr>
                <th>STT</th>
                <th>MSISDN</th>
                <th>MÃ KIT</th>
                <th>MÃ GÓI</th>
                <th>NGÀY KÍCH HOẠT</th>
                <th>NGÀY BẮT ĐẦU</th>
                <th>NGÀY KẾT THÚC</th>
                <th>CHU KỲ</th>
                <th>NGÀY THỨ</th>
                <th>DOANH THU CHƯA VAT</th>
                <th>TIỀN VAT</th>
                <th>TỔNG DOANH THU</th>
              </tr>
            </thead>

            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="12" className="empty-row">
                    Đang tải dữ liệu...
                  </td>
                </tr>
              ) : rows.length === 0 ? (
                <tr>
                  <td colSpan="12" className="empty-row">
                    Không có dữ liệu doanh thu trong ngày này.
                  </td>
                </tr>
              ) : (
                rows.map((row, index) => (
                  <tr key={`${row.kit_id}-${row.msisdn}-${index}`}>
                    <td>{index + 1}</td>
                    <td className="msisdn-cell">{row.msisdn}</td>
                    <td>{row.kit_id}</td>
                    <td>{row.ma_goi}</td>
                    <td>{formatDate(row.ngay_kich_hoat)}</td>
                    <td>{formatDate(row.ngay_d1)}</td>
                    <td>{formatDate(row.ngay_ket_thuc)}</td>
                    <td>{row.chu_ky} ngày</td>
                    <td>{row.ngay_thu}/{row.chu_ky}</td>
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

            {!loading && rows.length > 0 && (
              <tfoot>
                <tr>
                  <th colSpan="9">TỔNG CỘNG</th>
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

export default BcDt02Page
