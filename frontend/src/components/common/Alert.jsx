// Thông báo lỗi / thành công. Có thể kèm nút hành động và nút đóng.
function Alert({ type = 'error', children, actions, onClose }) {
  return (
    <div
      className={`alert alert-${type}`}
      role={type === 'error' ? 'alert' : 'status'}
    >
      <span aria-hidden="true">{type === 'error' ? '⚠' : '✓'}</span>
      <span className="alert-message">{children}</span>

      {actions}

      {onClose && (
        <button
          type="button"
          className="alert-close"
          aria-label="Đóng thông báo"
          onClick={onClose}
        >
          ×
        </button>
      )}
    </div>
  )
}

export default Alert
