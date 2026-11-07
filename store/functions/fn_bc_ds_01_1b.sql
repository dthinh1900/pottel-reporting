-- FUNCTION: public.fn_bc_ds_01_1b(date, date, integer, integer)

-- DROP FUNCTION IF EXISTS public.fn_bc_ds_01_1b(date, date, integer, integer);

CREATE OR REPLACE FUNCTION public.fn_bc_ds_01_1b(
	p_fromdate date,
	p_todate date,
	p_page_size integer,
	p_offset integer)
    RETURNS TABLE(event_date date, event_count bigint, unique_msisdn bigint, matched_events bigint, orphan_events bigint, missing_registed_date_events bigint) 
    LANGUAGE 'sql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
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

    SELECT
        d.event_date,
        d.event_count,
        d.unique_msisdn,
        d.matched_events,
        d.orphan_events,
        d.missing_registed_date_events
    FROM daily d
    WHERE (p_fromdate IS NULL OR d.event_date >= p_fromdate)
      AND (p_todate IS NULL OR d.event_date <= p_todate)
    ORDER BY d.event_date
    LIMIT p_page_size
    OFFSET p_offset;
$BODY$;

ALTER FUNCTION public.fn_bc_ds_01_1b(date, date, integer, integer)
    OWNER TO postgres;

