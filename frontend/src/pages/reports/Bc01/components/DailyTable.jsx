import { formatDate, formatNumber } from '../../../../utils/format'
import Pagination from '../../../../components/common/Pagination'

// Bảng 1B - Tổng hợp GOI_MOI theo ngày
function DailyTable({ rows, loading, pagination }) {
  const empty = rows.length === 0

  return (
    <section className="table-panel">
      <div className="table-title">
        <strong>Bảng 1B - Tổng hợp GOI_MOI theo ngày</strong>
        <p>Số liệu được tổng hợp theo ngày ghi nhận GOI_MOI trên CDR.</p>
      </div>

      <div className={`table-scroll ${loading && !empty ? 'is-loading' : ''}`}>
        <table>
          <thead>
            <tr>
              <th>NGÀY GHI NHẬN</th>
              <th>SỰ KIỆN GOI_MOI</th>
              <th>MSISDN DUY NHẤT</th>
              <th>SỰ KIỆN KHỚP KIT</th>
              <th>SỰ KIỆN KHÔNG TÌM THẤY KIT</th>
              <th>SỰ KIỆN THIẾU NGÀY ĐĂNG KÝ</th>
            </tr>
          </thead>

          <tbody>
            {empty ? (
              <tr>
                <td colSpan="6" className="empty-row">
                  {loading
                    ? 'Đang tải dữ liệu...'
                    : 'Không có dữ liệu phù hợp với điều kiện lọc.'}
                </td>
              </tr>
            ) : (
              rows.map((row, index) => (
                <tr key={`${row.event_date}-${index}`}>
                  <td>{formatDate(row.event_date)}</td>
                  <td>{formatNumber(row.event_count)}</td>
                  <td>{formatNumber(row.unique_msisdn)}</td>
                  <td>{formatNumber(row.matched_events)}</td>
                  <td>{formatNumber(row.orphan_events)}</td>
                  <td>{formatNumber(row.missing_registed_date_events)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <Pagination loading={loading} {...pagination} />
    </section>
  )
}

export default DailyTable
