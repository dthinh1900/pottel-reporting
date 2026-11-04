
-- =========================================================
-- BC-DS-01: BÁO CÁO ĐỐI SOÁT KIT VÀ GOI_MOI
--
-- 1A: Số liệu tổng quan                  0 tham số
-- 1B: Tổng hợp GOI_MOI theo ngày         4 tham số
-- 1C: Chi tiết thuê bao                 15 tham số
-- 1D: Đếm tổng số dòng                  13 tham số
-- =========================================================


-- @table 1A

WITH kit AS (
    SELECT
        NULLIF(BTRIM(msisdn_snapshot), '') AS msisdn,
        registed_date
    FROM crm.linhnn_kits_prod
),
kit_keys AS (
    SELECT
        msisdn,
        MAX(registed_date) AS registed_date
    FROM kit
    WHERE msisdn IS NOT NULL
    GROUP BY msisdn
),
goi_moi AS (
    SELECT
        NULLIF(BTRIM(msisdn), '') AS msisdn,
        COUNT(*) AS event_count
    FROM cdr.vnpt_package_new
    WHERE msisdn IS NOT NULL
      AND BTRIM(msisdn) <> ''
    GROUP BY NULLIF(BTRIM(msisdn), '')
),
matched AS (
    SELECT
        g.msisdn,
        k.registed_date
    FROM goi_moi g
    INNER JOIN kit_keys k
        ON k.msisdn = g.msisdn
)
SELECT
    (SELECT COUNT(*) FROM kit) AS total_kit,
    (SELECT COUNT(*) FROM kit_keys) AS unique_kit_msisdn,
    (SELECT COALESCE(SUM(event_count), 0)
     FROM goi_moi) AS total_goi_moi,
    (SELECT COUNT(*) FROM goi_moi)
        AS unique_goi_moi_msisdn,
    (SELECT COUNT(*) FROM matched)
        AS matched_msisdn,
    (SELECT COUNT(*)
     FROM goi_moi g
     LEFT JOIN kit_keys k ON k.msisdn = g.msisdn
     WHERE k.msisdn IS NULL) AS orphan_msisdn,
    (SELECT COUNT(*)
     FROM matched
     WHERE registed_date IS NULL)
        AS missing_registed_date,
    (SELECT COUNT(*)
     FROM goi_moi
     WHERE event_count >= 2)
        AS repeated_goi_moi_msisdn;


-- @table 1B

SELECT *
FROM public.fn_bc_ds_01_1b(
    CAST(? AS DATE),
    CAST(? AS DATE),
    CAST(? AS INTEGER),
    CAST(? AS INTEGER)
);


-- =========================================================
-- 1C: CHI TIẾT THUÊ BAO
-- 13 tham số lọc + 2 tham số phân trang
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
      AND (
          CAST(? AS DATE) IS NULL
          OR TO_DATE(TRIM(insert_time), 'DD-MON-YY') >= CAST(? AS DATE)
      )
      AND (
          CAST(? AS DATE) IS NULL
          OR TO_DATE(TRIM(insert_time), 'DD-MON-YY') <= CAST(? AS DATE)
      )
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
        -- 1. Lọc MSISDN
        (
            CAST(? AS TEXT) IS NULL
            OR msisdn LIKE CAST(? AS TEXT)
        )

        -- 2. Lọc trạng thái KIT
        AND (
            CAST(? AS TEXT) IS NULL
            OR EXISTS (
                SELECT 1
                FROM crm.linhnn_kits_prod kf
                WHERE NULLIF(BTRIM(kf.msisdn_snapshot), '') = details.msisdn
                  AND BTRIM(kf.status) = CAST(? AS TEXT)
            )
        )

        -- 3. Lọc trạng thái đối soát
        AND (
            CAST(? AS TEXT) IS NULL
            OR reconciliation_status = CAST(? AS TEXT)
        )

        -- 4. Lọc tình trạng GOI_MOI trong khoảng ngày
        AND (
            CAST(? AS TEXT) IS NULL
            OR (
                CAST(? AS TEXT) = 'HAS'
                AND event_count > 0
            )
            OR (
                CAST(? AS TEXT) = 'NONE'
                AND event_count = 0
                AND kit_row_count > 0
            )
        )
)
SELECT *
FROM filtered
ORDER BY last_event_date ASC NULLS LAST, msisdn ASC
LIMIT ? OFFSET ?;


-- =========================================================
-- 1D: ĐẾM TỔNG SỐ DÒNG
-- 13 tham số lọc, giống hệt bảng 1C
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
      AND (
          CAST(? AS DATE) IS NULL
          OR TO_DATE(TRIM(insert_time), 'DD-MON-YY') >= CAST(? AS DATE)
      )
      AND (
          CAST(? AS DATE) IS NULL
          OR TO_DATE(TRIM(insert_time), 'DD-MON-YY') <= CAST(? AS DATE)
      )
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
        -- 1. Lọc MSISDN
        (
            CAST(? AS TEXT) IS NULL
            OR msisdn LIKE CAST(? AS TEXT)
        )

        -- 2. Lọc trạng thái KIT
        AND (
            CAST(? AS TEXT) IS NULL
            OR EXISTS (
                SELECT 1
                FROM crm.linhnn_kits_prod kf
                WHERE NULLIF(BTRIM(kf.msisdn_snapshot), '') = details.msisdn
                  AND BTRIM(kf.status) = CAST(? AS TEXT)
            )
        )

        -- 3. Lọc trạng thái đối soát
        AND (
            CAST(? AS TEXT) IS NULL
            OR reconciliation_status = CAST(? AS TEXT)
        )

        -- 4. Lọc tình trạng GOI_MOI trong khoảng ngày
        AND (
            CAST(? AS TEXT) IS NULL
            OR (
                CAST(? AS TEXT) = 'HAS'
                AND event_count > 0
            )
            OR (
                CAST(? AS TEXT) = 'NONE'
                AND event_count = 0
                AND kit_row_count > 0
            )
        )
)
SELECT COUNT(*) AS total_rows
FROM filtered;
