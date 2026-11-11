
import { useEffect, useMemo, useState } from 'react'
import {
  fetchReportCatalog,
  fetchReportByCode,
} from '../../services/reportService'
import Alert from '../../components/common/Alert'
import './GenericReportPage.css'

function GenericReportPage({ initialReportCode = null }) {
  const [reports, setReports] = useState([])
  const [maBaoCao, setMaBaoCao] = useState('')
  const [metadata, setMetadata] = useState(null)
  const [thamSo, setThamSo] = useState({})
  const [result, setResult] = useState(null)
  const [pageSize, setPageSize] = useState(20)

  const [loadingCatalog, setLoadingCatalog] = useState(true)
  const [loadingReport, setLoadingReport] = useState(false)
  const [error, setError] = useState('')

  // =====================================================
  // 1. HÀM HỖ TRỢ
  // =====================================================

  function chuanHoaTen(value) {
    return String(value || '')
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/đ/g, 'd')
      .replace(/[^a-z0-9]/g, '')
  }

  function taoThamSoMacDinh(report) {
    const macDinh = {}

    const homNay = new Date()

    const ngayHomNay = [
      homNay.getFullYear(),
      String(homNay.getMonth() + 1).padStart(2, '0'),
      String(homNay.getDate()).padStart(2, '0'),
    ].join('-')

    const ngayDauThang = [
      homNay.getFullYear(),
      String(homNay.getMonth() + 1).padStart(2, '0'),
      '01',
    ].join('-')

    ;(report?.thamSo || []).forEach((item) => {
      const ten = chuanHoaTen(item.ten)
      const nhan = chuanHoaTen(item.nhan)
      const tenThamSo = `${ten} ${nhan}`

      if (item.kieu !== 'NGAY') {
        macDinh[item.ten] = ''
        return
      }

      const laNgayBatDau =
        tenThamSo.includes('tungay') ||
        tenThamSo.includes('ngaytu') ||
        tenThamSo.includes('startdate') ||
        tenThamSo.includes('fromdate') ||
        tenThamSo.includes('ngaybatdau') ||
        tenThamSo.includes('batdau')

      const laNgayKetThuc =
        tenThamSo.includes('denngay') ||
        tenThamSo.includes('ngayden') ||
        tenThamSo.includes('enddate') ||
        tenThamSo.includes('todate') ||
        tenThamSo.includes('ngayketthuc') ||
        tenThamSo.includes('ketthuc')

      if (laNgayBatDau) {
        macDinh[item.ten] = ngayDauThang
      } else if (laNgayKetThuc) {
        macDinh[item.ten] = ngayHomNay
      } else {
        macDinh[item.ten] = ngayHomNay
      }
    })

    const kichThuoc =
      report?.cauHinhPhanTrang?.kichThuocMacDinh || 20

    setPageSize(kichThuoc)
    setThamSo(macDinh)
    setResult(null)

    return macDinh
  }

  function layBaoCaoTheoMa(code, danhSach = reports) {
    return (
      danhSach.find((item) => item.maBaoCao === code) || null
    )
  }

  function layMetadataBang(tableName) {
    return (
      result?.metadataBang?.[tableName] ||
      metadata?.cacBang?.[tableName] ||
      {}
    )
  }

  function layTenBang(tableName) {
    return (
      layMetadataBang(tableName).nhan ||
      `Bảng ${tableName}`
    )
  }

  function layCauHinhCot(tableName, columnName) {
    return (
      layMetadataBang(tableName).cacCot?.[columnName] || {}
    )
  }

  function dinhDangGiaTri(value, format) {
    if (value === null || value === undefined) return ''

    if (!format) return String(value)

    format = String(format).toUpperCase()

    const number = Number(value)

    if (format === 'NUMBER' && Number.isFinite(number)) {
      return new Intl.NumberFormat('vi-VN').format(number)
    }

    if (
      ['CURRENCY', 'VND', 'MONEY'].includes(format) &&
      Number.isFinite(number)
    ) {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        maximumFractionDigits: 2,
      }).format(number)
    }

    if (format === 'PERCENT' && Number.isFinite(number)) {
      return `${new Intl.NumberFormat('vi-VN', {
        maximumFractionDigits: 2,
      }).format(number)}%`
    }

    if (format === 'DATE') {
      const date = new Date(value)

      if (!Number.isNaN(date.getTime())) {
        return date.toLocaleDateString('vi-VN')
      }
    }

    if (format === 'DATETIME') {
      const date = new Date(value)

      if (!Number.isNaN(date.getTime())) {
        return date.toLocaleString('vi-VN')
      }
    }

    return String(value)
  }

  // =====================================================
  // 2. KIỂM TRA THAM SỐ
  // =====================================================

  function coDuThamSoBatBuoc(report, values) {
    return !(report?.thamSo || []).some(
      (item) =>
        item.batBuoc &&
        (
          values[item.ten] === '' ||
          values[item.ten] === null ||
          values[item.ten] === undefined
        )
    )
  }

  function kiemTraThamSo() {
    const requiredFilter = (metadata?.thamSo || []).find(
      (item) =>
        item.batBuoc &&
        (
          thamSo[item.ten] === '' ||
          thamSo[item.ten] === null ||
          thamSo[item.ten] === undefined
        )
    )

    if (requiredFilter) {
      setError(
        `Vui lòng nhập ${requiredFilter.nhan || requiredFilter.ten}.`
      )
      return false
    }

    return true
  }

  // =====================================================
  // 3. TẢI DỮ LIỆU BÁO CÁO
  // =====================================================

  async function taiBaoCao(code, params) {
    const response = await fetchReportByCode(code, params)
    const data = response?.data ?? response

    setResult(data)
  }

  async function tuDongTaiBaoCao(report, values) {
    if (!report?.maBaoCao) return

    if (!coDuThamSoBatBuoc(report, values)) {
      return
    }

    const kichThuoc =
      report?.cauHinhPhanTrang?.kichThuocMacDinh || 20

    const params = { ...values }

    if (report.phanTrang) {
      params.page = 1
      params.pageSize = kichThuoc
    }

    try {
      setLoadingReport(true)
      setError('')

      await taiBaoCao(report.maBaoCao, params)
    } catch (err) {
      setError(
        err.message || 'Không thể tự động tải báo cáo.'
      )
    } finally {
      setLoadingReport(false)
    }
  }

  // =====================================================
  // 4. TẢI DANH SÁCH BÁO CÁO BAN ĐẦU
  // =====================================================

  useEffect(() => {
    const controller = new AbortController()

    async function loadCatalog() {
      try {
        setLoadingCatalog(true)
        setError('')

        const response = await fetchReportCatalog(controller.signal)
        const data = response?.data ?? response

        const danhSach = Array.isArray(data)
          ? data
          : data?.data || data?.reports || []

        setReports(danhSach)

        if (danhSach.length === 0) {
          setError('Hiện chưa có báo cáo nào được kích hoạt.')
          return
        }

        const selected =
          danhSach.find(
            (item) => item.maBaoCao === initialReportCode
          ) || danhSach[0]

        setMaBaoCao(selected.maBaoCao)
        setMetadata(selected)

        // Tạo bộ lọc mặc định cho báo cáo đầu tiên.
        const macDinh = taoThamSoMacDinh(selected)

        // Tự động tải nếu đã có đủ tham số bắt buộc.
        await tuDongTaiBaoCao(selected, macDinh)
      } catch (err) {
        if (err.name === 'AbortError') return

        setError(
          err.message || 'Không thể tải danh sách báo cáo.'
        )
      } finally {
        if (!controller.signal.aborted) {
          setLoadingCatalog(false)
        }
      }
    }

    loadCatalog()

    return () => controller.abort()
  }, [initialReportCode])

  // =====================================================
  // 5. CHỌN BÁO CÁO
  // =====================================================

  function handleReportChange(event) {
    const code = event.target.value
    const selected = layBaoCaoTheoMa(code)

    if (!selected) return

    setMaBaoCao(code)
    setMetadata(selected)
    setError('')

    // Tạo lại bộ lọc mặc định theo báo cáo mới.
    const macDinh = taoThamSoMacDinh(selected)

    // Tự động tải dữ liệu báo cáo mới.
    tuDongTaiBaoCao(selected, macDinh)
  }

  // =====================================================
  // 6. THAY ĐỔI BỘ LỌC
  // =====================================================

  function handleFilterChange(name, value) {
    setThamSo((current) => ({
      ...current,
      [name]: value,
    }))
  }

  // =====================================================
  // 7. CHẠY BÁO CÁO THỦ CÔNG
  // =====================================================

  async function handleRunReport() {
    if (!maBaoCao) {
      setError('Vui lòng chọn báo cáo.')
      return
    }

    if (!kiemTraThamSo()) return

    try {
      setLoadingReport(true)
      setError('')
      setResult(null)

      const params = { ...thamSo }

      if (metadata?.phanTrang) {
        params.page = 1
        params.pageSize = pageSize
      }

      await taiBaoCao(maBaoCao, params)
    } catch (err) {
      setError(err.message || 'Không thể chạy báo cáo.')
    } finally {
      setLoadingReport(false)
    }
  }

  // =====================================================
  // 8. PHÂN TRANG
  // =====================================================

  async function handlePageChange(nextPage) {
    if (
      !metadata?.phanTrang ||
      !result ||
      loadingReport
    ) {
      return
    }

    try {
      setLoadingReport(true)
      setError('')

      await taiBaoCao(maBaoCao, {
        ...thamSo,
        page: nextPage,
        pageSize,
      })
    } catch (err) {
      setError(err.message || 'Không thể tải trang dữ liệu.')
    } finally {
      setLoadingReport(false)
    }
  }

  async function handlePageSizeChange(event) {
    const newPageSize = Number(event.target.value)

    if (!metadata?.phanTrang || loadingReport) return

    setPageSize(newPageSize)

    // Chưa có dữ liệu thì chỉ cập nhật lựa chọn.
    if (!result) return

    try {
      setLoadingReport(true)
      setError('')

      await taiBaoCao(maBaoCao, {
        ...thamSo,
        page: 1,
        pageSize: newPageSize,
      })
    } catch (err) {
      setError(
        err.message || 'Không thể thay đổi số dòng hiển thị.'
      )
    } finally {
      setLoadingReport(false)
    }
  }

  // =====================================================
  // 9. HIỂN THỊ BỘ LỌC THEO METADATA
  // =====================================================

  function renderFilter(item) {
    const value = thamSo[item.ten] ?? ''
    const inputId = `filter-${item.ten}`

    return (
      <div className="generic-filter-item" key={item.ten}>
        <label htmlFor={inputId}>
          {item.nhan || item.ten}
          {item.batBuoc && <span> *</span>}
        </label>

        {item.kieu === 'CHON' ? (
          <select
            id={inputId}
            value={value}
            onChange={(event) =>
              handleFilterChange(item.ten, event.target.value)
            }
          >
            <option value="">-- Tất cả --</option>

            {(item.giaTri || []).map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        ) : (
          <input
            id={inputId}
            type={
              item.kieu === 'NGAY'
                ? 'date'
                : item.kieu === 'SO'
                  ? 'number'
                  : 'text'
            }
            value={value}
            onChange={(event) =>
              handleFilterChange(item.ten, event.target.value)
            }
            placeholder={`Nhập ${item.nhan || item.ten}`}
          />
        )}
      </div>
    )
  }

  // =====================================================
  // 10. THẺ THỐNG KÊ
  // =====================================================

  const summaryCards = result?.cacThe || []

  // =====================================================
  // 11. HIỂN THỊ BẢNG
  // =====================================================

  function renderTable(table, tableName) {
    const columns = table?.cot || []
    const rows = table?.dong || []

    const visibleColumns = columns.filter((column) => {
      const config = layCauHinhCot(tableName, column)
      return config.hienThi !== false
    })

    return (
      <section className="report-section" key={tableName}>
        <h2>{layTenBang(tableName)}</h2>

        {rows.length === 0 ? (
          <p>Không có dữ liệu.</p>
        ) : (
          <div className="generic-table-wrapper">
            <table className="generic-report-table">
              <thead>
                <tr>
                  {visibleColumns.map((column) => {
                    const config = layCauHinhCot(
                      tableName,
                      column
                    )

                    return (
                      <th key={column}>
                        {config.nhan || column}
                      </th>
                    )
                  })}
                </tr>
              </thead>

              <tbody>
                {rows.map((row, rowIndex) => (
                  <tr key={rowIndex}>
                    {visibleColumns.map((column) => {
                      const config = layCauHinhCot(
                        tableName,
                        column
                      )

                      return (
                        <td key={column}>
                          {dinhDangGiaTri(
                            row[column],
                            config.dinhDang
                          )}
                        </td>
                      )
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    )
  }

  // =====================================================
  // 12. XÁC ĐỊNH CÁC BẢNG ĐƯỢC HIỂN THỊ
  // =====================================================

  const visibleTables = useMemo(() => {
    if (!result?.cacBang) return []

    return Object.entries(result.cacBang).filter(
      ([tableName]) => {
        const config =
          result?.metadataBang?.[tableName] ||
          metadata?.cacBang?.[tableName]

        return config?.hienThi !== false
      }
    )
  }, [result, metadata])

  // =====================================================
  // 13. GIAO DIỆN
  // =====================================================

  if (loadingCatalog) {
    return (
      <main className="main-container">
        Đang tải danh sách báo cáo...
      </main>
    )
  }

  return (
    <main className="main-container">
      <div className="report-heading">
        <span className="report-label">REPORT</span>
        <h1>{metadata?.tenBaoCao || 'Báo cáo'}</h1>
        {metadata?.moTa && <p>{metadata.moTa}</p>}
      </div>

      {error && (
        <Alert type="error">
          {error}
        </Alert>
      )}

      <section className="report-section">
        {metadata && (
          <>
            <div className="generic-filter-grid">
              {(metadata.thamSo || []).map(renderFilter)}
            </div>

            <button
              type="button"
              onClick={handleRunReport}
              disabled={loadingReport}
            >
              {loadingReport ? 'Đang tải...' : 'Chạy báo cáo'}
            </button>
          </>
        )}
      </section>

      {/* CÁC THẺ THỐNG KÊ */}
      {result && summaryCards.length > 0 && (
        <section className="generic-summary-grid">
          {summaryCards.map((the) => (
            <div className="generic-summary-card" key={the.ma}>
              <span>{the.nhan}</span>
              <strong>
                {dinhDangGiaTri(the.giaTri, the.dinhDang) || '—'}
              </strong>
            </div>
          ))}
        </section>
      )}

      {/* THANH PHÂN TRANG */}
      {metadata?.phanTrang && (
        <div className="generic-pagination">
          <select
            value={pageSize}
            onChange={handlePageSizeChange}
            disabled={loadingReport}
          >
            <option value={10}>10 dòng</option>
            <option value={20}>20 dòng</option>
            <option value={50}>50 dòng</option>
            <option value={100}>100 dòng</option>
          </select>

          <span>
            {result
              ? `Tổng số dòng: ${result.totalRows ?? 0} | Trang ${result.currentPage ?? 1}/${result.totalPages ?? 1}`
              : loadingReport
                ? 'Đang tải dữ liệu...'
                : 'Chưa có dữ liệu'}
          </span>

          <button
            type="button"
            disabled={
              !result ||
              loadingReport ||
              result.currentPage <= 1
            }
            onClick={() =>
              handlePageChange(result.currentPage - 1)
            }
          >
            Trang trước
          </button>

          <button
            type="button"
            disabled={
              !result ||
              loadingReport ||
              result.currentPage >= result.totalPages
            }
            onClick={() =>
              handlePageChange(result.currentPage + 1)
            }
          >
            Trang sau
          </button>
        </div>
      )}

      {/* CÁC BẢNG HIỂN THỊ THEO METADATA */}
      {loadingReport && !result && (
        <p>Đang tải dữ liệu báo cáo...</p>
      )}

      {visibleTables.map(([tableName, table]) =>
        renderTable(table, tableName)
      )}
    </main>
  )
}

export default GenericReportPage
