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
import Pagination from '../../../components/common/Pagination'

const REPORT_PATH = '/reports/bc-ds-01'
const DEFAULT_PAGE_SIZE = 20

function readStateFromUrl() {
  const params = new URLSearchParams(window.location.search)
  const pageSize = Number(params.get('pageSize'))
  const page = Number(params.get('page'))

  return {
    page: Number.isInteger(page) && page >= 1 ? page : 1,
    pageSize: PAGE_SIZE_OPTIONS.includes(pageSize)
      ? pageSize
      : DEFAULT_PAGE_SIZE,
    filters: {
      fromDate: params.get('fromDate') || '',
      toDate: params.get('toDate') || '',
      msisdn: params.get('msisdn') || '',
      kitStatus: params.get('kitStatus') || '',
      reconciliationStatus:
        params.get('reconciliationStatus') || '',
      goiMoiStatus: params.get('goiMoiStatus') || '',
    },
  }
}

function buildUrl({ page, pageSize, filters }) {
  const params = new URLSearchParams({
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

  const [filters, setFilters] = useState(initial.filters)
  const [page, setPage] = useState(initial.page)
  const [pageSize, setPageSize] = useState(initial.pageSize)

  const [report, setReport] = useState(null)
  const [meta, setMeta] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [reloadKey, setReloadKey] = useState(0)

  const metaLoadedRef = useRef(false)

  // Đồng bộ bộ lọc và phân trang lên URL.
  useEffect(() => {
    replaceUrl(buildUrl({ page, pageSize, filters }))
  }, [page, pageSize, filters])

  // Tải dữ liệu bảng 1B và thông tin tổng quan 1A.
  useEffect(() => {
    const controller = new AbortController()

    async function load() {
      const dateError = validateDateRange(
        filters.fromDate,
        filters.toDate
      )

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
            bc: '1B',
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
            reconciliationStatuses:
              result.reconciliationStatuses || [],
          })
        }

        setReport(result)
      } catch (err) {
        if (err.name === 'AbortError') return

        setError(err.message || 'Không thể tải dữ liệu báo cáo.')
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    load()

    return () => controller.abort()
  }, [filters, page, pageSize, reloadKey])

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

  const sameTable = report?.selectedBc === '1B'
  const rows1B = sameTable ? report.dailySummary || [] : []

  const pagination = {
    page,
    pageSize,
    totalRows: sameTable ? report.totalRows : 0,
    totalPages: sameTable ? report.totalPages : 1,
    onPageChange: setPage,
    onPageSizeChange: handlePageSizeChange,
  }

  return (
    <main className="main-container">
      <div className="report-heading">
        <span className="report-label">REPORT / BC-DS-01</span>
        <h1>Tổng quan</h1>
        <p>
          Tổng hợp dữ liệu KIT và GOI_MOI, theo dõi kết quả đối soát
          và thống kê theo ngày.
        </p>
      </div>

      <div className="notice">
        <span>ⓘ</span>
        Đây là số liệu truy vấn trực tiếp từ dữ liệu nguồn. Chưa phải
        kết quả nghiệm thu đối soát tài chính.
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
        <h2>Tổng hợp GOI_MOI theo ngày</h2>
        <p>Thống kê sự kiện theo từng ngày</p>

        <ReportFilters
          bc="1B"
          appliedFilters={filters}
          kitStatuses={meta?.kitStatuses || []}
          reconciliationStatuses={
            meta?.reconciliationStatuses || []
          }
          loading={loading}
          onApply={handleApplyFilters}
          onReset={handleResetFilters}
        />

        <Pagination loading={loading} {...pagination} />

        <DailyTable
          rows={rows1B}
          loading={loading}
        />
      </section>

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
                onClick={() => setReloadKey(key => key + 1)}
              >
                Thử lại
              </button>
            </>
          }
        >
          {error}
        </Alert>
      )}
    </main>
  )
}

export default Bc01Page