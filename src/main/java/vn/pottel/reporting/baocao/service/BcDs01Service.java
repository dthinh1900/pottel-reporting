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

<<<<<<< HEAD
=======
    public List<Map<String, Object>> getDailySummary(
            LocalDate fromDate,
            LocalDate toDate,
            int pageSize,
            int offset) {

        return repository.getDailySummary(
                fromDate,
                toDate,
                pageSize,
                offset
        );
    }

>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
    public int countDailySummary(
            LocalDate fromDate,
            LocalDate toDate) {

        return repository.countDailySummary(
                fromDate,
                toDate
        );
    }

    // =========================
<<<<<<< HEAD
=======
    // BẢNG 1C - CHI TIẾT THUÊ BAO
    // =========================

    public List<Map<String, Object>> getSubscriberDetails(
            LocalDate fromDate,
            LocalDate toDate,
            String msisdn,
            String kitStatus,
            String reconciliationStatus,
            String tinhTrangGoiMoi,
            int pageSize,
            int offset) {

        return repository.getSubscriberDetails(
                fromDate,
                toDate,
                msisdn,
                kitStatus,
                reconciliationStatus,
                tinhTrangGoiMoi,
                pageSize,
                offset
        );
    }

    public int countSubscriberDetails(
            LocalDate fromDate,
            LocalDate toDate,
            String msisdn,
            String kitStatus,
            String reconciliationStatus,
            String tinhTrangGoiMoi) {

        return repository.countSubscriberDetails(
                fromDate,
                toDate,
                msisdn,
                kitStatus,
                reconciliationStatus,
                tinhTrangGoiMoi
        );
    }

    // =========================
>>>>>>> e793bc8e50a86953ab83310ebe6cc50ed425b7f2
    // DANH SÁCH TRẠNG THÁI CHO BỘ LỌC
    // =========================

    public List<String> getKitStatuses() {
        return repository.getKitStatuses();
    }

    public List<String> getReconciliationStatuses() {
        return repository.getReconciliationStatuses();
    }
}