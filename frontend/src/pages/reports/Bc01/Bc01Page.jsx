import { useEffect, useRef, useState } from 'react'
import './Bc01.css'
import Alert from '../../../components/common/Alert'
import { fetchBcDs01 } from '../../../services/reportService'
import { PAGE_SIZE_OPTIONS } from '../../../components/common/Pagination'
import { replaceUrl } from '../../../router'
import { validateDateRange } from '../../../utils/format'
import SummaryCards from './components/SummaryCards'
import ReportFilters, { EMPTY_FILTERS } from './components/ReportFilters'
import DailyTable from './components/DailyTable'
import SubscriberTable from './components/SubscriberTable'

const REPORT_PATH = '/reports/bc-ds-01'
const DEFAULT_PAGE_SIZE = 20

// Đọc bộ lọc từ URL (giống Thymeleaf: ?bc=1C&fromDate=...&page=2...)
function readStateFromUrl() {
  const params = new URLSearchParams(window.location.search)
  const pageSize = Number(params.get('pageSize'))
  const page = Number(params.get('page'))

  return {
    bc: params.get('bc') === '1C' ? '1C' : '1B',
    page: Number.isInteger(page) && page >= 1 ? page : 1,
    pageSize: PAGE_SIZE_OPTIONS.includes(pageSize)
      ? pageSize
      : DEFAULT_PAGE_SIZE,
    filters: {
      fromDate: params.get('fromDate') || '',
      toDate: params.get('toDate') || '',
      msisdn: params.get('msisdn') || '',
      kitStatus: params.get('kitStatus') || '',
      reconciliationStatus: params.get('reconciliationStatus') || '',
      goiMoiStatus: params.get('goiMoiStatus') || '',
    },
  }
}

// Ghi bộ lọc lên URL để có thể tải lại trang hoặc chia sẻ đường dẫn
function buildUrl({ bc, page, pageSize, filters }) {
  const params = new URLSearchParams({
    bc,
    page: String(page),
    pageSize: String(pageSize),
  })

  Object.entries(filters).forEach(([name, value]) => {
    if (value) params.set(name, value)
  })

  return `${REPORT_PATH}?${params.toString()}`
}

