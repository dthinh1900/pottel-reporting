-- =========================================================
-- BC-DS-01: BÁO CÁO ĐỐI SOÁT KIT VÀ GOI_MOI
--
-- 1A: Số liệu tổng quan          0 tham số
-- 1B: Tổng hợp GOI_MOI theo ngày 4 tham số
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
    CAST(:tuNgay AS DATE),
    CAST(:denNgay AS DATE),
    CAST(:limit AS INTEGER),
    CAST(:offset AS INTEGER)
);


-- @table 1D
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
        TO_DATE(TRIM(insert_time), 'DD-MON-YY') AS event_date,
        NULLIF(BTRIM(msisdn), '') AS msisdn
    FROM cdr.vnpt_package_new
    WHERE msisdn IS NOT NULL
      AND BTRIM(msisdn) <> ''
      AND insert_time IS NOT NULL
      AND TRIM(insert_time) <> ''
),
daily AS (
    SELECT
        g.event_date,
        COUNT(*) AS event_count,
        COUNT(DISTINCT g.msisdn) AS unique_msisdn,
        COUNT(*) FILTER (
            WHERE k.msisdn IS NOT NULL
        ) AS matched_events,
        COUNT(*) FILTER (
            WHERE k.msisdn IS NULL
        ) AS orphan_events,
        COUNT(*) FILTER (
            WHERE k.msisdn IS NOT NULL
              AND k.registed_date IS NULL
        ) AS missing_registed_date_events
    FROM goi_moi g
    LEFT JOIN kit_keys k
        ON k.msisdn = g.msisdn
    GROUP BY g.event_date
)
SELECT COUNT(*) AS total_rows
FROM daily d
WHERE (:tuNgay IS NULL OR d.event_date >= CAST(:tuNgay AS DATE))
  AND (:denNgay IS NULL OR d.event_date <= CAST(:denNgay AS DATE));