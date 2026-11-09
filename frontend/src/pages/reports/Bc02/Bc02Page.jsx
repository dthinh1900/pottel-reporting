import { useEffect, useRef, useState } from 'react'
import '../Bc01/Bc01.css'
import Alert from '../../../components/common/Alert'
import { fetchBcDs01 } from '../../../services/reportService'
import { PAGE_SIZE_OPTIONS } from '../../../components/common/Pagination'
import { replaceUrl } from '../../../router'
import { validateDateRange } from '../../../utils/format'
import ReportFilters, { EMPTY_FILTERS } from '../Bc01/components/ReportFilters'
import SubscriberTable from '../Bc01/components/SubscriberTable'

const REPORT_PATH = '/reports/bc-ds-02'
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

function Bc01DetailPage() {
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

  // Tải riêng dữ liệu bảng 1C.
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
            bc: '1C',
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

  const sameTable = report?.selectedBc === '1C'
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
        <span className="report-label">
          REPORT / BC-DS-02
        </span>
        <h1>Chi tiết thuê bao</h1>
        <p>
          Tra cứu và đối soát thông tin KIT và GOI_MOI theo từng thuê bao.
        </p>
      </div>

      <div className="notice">
        <span>ⓘ</span>
        Dữ liệu chi tiết được truy vấn trực tiếp từ PostgreSQL.
      </div>

      <section className="report-section">
        <h2>Chi tiết thuê bao</h2>
        <p>Tra cứu và đối soát thông tin từng thuê bao</p>

        <ReportFilters
          bc="1C"
          appliedFilters={filters}
          kitStatuses={meta?.kitStatuses || []}
          reconciliationStatuses={
            meta?.reconciliationStatuses || []
          }
          loading={loading}
          onApply={handleApplyFilters}
          onReset={handleResetFilters}
        />

        <SubscriberTable
          rows={rows1C}
          rowOffset={rowOffset}
          loading={loading}
          pagination={pagination}
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

export default Bc01DetailPage