import { useEffect } from 'react'

// Hộp thoại xác nhận (thay cho confirm() của trình duyệt)
function ConfirmDialog({
  title,
  message,
  confirmLabel = 'Xác nhận',
  danger = false,
  busy = false,
  onConfirm,
  onCancel,
}) {
  // Nhấn Esc để hủy
  useEffect(() => {
    function handleKey(event) {
      if (event.key === 'Escape' && !busy) onCancel()
    }

    window.addEventListener('keydown', handleKey)
    return () => window.removeEventListener('keydown', handleKey)
  }, [busy, onCancel])

  return (
    <div className="dialog-backdrop" onClick={busy ? undefined : onCancel}>
      <div
        className="dialog"
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="dialog-title"
        onClick={(event) => event.stopPropagation()}
      >
        <h3 id="dialog-title">{title}</h3>
        <p>{message}</p>

        <div className="dialog-actions">
          <button
            type="button"
            className="secondary-button"
            disabled={busy}
            onClick={onCancel}
          >
            Hủy
          </button>

          <button
            type="button"
            className={danger ? 'danger-button' : 'primary-button'}
            disabled={busy}
            onClick={onConfirm}
          >
            {busy ? 'Đang xử lý...' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}

export default ConfirmDialog
