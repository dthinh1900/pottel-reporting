import { formatNumber } from '../../utils/format'

export const PAGE_SIZE_OPTIONS = [10, 20, 50, 100]

// Thanh phân trang dùng chung cho các bảng báo cáo
function Pagination({
  page,
  pageSize,
  totalRows,
  totalPages,
  loading,
  onPageChange,
  onPageSizeChange,
}) {
  const startRecord = totalRows === 0 ? 0 : (page - 1) * pageSize + 1
  const endRecord = Math.min(page * pageSize, totalRows)
  const lastPage = Math.max(1, totalPages)

  return (
    <div className="pagination">
      <div className="pagination-info">
        <span>
          Hiển thị {formatNumber(startRecord)} - {formatNumber(endRecord)}
          {' '}trong tổng số <strong>{formatNumber(totalRows)}</strong> bản ghi
        </span>

        <label>
          Số dòng/trang:
          <select
            value={pageSize}
            disabled={loading}
            onChange={(event) =>
              onPageSizeChange(Number(event.target.value))
            }
          >
            {PAGE_SIZE_OPTIONS.map((size) => (
              <option key={size} value={size}>
                {size}
              </option>
            ))}
          </select>
        </label>
      </div>

      <div className="page-controls">
        <button
          type="button"
          disabled={loading || page <= 1}
          onClick={() => onPageChange(1)}
          aria-label="Trang đầu"
        >
          «
        </button>

        <button
          type="button"
          disabled={loading || page <= 1}
          onClick={() => onPageChange(page - 1)}
        >
          Trước
        </button>

        <span>
          Trang <strong>{page}</strong> / <strong>{lastPage}</strong>
        </span>

        <button
          type="button"
          disabled={loading || page >= lastPage}
          onClick={() => onPageChange(page + 1)}
        >
          Sau
        </button>

        <button
          type="button"
          disabled={loading || page >= lastPage}
          onClick={() => onPageChange(lastPage)}
          aria-label="Trang cuối"
        >
          »
        </button>
      </div>
    </div>
  )
}

export default Pagination
