-- =========================================================
-- BC-DS-02: CHI TIẾT THUÊ BAO KIT VÀ GOI_MOI
--
-- 1C: Chi tiết thuê bao       8 tham số có tên
-- 1D: Đếm tổng số dòng        6 tham số có tên
--
-- Các tham số lọc:
-- :tuNgay
-- :denNgay
-- :msisdn
-- :trangThaiKit
-- :trangThaiDoiSoat
-- :tinhTrangGoiMoi
--
-- Phân trang bảng 1C:
-- :limit
-- :offset
-- =========================================================


-- @table 1C

WITH kit AS (
    SELECT
        NULLIF(BTRIM(msisdn_snapshot), '') AS msisdn,
        STRING_AGG(DISTINCT status, ', ') AS kit_status,
        MAX(registed_date) AS registed_date,
        COUNT(*) AS kit_row_count
    FROM crm.linhnn_kits_prod
    WHERE NULLIF(BTRIM(msisdn_snapshot), '') IS NOT NULL
    GROUP BY NULLIF(BTRIM(msisdn_snapshot), '')
),
goi_moi AS (
    SELECT
        NULLIF(BTRIM(msisdn), '') AS msisdn,
        COUNT(*) AS event_count,
        MIN(TO_DATE(TRIM(insert_time), 'DD-MON-YY')) AS first_event_date,
        MAX(TO_DATE(TRIM(insert_time), 'DD-MON-YY')) AS last_event_date
    FROM cdr.vnpt_package_new
    WHERE NULLIF(BTRIM(msisdn), '') IS NOT NULL
      AND insert_time IS NOT NULL
      AND TRIM(insert_time) <> ''
    GROUP BY NULLIF(BTRIM(msisdn), '')
),
details AS (
    SELECT
        COALESCE(k.msisdn, g.msisdn) AS msisdn,
        k.kit_status,
        k.registed_date,
        COALESCE(k.kit_row_count, 0) AS kit_row_count,
        COALESCE(g.event_count, 0) AS event_count,
        g.first_event_date,
        g.last_event_date,
        CASE
            WHEN k.msisdn IS NULL THEN 'Không tìm thấy KIT'
            WHEN g.msisdn IS NULL THEN 'Chỉ có KIT'
            WHEN k.registed_date IS NULL THEN 'Khớp KIT - thiếu ngày đăng ký'
            ELSE 'Khớp KIT'
        END AS reconciliation_status
    FROM kit k
    FULL OUTER JOIN goi_moi g
        ON k.msisdn = g.msisdn
),
filtered AS (
    SELECT *
    FROM details
    WHERE
        -- 0. Lọc theo ngày đăng ký KIT
        (
            CAST(:tuNgay AS DATE) IS NULL
            OR registed_date::date >= CAST(:tuNgay AS DATE)
        )
        AND (
            CAST(:denNgay AS DATE) IS NULL
            OR registed_date::date <= CAST(:denNgay AS DATE)
        )

        -- 1. Lọc MSISDN
        AND (
            CAST(:msisdn AS TEXT) IS NULL
            OR msisdn LIKE CAST(:msisdn AS TEXT)
        )

        -- 2. Lọc trạng thái KIT
        AND (
            CAST(:trangThaiKit AS TEXT) IS NULL
            OR EXISTS (
                SELECT 1
                FROM crm.linhnn_kits_prod kf
                WHERE NULLIF(BTRIM(kf.msisdn_snapshot), '') = details.msisdn
                  AND BTRIM(kf.status) = CAST(:trangThaiKit AS TEXT)
            )
        )

        -- 3. Lọc trạng thái đối soát
        AND (
            CAST(:trangThaiDoiSoat AS TEXT) IS NULL
            OR reconciliation_status = CAST(:trangThaiDoiSoat AS TEXT)
        )

        -- 4. Lọc tình trạng GOI_MOI
        AND (
            CAST(:tinhTrangGoiMoi AS TEXT) IS NULL
            OR (
                CAST(:tinhTrangGoiMoi AS TEXT) = 'HAS'
                AND event_count > 0
            )
            OR (
                CAST(:tinhTrangGoiMoi AS TEXT) = 'NONE'
                AND event_count = 0
                AND kit_row_count > 0
            )
        )
)
SELECT *
FROM filtered
ORDER BY registed_date ASC NULLS LAST, msisdn ASC
LIMIT CAST(:limit AS INTEGER)
OFFSET CAST(:offset AS INTEGER);


