
-- =====================================================
-- BC-DT-02: CHI TIẾT DOANH THU THEO THUÊ BAO
-- 1A: Chi tiết doanh thu từng thuê bao trong một ngày
-- Tham số:
--   1. Ngày cần xem (YYYY-MM-DD)
-- =====================================================

-- @table 1A

WITH
-- 1. NGÀY CẦN XEM
tham_so AS (
    SELECT
        TO_DATE(CAST(? AS TEXT), 'YYYY-MM-DD') AS ngay_xem,
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
        k.ngay_kich_hoat,
        k.agency_id,
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

-- 6. SINH CÁC NGÀY TRONG CHU KỲ GÓI
phan_bo_ngay AS (
    SELECT
        b.kit_id,
        b.msisdn,
        b.ma_goi,
        b.ngay_kich_hoat,
        b.agency_id,
        b.ngay_d1,
        (b.ngay_d1 + b.chu_ky - 1) AS ngay_ket_thuc,
        b.chu_ky,
        gs.ngay::date AS ngay,
        (gs.ngay::date - b.ngay_d1 + 1) AS ngay_thu,
        b.gia_gross,
        b.gia_net
    FROM goi_pb b

    CROSS JOIN LATERAL generate_series(
        b.ngay_d1::timestamp,
        (b.ngay_d1 + b.chu_ky - 1)::timestamp,
        INTERVAL '1 day'
    ) gs(ngay)

    CROSS JOIN tham_so ts

    WHERE gs.ngay::date = ts.ngay_xem
),

-- 7. TÍNH DOANH THU CỦA TỪNG THUÊ BAO TRONG NGÀY
doanh_thu_ngay AS (
    SELECT
        kit_id,
        msisdn,
        ma_goi,
        ngay_kich_hoat,
        agency_id,
        ngay_d1,
        ngay_ket_thuc,
        chu_ky,
        ngay,
        ngay_thu,

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
)

-- 8. KẾT QUẢ CHI TIẾT
SELECT
    ngay AS ngay_bao_cao,
    kit_id,
    msisdn,
    ma_goi,
    ngay_kich_hoat,
    ngay_d1,
    ngay_ket_thuc,
    chu_ky,
    ngay_thu,

    ROUND(doanh_thu_net, 2) AS doanh_thu_chua_vat,

    ROUND(
        doanh_thu_gross - doanh_thu_net,
        2
    ) AS tien_vat,

    ROUND(doanh_thu_gross, 2) AS tong_doanh_thu

FROM doanh_thu_ngay
ORDER BY msisdn, kit_id;
