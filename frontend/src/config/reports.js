// Danh mục báo cáo hiển thị ở trang chủ và menu bên trái.
// Khi có báo cáo mới: thêm một mục ở đây và thêm route trong App.jsx.
export const REPORT_CATALOG = [
  {
    code: 'BC-DS-01',
    title: 'Tổng quan',
    description:
      'Tổng hợp dữ liệu KIT và GOI_MOI, theo dõi kết quả đối soát và thống kê theo ngày.',
    category: 'Báo cáo đối soát',
    menuLabel: 'Tổng quan',
    path: '/reports/bc-ds-01',
    status: 'active',
    icon: '▤',
  },
  {
    code: 'BC-DS-01-DETAIL',
    title: 'Chi tiết thuê bao',
    description:
      'Danh sách chi tiết thuê bao KIT và GOI_MOI, hỗ trợ tra cứu và đối soát từng thuê bao.',
    category: 'Báo cáo đối soát',
    menuLabel: 'Chi tiết thuê bao',
    path: '/reports/bc-ds-01-detail',
    status: 'active',
    icon: '☷',
  },
  {
    code: 'BC-SL',
    title: 'Báo cáo sản lượng',
    description:
      'Theo dõi số lượng KIT, thuê bao kích hoạt và các luồng CDR.',
    category: 'Báo cáo sản lượng',
    status: 'pending',
    icon: '▥',
  },
  {
    code: 'BC-DT',
    title: 'Báo cáo doanh thu',
    description: 'Tổng hợp doanh thu và phân tích hiệu quả kinh doanh.',
    category: 'Báo cáo doanh thu',
    status: 'pending',
    icon: '₫',
  },
]