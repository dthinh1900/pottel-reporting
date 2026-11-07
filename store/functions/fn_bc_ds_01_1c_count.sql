-- FUNCTION: public.fn_bc_ds_01_1c_count(date, date, text, text, text)

-- DROP FUNCTION IF EXISTS public.fn_bc_ds_01_1c_count(date, date, text, text, text);

CREATE OR REPLACE FUNCTION public.fn_bc_ds_01_1c_count(
	p_fromdate date,
	p_todate date,
	p_msisdn text,
	p_kit_status text,
	p_reconciliation_status text)
    RETURNS bigint
    LANGUAGE 'sql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$
    WITH kit AS (
        SELECT
            NULLIF(BTRIM(k.msisdn_snapshot), '') AS msisdn,
            MAX(k.registed_date) AS registed_date
        FROM crm.linhnn_kits_prod k
        WHERE NULLIF(BTRIM(k.msisdn_snapshot), '') IS NOT NULL
        GROUP BY NULLIF(BTRIM(k.msisdn_snapshot), '')
    ),
    goi_moi AS (
    SELECT
        NULLIF(BTRIM(g.msisdn), '') AS msisdn,
        COUNT(*) AS event_count,
        MAX(TO_DATE(TRIM(g.insert_time), 'DD-MON-YY'))
            AS last_event_date
    FROM cdr.vnpt_package_new g
    WHERE NULLIF(BTRIM(g.msisdn), '') IS NOT NULL
      AND g.insert_time IS NOT NULL
      AND TRIM(g.insert_time) <> ''
    GROUP BY NULLIF(BTRIM(g.msisdn), '')
    ),
    details AS (
        SELECT
            COALESCE(k.msisdn, g.msisdn) AS msisdn,
            k.registed_date,
            COALESCE(g.event_count, 0) AS event_count,
            CASE
                WHEN k.msisdn IS NULL
                    THEN 'Không tìm thấy KIT'
                WHEN g.msisdn IS NULL
                    THEN 'Chỉ có KIT'
                WHEN k.registed_date IS NULL
                    THEN 'Khớp KIT - thiếu ngày đăng ký'
                ELSE 'Khớp KIT'
            END AS reconciliation_status
        FROM kit k
        FULL OUTER JOIN goi_moi g
            ON k.msisdn = g.msisdn
    )
    SELECT COUNT(*)
    FROM details d
    WHERE (p_msisdn IS NULL OR d.msisdn LIKE p_msisdn)
      AND (
          p_kit_status IS NULL
          OR EXISTS (
              SELECT 1
              FROM crm.linhnn_kits_prod kf
              WHERE NULLIF(BTRIM(kf.msisdn_snapshot), '') = d.msisdn
                AND BTRIM(kf.status) = p_kit_status
          )
      )
        AND (
            p_reconciliation_status IS NULL
            OR d.reconciliation_status = p_reconciliation_status
        )
        AND (
            p_fromdate IS NULL
            OR d.last_event_date >= p_fromdate
        )
        AND (
            p_todate IS NULL
            OR d.last_event_date <= p_todate
        );
$BODY$;

ALTER FUNCTION public.fn_bc_ds_01_1c_count(date, date, text, text, text)
    OWNER TO postgres;

