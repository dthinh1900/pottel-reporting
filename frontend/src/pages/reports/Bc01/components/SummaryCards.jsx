import { formatNumber } from '../../../../utils/format'

// Cấu hình 8 thẻ thống kê (bảng 1A). Giá trị lấy từ Backend.
const SUMMARY_CONFIG = [
  { key: 'total_kit', label: 'Tổng KIT', color: 'blue', icon: 'K' },
  { key: 'unique_kit_msisdn', label: 'MSISDN KIT duy nhất', color: 'purple', icon: 'M' },
  { key: 'total_goi_moi', label: 'Sự kiện GOI_MOI', color: 'green', icon: 'G' },
  { key: 'unique_goi_moi_msisdn', label: 'MSISDN GOI_MOI duy nhất', color: 'cyan', icon: 'U' },
  { key: 'matched_msisdn', label: 'Khớp KIT', color: 'green', icon: '✓' },
  { key: 'orphan_msisdn', label: 'Không tìm thấy KIT', color: 'red', icon: '!' },
  { key: 'missing_registed_date', label: 'Thiếu ngày đăng ký', color: 'orange', icon: 'Đ' },
  { key: 'repeated_goi_moi_msisdn', label: 'MSISDN có nhiều GOI_MOI', color: 'pink', icon: '↻' },
]

function SummaryCards({ summary }) {
  return (
    <div className="summary-grid">
      {SUMMARY_CONFIG.map((item) => (
        <div className={`summary-card ${item.color}`} key={item.key}>
          <div className="card-top">
            <span>{item.label}</span>
            <span className="card-icon">{item.icon}</span>
          </div>

          <strong>{formatNumber(summary?.[item.key])}</strong>
        </div>
      ))}
    </div>
  )
}

export default SummaryCards