-- =========================================================
-- 1D: ĐẾM TỔNG SỐ DÒNG
-- 6 tham số lọc, giống bảng 1C
-- =========================================================

-- @table 1D

WITH kit AS (
    SELECT
        NULLIF(BTRIM(msisdn_snapshot), '') AS msisdn,
        STRING_AGG(DISTINCT status, ', ') AS kit_status,
        MAX(registed_date) AS registed_date,
        COUNT(*) AS kit_row_count
    FROM crm.linhnn_kits_prod
    WHERE NULLIF(BTRIM(msisdn_snapshot), '') IS NOT NULL
    GROUP BY NULLIF(BTRIM(msisdn_snapshot), '')
),
goi_moi AS (
    SELECT
        NULLIF(BTRIM(msisdn), '') AS msisdn,
        COUNT(*) AS event_count,
        MIN(TO_DATE(TRIM(insert_time), 'DD-MON-YY')) AS first_event_date,
        MAX(TO_DATE(TRIM(insert_time), 'DD-MON-YY')) AS last_event_date
    FROM cdr.vnpt_package_new
    WHERE NULLIF(BTRIM(msisdn), '') IS NOT NULL
      AND insert_time IS NOT NULL
      AND TRIM(insert_time) <> ''
    GROUP BY NULLIF(BTRIM(msisdn), '')
),
details AS (
    SELECT
        COALESCE(k.msisdn, g.msisdn) AS msisdn,
        k.kit_status,
        k.registed_date,
        COALESCE(k.kit_row_count, 0) AS kit_row_count,
        COALESCE(g.event_count, 0) AS event_count,
        g.first_event_date,
        g.last_event_date,
        CASE
            WHEN k.msisdn IS NULL THEN 'Không tìm thấy KIT'
            WHEN g.msisdn IS NULL THEN 'Chỉ có KIT'
            WHEN k.registed_date IS NULL THEN 'Khớp KIT - thiếu ngày đăng ký'
            ELSE 'Khớp KIT'
        END AS reconciliation_status
    FROM kit k
    FULL OUTER JOIN goi_moi g
        ON k.msisdn = g.msisdn
),
filtered AS (
    SELECT *
    FROM details
    WHERE
        -- 0. Lọc theo ngày đăng ký KIT
        (
            CAST(:tuNgay AS DATE) IS NULL
            OR registed_date::date >= CAST(:tuNgay AS DATE)
        )
        AND (
            CAST(:denNgay AS DATE) IS NULL
            OR registed_date::date <= CAST(:denNgay AS DATE)
        )

        -- 1. Lọc MSISDN
        AND (
            CAST(:msisdn AS TEXT) IS NULL
            OR msisdn LIKE CAST(:msisdn AS TEXT)
        )

        -- 2. Lọc trạng thái KIT
        AND (
            CAST(:trangThaiKit AS TEXT) IS NULL
            OR EXISTS (
                SELECT 1
                FROM crm.linhnn_kits_prod kf
                WHERE NULLIF(BTRIM(kf.msisdn_snapshot), '') = details.msisdn
                  AND BTRIM(kf.status) = CAST(:trangThaiKit AS TEXT)
            )
        )

        -- 3. Lọc trạng thái đối soát
        AND (
            CAST(:trangThaiDoiSoat AS TEXT) IS NULL
            OR reconciliation_status = CAST(:trangThaiDoiSoat AS TEXT)
        )

        -- 4. Lọc tình trạng GOI_MOI
        AND (
            CAST(:tinhTrangGoiMoi AS TEXT) IS NULL
            OR (
                CAST(:tinhTrangGoiMoi AS TEXT) = 'HAS'
                AND event_count > 0
            )
            OR (
                CAST(:tinhTrangGoiMoi AS TEXT) = 'NONE'
                AND event_count = 0
                AND kit_row_count > 0
            )
        )
)
SELECT COUNT(*) AS total_rows
FROM filtered;