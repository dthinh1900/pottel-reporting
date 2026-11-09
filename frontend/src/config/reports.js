// Danh mục báo cáo hiển thị ở trang chủ và menu bên trái.
// Khi có báo cáo mới: thêm một mục ở đây và thêm route trong App.jsx.
export const REPORT_CATALOG = [
  {
    code: 'BC-DS-01',
    title: 'Tổng quan',
    description:
      'Tổng hợp dữ liệu KIT và GOI_MOI, theo dõi kết quả đối soát và thống kê theo ngày.',
    category: 'Tổng quan dữ liệu',
    menuLabel: 'Tổng quan',
    path: '/reports/bc-ds-01',
    status: 'active',
    icon: '▤',
  },
  {
    code: 'BC-DS-02',
    title: 'Chi tiết thuê bao',
    description:
      'Danh sách chi tiết thuê bao KIT và GOI_MOI, hỗ trợ tra cứu và đối soát từng thuê bao.',
    category: 'Tổng quan dữ liệu',
    menuLabel: 'Chi tiết thuê bao',
    path: '/reports/bc-ds-02',
    status: 'active',
    icon: '☷',
  },
  {
    code: 'BC-SL',
    title: 'Báo cáo sản lượng',
    description:
      'Theo dõi số lượng KIT, thuê bao kích hoạt và các luồng CDR.',
    category: 'Tổng quan dữ liệu',
    status: 'pending',
    icon: '▥',
  },
  {
  code: 'BC-DT-01',
  title: 'Tổng hợp doanh thu theo ngày',
  description:
    'Thống kê số thuê bao, doanh thu chưa VAT, tiền VAT và tổng doanh thu theo ngày.',
  category: 'Thống kê doanh thu',
  menuLabel: 'Tổng hợp doanh thu theo ngày',
  path: '/reports/bc-dt-01',
  status: 'active',
  icon: '₫',
  },
  {
    code: 'BC-DT-02',
    title: 'Chi tiết doanh thu theo thuê bao',
    description:
      'Tra cứu doanh thu phân bổ của từng thuê bao trong ngày được chọn.',
    category: 'Thống kê doanh thu',
    menuLabel: 'Chi tiết doanh thu theo thuê bao',
    path: '/reports/bc-dt-02',
    status: 'active',
    icon: '₫',
  },
] 