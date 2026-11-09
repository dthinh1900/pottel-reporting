
-- =====================================================
-- BC-DT-01: TỔNG HỢP DOANH THU THEO NGÀY
-- 1A: Tổng hợp doanh thu theo ngày
-- Tham số:
--   1. Ngày bắt đầu (YYYY-MM-DD)
--   2. Ngày kết thúc (YYYY-MM-DD)
-- =====================================================

-- @table 1A

WITH
-- 1. KHOẢNG NGÀY BÁO CÁO
tham_so AS (
    SELECT
        TO_DATE(CAST(? AS TEXT), 'YYYY-MM-DD') AS tu_ngay,
        TO_DATE(CAST(? AS TEXT), 'YYYY-MM-DD') AS den_ngay,
        0.10::numeric AS vat_dt
),

-- 2. KIT ĐÃ KÍCH HOẠT VÀ CÓ ĐẠI LÝ
kich_hoat AS (
    SELECT
        k.id AS kit_id,
        TRIM(k.msisdn_snapshot) AS msisdn,
        TRIM(k.main_package_code) AS ma_goi,
        k.registed_date::date AS ngay_kich_hoat,
        k.agency_id
    FROM crm.linhnn_kits_prod k
    INNER JOIN esim_agency.agencies a
        ON a.id = k.agency_id
    WHERE k.registed_date IS NOT NULL
),

-- 3. NGÀY GOI_MOI ĐẦU TIÊN CỦA MỖI THUÊ BAO
goi_moi_dau AS (
    SELECT
        TRIM(msisdn) AS msisdn,
        MIN(TO_DATE(TRIM(insert_time), 'DD-MON-YY'))
            AS ngay_active_goi
    FROM cdr.vnpt_package_new
    WHERE msisdn IS NOT NULL
      AND insert_time IS NOT NULL
    GROUP BY TRIM(msisdn)
),

-- 4. CHU KỲ GÓI
chu_ky AS (
    SELECT *
    FROM (
        VALUES
            ('DL5', 5),
            ('DL7', 7),
            ('DL10', 10),
            ('DL15', 15),
            ('DL30', 30),
            ('M2M5', 5),
            ('M2M7', 7),
            ('M2M10', 10),
            ('M2M15', 15)
    ) AS t(ma_goi, so_ngay)
),

-- 5. XÁC ĐỊNH GIÁ VÀ KHOẢNG PHÂN BỔ
goi_pb AS (
    SELECT
        k.kit_id,
        k.msisdn,
        k.ma_goi,
        g.ngay_active_goi AS ngay_d1,
        c.so_ngay AS chu_ky,
        p.base_price::numeric AS gia_gross,

        ROUND(
            p.base_price::numeric / (1 + ts.vat_dt),
            2
        ) AS gia_net,

        ts.vat_dt

    FROM kich_hoat k

    INNER JOIN goi_moi_dau g
        ON g.msisdn = k.msisdn

    INNER JOIN inventory.products p
        ON TRIM(p.code) = k.ma_goi

    INNER JOIN chu_ky c
        ON c.ma_goi = k.ma_goi

    CROSS JOIN tham_so ts

    WHERE g.ngay_active_goi IS NOT NULL
),

-- 6. SINH TỪNG NGÀY TRONG CHU KỲ GÓI
phan_bo_ngay AS (
    SELECT
        b.kit_id,
        b.msisdn,
        b.ma_goi,
        gs.ngay::date AS ngay,

        b.gia_gross,
        b.gia_net,
        b.chu_ky,

        (gs.ngay::date - b.ngay_d1 + 1) AS ngay_thu

    FROM goi_pb b

    CROSS JOIN LATERAL generate_series(
        b.ngay_d1::timestamp,
        (b.ngay_d1 + b.chu_ky - 1)::timestamp,
        INTERVAL '1 day'
    ) gs(ngay)

    CROSS JOIN tham_so ts

    WHERE gs.ngay::date BETWEEN ts.tu_ngay AND ts.den_ngay
),

-- 7. TÍNH DOANH THU TỪNG NGÀY
doanh_thu_ngay AS (
    SELECT
        kit_id,
        msisdn,
        ma_goi,
        ngay,

        CASE
            WHEN ngay_thu = chu_ky
                THEN gia_gross
                     - ROUND(gia_gross / chu_ky, 2)
                       * (chu_ky - 1)
            ELSE ROUND(gia_gross / chu_ky, 2)
        END AS doanh_thu_gross,

        CASE
            WHEN ngay_thu = chu_ky
                THEN gia_net
                     - ROUND(gia_net / chu_ky, 2)
                       * (chu_ky - 1)
            ELSE ROUND(gia_net / chu_ky, 2)
        END AS doanh_thu_net

    FROM phan_bo_ngay
),

-- 8. TẠO DANH SÁCH TẤT CẢ NGÀY BÁO CÁO
lich_ngay AS (
    SELECT
        gs.ngay::date AS ngay
    FROM tham_so ts

    CROSS JOIN LATERAL generate_series(
        ts.tu_ngay::timestamp,
        ts.den_ngay::timestamp,
        INTERVAL '1 day'
    ) gs(ngay)
)

-- 9. TỔNG HỢP DOANH THU THEO NGÀY
SELECT
    l.ngay AS ngay_bao_cao,

    COUNT(DISTINCT d.kit_id) AS so_thue_bao,

    ROUND(
        COALESCE(SUM(d.doanh_thu_net), 0),
        2
    ) AS doanh_thu_chua_vat,

    ROUND(
        COALESCE(
            SUM(d.doanh_thu_gross - d.doanh_thu_net),
            0
        ),
        2
    ) AS tien_vat,

    ROUND(
        COALESCE(SUM(d.doanh_thu_gross), 0),
        2
    ) AS tong_doanh_thu

FROM lich_ngay l

LEFT JOIN doanh_thu_ngay d
    ON d.ngay = l.ngay

GROUP BY l.ngay
ORDER BY l.ngay;
