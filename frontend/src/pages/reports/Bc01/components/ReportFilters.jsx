import { useState } from 'react'
import { todayInVietnam, validateDateRange } from '../../../../utils/format'

export const EMPTY_FILTERS = {
  fromDate: '',
  toDate: '',
  msisdn: '',
  kitStatus: '',
  reconciliationStatus: '',
  goiMoiStatus: '',
}

// Bộ lọc báo cáo. Bảng 1B chỉ lọc theo ngày, bảng 1C có thêm 4 bộ lọc.
// Dữ liệu chỉ được áp dụng khi bấm "Lọc dữ liệu".
function ReportFilters({
  bc,
  appliedFilters,
  kitStatuses,
  reconciliationStatuses,
  loading,
  onApply,
  onReset,
}) {
  const [draft, setDraft] = useState(appliedFilters)

  const is1C = bc === '1C'
  const today = todayInVietnam()
  const dateError = validateDateRange(draft.fromDate, draft.toDate)

  function update(name, value) {
    setDraft((current) => ({ ...current, [name]: value }))
  }

  function handleSubmit(event) {
    event.preventDefault()

    if (dateError) return

    onApply({
      ...draft,
      msisdn: draft.msisdn.trim(),
    })
  }

  function handleReset() {
    setDraft(EMPTY_FILTERS)
    onReset()
  }

  return (
    <section className="filter-panel">
      <div className="panel-heading">
        <span className="panel-icon">▤</span>
        Bộ lọc dữ liệu
      </div>

      <form className="filter-content" onSubmit={handleSubmit}>
        {dateError && (
          <p className="field-error" role="alert">
            {dateError}
          </p>
        )}

        <div className="filter-fields">
          <label>
            Từ ngày GOI_MOI
            <input
              type="date"
              value={draft.fromDate}
              max={today}
              className={dateError ? 'is-invalid' : ''}
              onChange={(event) => update('fromDate', event.target.value)}
            />
          </label>

          <label>
            Đến ngày GOI_MOI
            <input
              type="date"
              value={draft.toDate}
              max={today}
              className={dateError ? 'is-invalid' : ''}
              onChange={(event) => update('toDate', event.target.value)}
            />
          </label>

          {is1C && (
            <>
              <label>
                MSISDN
                <input
                  type="text"
                  value={draft.msisdn}
                  maxLength={30}
                  placeholder="Nhập MSISDN"
                  onChange={(event) => update('msisdn', event.target.value)}
                />
              </label>

              <label>
                Trạng thái KIT
                <select
                  value={draft.kitStatus}
                  onChange={(event) => update('kitStatus', event.target.value)}
                >
                  <option value="">Tất cả trạng thái</option>
                  {kitStatuses.map((status) => (
                    <option key={status} value={status}>
                      {status}
                    </option>
                  ))}
                </select>
              </label>

              <label>
                Tình trạng GOI_MOI
                <select
                  value={draft.goiMoiStatus}
                  onChange={(event) =>
                    update('goiMoiStatus', event.target.value)
                  }
                >
                  <option value="">Tất cả</option>
                  <option value="HAS">Đã có GOI_MOI</option>
                  <option value="NONE">Chưa có GOI_MOI</option>
                </select>
              </label>

              <label>
                Tình trạng đối soát
                <select
                  value={draft.reconciliationStatus}
                  onChange={(event) =>
                    update('reconciliationStatus', event.target.value)
                  }
                >
                  <option value="">Tất cả tình trạng</option>
                  {reconciliationStatuses.map((status) => (
                    <option key={status} value={status}>
                      {status}
                    </option>
                  ))}
                </select>
              </label>
            </>
          )}
        </div>

        <div className="filter-actions">
          <button
            type="submit"
            className="primary-button"
            disabled={loading || Boolean(dateError)}
          >
            ⌕ Lọc dữ liệu
          </button>

          <button
            type="button"
            className="secondary-button"
            disabled={loading}
            onClick={handleReset}
          >
            ↻ Đặt lại
          </button>
        </div>
      </form>
    </section>
  )
}

export default ReportFilters
