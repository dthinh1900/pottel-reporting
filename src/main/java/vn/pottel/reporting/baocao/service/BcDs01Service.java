package vn.pottel.reporting.baocao.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import vn.pottel.reporting.baocao.repository.BcDs01Repository;

@Service
public class BcDs01Service {

    private final BcDs01Repository repository;

    public BcDs01Service(BcDs01Repository repository) {
        this.repository = repository;
    }

    // =========================
    // BẢNG 1A - TỔNG QUAN
    // =========================

    public Map<String, Object> getSummary() {
        return repository.getSummary();
    }

    // =========================
    // BẢNG 1B - TỔNG HỢP THEO NGÀY
    // =========================

    public int countDailySummary(
            LocalDate fromDate,
            LocalDate toDate) {

        return repository.countDailySummary(
                fromDate,
                toDate
        );
    }

    // =========================
    // DANH SÁCH TRẠNG THÁI CHO BỘ LỌC
    // =========================

    public List<String> getKitStatuses() {
        return repository.getKitStatuses();
    }

    public List<String> getReconciliationStatuses() {
        return repository.getReconciliationStatuses();
    }
}