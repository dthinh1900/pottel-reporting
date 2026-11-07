-- FUNCTION: public.fn_bc_ds_01_1b_count(date, date)

-- DROP FUNCTION IF EXISTS public.fn_bc_ds_01_1b_count(date, date);

CREATE OR REPLACE FUNCTION public.fn_bc_ds_01_1b_count(
	p_fromdate date,
	p_todate date)
    RETURNS bigint
    LANGUAGE 'sql'
    COST 100
    VOLATILE PARALLEL UNSAFE
AS $BODY$

    SELECT COUNT(DISTINCT TO_DATE(TRIM(insert_time), 'DD-MON-YY'))
    FROM cdr.vnpt_package_new
    WHERE insert_time IS NOT NULL
      AND TRIM(insert_time) <> ''
      AND (p_fromdate IS NULL
           OR TO_DATE(TRIM(insert_time), 'DD-MON-YY') >= p_fromdate)
      AND (p_todate IS NULL
           OR TO_DATE(TRIM(insert_time), 'DD-MON-YY') <= p_todate);
$BODY$;

ALTER FUNCTION public.fn_bc_ds_01_1b_count(date, date)
    OWNER TO postgres;

