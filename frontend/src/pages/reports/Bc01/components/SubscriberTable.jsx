import { formatDate, formatNumber } from '../../../../utils/format'
import Pagination from '../../../../components/common/Pagination'

// Màu badge cho tình trạng đối soát:
// xanh = Khớp KIT, đỏ = Không tìm thấy KIT, vàng = các trạng thái còn lại
function reconciliationBadge(status) {
  if (status === 'Khớp KIT') return 'badge badge-success'
  if (status === 'Không tìm thấy KIT') return 'badge badge-danger'
  return 'badge badge-warning'
}

// Bảng 1C - Chi tiết thuê bao
// rowOffset: số dòng đứng trước trang hiện tại, dùng để đánh STT
function SubscriberTable({ rows, rowOffset, loading, pagination }) {
  const empty = rows.length === 0

  return (
    <section className="table-panel">
      <div className="table-title">
        <strong>Bảng 1C - Chi tiết thuê bao</strong>
        <p>Mỗi MSISDN được hiển thị một dòng, tổng hợp từ KIT và GOI_MOI.</p>
      </div>

      <div className={`table-scroll ${loading && !empty ? 'is-loading' : ''}`}>
        <table>
          <thead>
            <tr>
              <th>STT</th>
              <th>MSISDN</th>
              <th>TRẠNG THÁI KIT</th>
              <th>NGÀY ĐĂNG KÝ</th>
              <th>SỐ DÒNG KIT</th>
              <th>SỐ SỰ KIỆN GOI_MOI</th>
              <th>NGÀY GOI_MOI ĐẦU TIÊN</th>
              <th>NGÀY GOI_MOI CUỐI CÙNG</th>
              <th>TÌNH TRẠNG ĐỐI SOÁT</th>
            </tr>
          </thead>

          <tbody>
            {empty ? (
              <tr>
                <td colSpan="9" className="empty-row">
                  {loading
                    ? 'Đang tải dữ liệu...'
                    : 'Không có dữ liệu phù hợp với điều kiện lọc.'}
                </td>
              </tr>
            ) : (
              rows.map((row, index) => (
                <tr key={`${row.msisdn}-${index}`}>
                  <td>{rowOffset + index + 1}</td>
                  <td><strong>{row.msisdn}</strong></td>
                  <td>{row.kit_status || '-'}</td>
                  <td>
                    {row.registed_date ? formatDate(row.registed_date) : 'Thiếu'}
                  </td>
                  <td>{formatNumber(row.kit_row_count)}</td>
                  <td>{formatNumber(row.event_count)}</td>
                  <td>
                    {row.first_event_date ? formatDate(row.first_event_date) : '-'}
                  </td>
                  <td>
                    {row.last_event_date ? formatDate(row.last_event_date) : '-'}
                  </td>
                  <td>
                    <span className={reconciliationBadge(row.reconciliation_status)}>
                      {row.reconciliation_status || '-'}
                    </span>
                  </td>
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

export default SubscriberTable