function Bc01Page() {
  const [initial] = useState(readStateFromUrl)

  const [bc, setBc] = useState(initial.bc)
  const [filters, setFilters] = useState(initial.filters)
  const [page, setPage] = useState(initial.page)
  const [pageSize, setPageSize] = useState(initial.pageSize)

  const [report, setReport] = useState(null) // Dữ liệu bảng của lần tải gần nhất
  const [meta, setMeta] = useState(null) // Bảng 1A và danh sách lọc (tải một lần)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  const metaLoadedRef = useRef(false)

  // Đồng bộ trạng thái lên URL
  useEffect(() => {
    replaceUrl(buildUrl({ bc, page, pageSize, filters }))
  }, [bc, page, pageSize, filters])

  // Tải dữ liệu từ Backend mỗi khi đổi tab, bộ lọc, trang hoặc số dòng
  useEffect(() => {
    const controller = new AbortController()

    async function load() {
      const dateError = validateDateRange(filters.fromDate, filters.toDate)

      if (dateError) {
        setError(dateError)
        setLoading(false)
        return
      }

      setLoading(true)
      setError('')

      try {
        const result = await fetchBcDs01(
          {
            bc,
            ...filters,
            page,
            pageSize,
            includeMeta: !metaLoadedRef.current,
          },
          controller.signal
        )

        if (result.summary) {
          metaLoadedRef.current = true
          setMeta({
            summary: result.summary,
            kitStatuses: result.kitStatuses || [],
            reconciliationStatuses: result.reconciliationStatuses || [],
          })
        }

        setReport(result)
      } catch (err) {
        if (err.name === 'AbortError') return

        setError(err.message || 'Không thể tải dữ liệu báo cáo.')
      } finally {
        if (!controller.signal.aborted) setLoading(false)
      }
    }

    load()

    return () => controller.abort()
  }, [bc, filters, page, pageSize, reloadKey])

  function handleTabChange(nextBc) {
    if (nextBc === bc) return
    setBc(nextBc)
    setPage(1)
  }

  function handleApplyFilters(nextFilters) {
    setFilters(nextFilters)
    setPage(1)
  }

  function handleResetFilters() {
    setFilters(EMPTY_FILTERS)
    setPage(1)
    setPageSize(DEFAULT_PAGE_SIZE)
  }

  function handlePageSizeChange(size) {
    setPageSize(size)
    setPage(1)
  }

  // Chỉ hiển thị dòng dữ liệu nếu đúng loại bảng đang chọn
  const sameTable = report?.selectedBc === bc
  const rows1B = sameTable ? report.dailySummary || [] : []
  const rows1C = sameTable ? report.subscriberDetails || [] : []

  const pagination = {
    page,
    pageSize,
    totalRows: sameTable ? report.totalRows : 0,
    totalPages: sameTable ? report.totalPages : 1,
    onPageChange: setPage,
    onPageSizeChange: handlePageSizeChange,
  }

  const rowOffset = sameTable
    ? (report.currentPage - 1) * report.pageSize
    : 0

  return (
    <main className="main-container">
      <div className="report-heading">
        <span className="report-label">REPORT / BC-DS-01</span>
        <h1>Đối soát KIT và GOI_MOI</h1>
        <p>Tổng quan dữ liệu KIT và sự kiện GOI_MOI từ PostgreSQL.</p>
      </div>

      <div className="notice">
        <span>ⓘ</span>
        Đây là số liệu truy vấn trực tiếp từ dữ liệu nguồn. Chưa phải kết quả
        nghiệm thu đối soát tài chính.
      </div>

      <section className="overview">
        <h2>Tổng quan dữ liệu</h2>
        <p>Các chỉ số tổng hợp từ dữ liệu nguồn</p>

        {meta ? (
          <SummaryCards summary={meta.summary} />
        ) : (
          <p>
            {loading
              ? 'Đang tải số liệu thống kê...'
              : 'Chưa có dữ liệu thống kê.'}
          </p>
        )}
      </section>

      <section className="report-section">
        <div className="section-heading">
          <div>
            <h2>Chi tiết báo cáo</h2>
            <p>Lựa chọn loại báo cáo và điều kiện tra cứu</p>
          </div>

          <div className="tabs">
            <button
              type="button"
              className={bc === '1B' ? 'active' : ''}
              onClick={() => handleTabChange('1B')}
            >
              BC 1B – Theo ngày
            </button>

            <button
              type="button"
              className={bc === '1C' ? 'active' : ''}
              onClick={() => handleTabChange('1C')}
            >
              BC 1C – Chi tiết thuê bao
            </button>
          </div>
        </div>

        <ReportFilters
          bc={bc}
          appliedFilters={filters}
          kitStatuses={meta?.kitStatuses || []}
          reconciliationStatuses={meta?.reconciliationStatuses || []}
          loading={loading}
          onApply={handleApplyFilters}
          onReset={handleResetFilters}
        />

        {error && (
          <Alert
            type="error"
            actions={
              <>
                {page > 1 && (
                  <button type="button" onClick={() => setPage(1)}>
                    Về trang đầu
                  </button>
                )}
                <button
                  type="button"
                  onClick={() => setReloadKey((key) => key + 1)}
                >
                  Thử lại
                </button>
              </>
            }
          >
            {error}
          </Alert>
        )}

        {bc === '1B' ? (
          <DailyTable
            rows={rows1B}
            loading={loading}
            pagination={pagination}
          />
        ) : (
          <SubscriberTable
            rows={rows1C}
            rowOffset={rowOffset}
            loading={loading}
            pagination={pagination}
          />
        )}
      </section>
    </main>
  )
}

export default Bc01Page
