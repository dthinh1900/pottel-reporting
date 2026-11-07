// Danh mục báo cáo hiển thị ở trang chủ và menu bên trái.
// Khi có báo cáo mới: thêm một mục ở đây (status: 'active') và thêm route trong App.jsx.
export const REPORT_CATALOG = [
  {
    code: 'BC-DS-01',
    title: 'Đối soát dữ liệu',
    description:
      'Kiểm tra dữ liệu KIT và GOI_MOI, phát hiện các trường hợp bất thường.',
    category: 'Báo cáo đối soát',
    menuLabel: 'Đối soát KIT và GOI_MOI',
    path: '/reports/bc-ds-01',
    status: 'active',
    icon: '⇄',
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
