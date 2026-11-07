package vn.pottel.reporting.baocao.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BcDs01Repository {

    private final JdbcTemplate jdbcTemplate;

    public BcDs01Repository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =====================================================
    // BẢNG 1A - SỐ LIỆU TỔNG QUAN
    // =====================================================

    public Map<String, Object> getSummary() {

        String sql = """
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
                    AS repeated_goi_moi_msisdn
            """;

        return jdbcTemplate.queryForMap(sql);
    }

    // =====================================================
    // DANH SÁCH LỰA CHỌN CHO BỘ LỌC
    // =====================================================

    public List<String> getKitStatuses() {

        String sql = """
            SELECT DISTINCT BTRIM(status)
            FROM crm.linhnn_kits_prod
            WHERE status IS NOT NULL
              AND BTRIM(status) <> ''
            ORDER BY BTRIM(status)
            """;

        return jdbcTemplate.queryForList(sql, String.class);
    }

    public List<String> getReconciliationStatuses() {

        String sql = """
            SELECT DISTINCT reconciliation_status
            FROM (
                SELECT
                    CASE
                        WHEN k.msisdn IS NULL
                            THEN 'Không tìm thấy KIT'
                        WHEN g.msisdn IS NULL
                            THEN 'Chỉ có KIT'
                        WHEN k.registed_date IS NULL
                            THEN 'Khớp KIT - thiếu ngày đăng ký'
                        ELSE 'Khớp KIT'
                    END AS reconciliation_status
                FROM (
                    SELECT
                        NULLIF(BTRIM(msisdn_snapshot), '') AS msisdn,
                        MAX(registed_date) AS registed_date
                    FROM crm.linhnn_kits_prod
                    WHERE NULLIF(BTRIM(msisdn_snapshot), '') IS NOT NULL
                    GROUP BY NULLIF(BTRIM(msisdn_snapshot), '')
                ) k
                FULL OUTER JOIN (
                    SELECT DISTINCT
                        NULLIF(BTRIM(msisdn), '') AS msisdn
                    FROM cdr.vnpt_package_new
                    WHERE NULLIF(BTRIM(msisdn), '') IS NOT NULL
                ) g
                    ON k.msisdn = g.msisdn
            ) statuses
            ORDER BY reconciliation_status
            """;

        return jdbcTemplate.queryForList(sql, String.class);
    }

    // =====================================================
    // ĐẾM TỔNG SỐ NGÀY CỦA BẢNG 1B SAU KHI LỌC
    // =====================================================

    public int countDailySummary(LocalDate fromDate,LocalDate toDate) {

        String sql = """
            SELECT public.fn_bc_ds_01_1b_count(
                CAST(? AS DATE),
                CAST(? AS DATE)
            )
            """;

        Date from = fromDate == null ? null : Date.valueOf(fromDate);
        Date to = toDate == null ? null : Date.valueOf(toDate);

        Long result = jdbcTemplate.queryForObject(
            sql,
            Long.class,
            from,
            to
        );

        return result == null ? 0 : result.intValue();
    }
}