package vn.pottel.reporting.runner;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import vn.pottel.reporting.model.ThamSoBaoCao;
import vn.pottel.reporting.service.BcDs01Service;

@Component
public class BaoCaoRunner {

    private final BcDs01Service service;

    public BaoCaoRunner(BcDs01Service service) {
        this.service = service;
    }

    // =========================
    // BẢNG 1A - TỔNG QUAN
    // =========================

    public Map<String, Object> chayTongQuan() {
        return service.getSummary();
    }

    // =========================
    // BẢNG 1B - THEO NGÀY
    // =========================

    public List<Map<String, Object>> chayBang1B(ThamSoBaoCao thamSo) {
        return service.getDailySummary(
                thamSo.tuNgay(),
                thamSo.denNgay(),
                thamSo.kichThuoc(),
                thamSo.tinhOffset()
        );
    }

    public int demSoNgayBang1B(ThamSoBaoCao thamSo) {
        return service.countDailySummary(
                thamSo.tuNgay(),
                thamSo.denNgay()
        );
    }

    // =========================
    // BẢNG 1C - CHI TIẾT THUÊ BAO
    // =========================

    public List<Map<String, Object>> chayBang1C(ThamSoBaoCao thamSo) {
        return service.getSubscriberDetails(
                thamSo.tuNgay(),
                thamSo.denNgay(),
                thamSo.msisdn(),
                thamSo.trangThaiKit(),
                thamSo.trangThaiDoiSoat(),
                thamSo.tinhTrangGoiMoi(),
                thamSo.kichThuoc(),
                thamSo.tinhOffset()
        );
    }

    public int demDongBang1C(ThamSoBaoCao thamSo) {
        return service.countSubscriberDetails(
                thamSo.tuNgay(),
                thamSo.denNgay(),
                thamSo.msisdn(),
                thamSo.trangThaiKit(),
                thamSo.trangThaiDoiSoat(),
                thamSo.tinhTrangGoiMoi()
        );
    }

    // =========================
    // DANH SÁCH TRẠNG THÁI CHO BỘ LỌC
    // =========================

    public List<String> layDanhSachTrangThaiKit() {
        return service.getKitStatuses();
    }

    public List<String> layDanhSachTrangThaiDoiSoat() {
        return service.getReconciliationStatuses();
    }
}