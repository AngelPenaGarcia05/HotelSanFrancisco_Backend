package com.sanfrancisco.api.modules.reportes.export;

import com.sanfrancisco.api.modules.reportes.dto.response.ManagementDashboardResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.OccupancyReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.ReservationsReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.RevenueReportResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Genera archivos Excel (.xlsx) de los reportes con Apache POI. Una hoja por
 * sección (resumen, series, desgloses) para que quede tabular y limpio, a
 * diferencia del CSV multi-sección. Los montos van como número real con
 * formato de moneda/porcentaje, evitando el problema de separador decimal.
 */
@Component
public class ExcelReportExporter {

    public static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String EXTENSION = "xlsx";

    // ---------------------------------------------------------------
    // Estilos por libro (se crean una vez y se reutilizan)
    // ---------------------------------------------------------------
    private static final class Estilos {
        final CellStyle titulo;
        final CellStyle header;
        final CellStyle moneda;
        final CellStyle porcentaje;
        final CellStyle entero;

        Estilos(Workbook wb) {
            DataFormat fmt = wb.createDataFormat();

            Font fontTitulo = wb.createFont();
            fontTitulo.setBold(true);
            fontTitulo.setFontHeightInPoints((short) 14);
            titulo = wb.createCellStyle();
            titulo.setFont(fontTitulo);

            Font fontHeader = wb.createFont();
            fontHeader.setBold(true);
            header = wb.createCellStyle();
            header.setFont(fontHeader);
            header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setBorderBottom(BorderStyle.THIN);

            moneda = wb.createCellStyle();
            moneda.setDataFormat(fmt.getFormat("\"S/ \"#,##0.00"));

            porcentaje = wb.createCellStyle();
            porcentaje.setDataFormat(fmt.getFormat("0.0\"%\""));

            entero = wb.createCellStyle();
            entero.setDataFormat(fmt.getFormat("#,##0"));
        }
    }

    // ---------------------------------------------------------------
    // API pública por tipo de reporte
    // ---------------------------------------------------------------

    public byte[] ingresos(RevenueReportResponse r) {
        try (Workbook wb = new XSSFWorkbook()) {
            Estilos e = new Estilos(wb);
            hojaIngresos(wb, e, r);
            return toBytes(wb);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public byte[] reservas(ReservationsReportResponse r) {
        try (Workbook wb = new XSSFWorkbook()) {
            Estilos e = new Estilos(wb);
            hojaReservas(wb, e, r);
            return toBytes(wb);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public byte[] ocupacion(OccupancyReportResponse r) {
        try (Workbook wb = new XSSFWorkbook()) {
            Estilos e = new Estilos(wb);
            hojaOcupacion(wb, e, r);
            return toBytes(wb);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public byte[] gerencial(ManagementDashboardResponse r) {
        try (Workbook wb = new XSSFWorkbook()) {
            Estilos e = new Estilos(wb);
            hojaKpis(wb, e, r);
            hojaIngresos(wb, e, r.ingresos());
            hojaReservas(wb, e, r.reservas());
            hojaOcupacion(wb, e, r.ocupacion());
            return toBytes(wb);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    // ---------------------------------------------------------------
    // Hojas
    // ---------------------------------------------------------------

    private void hojaKpis(Workbook wb, Estilos e, ManagementDashboardResponse r) {
        Sheet s = nuevaHoja(wb, "KPIs");
        int rn = titulo(s, e, "Dashboard gerencial");
        header(s, e, rn++, "Indicador", "Valor");
        ManagementDashboardResponse.ManagementKpis k = r.kpis();
        rn = filaMoneda(s, e, rn, "Ingresos mes actual", k.ingresosMesActual());
        rn = filaMoneda(s, e, rn, "Ingresos mes anterior", k.ingresosMesAnterior());
        rn = filaPct(s, e, rn, "Variación ingresos", k.variacionIngresos());
        rn = filaPct(s, e, rn, "Ocupación actual", k.ocupacionActual());
        rn = filaPct(s, e, rn, "Ocupación mes anterior", k.ocupacionMesAnterior());
        rn = filaPct(s, e, rn, "Variación ocupación", k.variacionOcupacion());
        rn = filaEntero(s, e, rn, "Reservas activas", k.reservasActivas());
        rn = filaEntero(s, e, rn, "Reservas pendientes de pago", k.reservasPendientesPago());
        rn = filaMoneda(s, e, rn, "ADR", k.adrActual());
        rn = filaMoneda(s, e, rn, "RevPAR", k.revparActual());
        rn = filaEntero(s, e, rn, "Cancelaciones del mes", k.cancelacionesMes());
    }

    private void hojaIngresos(Workbook wb, Estilos e, RevenueReportResponse r) {
        Sheet resumen = nuevaHoja(wb, "Ingresos");
        int rn = titulo(resumen, e, "Reporte de ingresos");
        header(resumen, e, rn++, "Concepto", "Monto");
        rn = filaMoneda(resumen, e, rn, "Total ingresos", r.totalIngresos());
        rn = filaMoneda(resumen, e, rn, "Total anticipos", r.totalAnticipos());
        rn = filaMoneda(resumen, e, rn, "Total saldos", r.totalSaldos());
        rn = filaMoneda(resumen, e, rn, "Total reembolsos", r.totalReembolsos());
        rn = filaMoneda(resumen, e, rn, "Ingreso promedio diario", r.ingresoPromedioDiario());

        Sheet serie = nuevaHoja(wb, "Ingresos - Serie");
        int sr = titulo(serie, e, "Serie diaria de ingresos");
        header(serie, e, sr++, "Fecha", "Anticipos", "Saldos", "Reembolsos");
        for (RevenueReportResponse.RevenuePoint p : r.serie()) {
            Row row = serie.createRow(sr++);
            texto(row, 0, p.fecha());
            moneda(e, row, 1, p.ingresosAnticipos());
            moneda(e, row, 2, p.ingresosSaldos());
            moneda(e, row, 3, p.reembolsos());
        }

        Sheet metodos = nuevaHoja(wb, "Ingresos - Métodos");
        int mr = titulo(metodos, e, "Ingresos por método de pago");
        header(metodos, e, mr++, "Método de pago", "Monto", "Porcentaje");
        for (RevenueReportResponse.RevenueByMethod m : r.porMetodoPago()) {
            Row row = metodos.createRow(mr++);
            texto(row, 0, m.metodoPago());
            moneda(e, row, 1, m.monto());
            pct(e, row, 2, m.porcentaje());
        }
    }

    private void hojaReservas(Workbook wb, Estilos e, ReservationsReportResponse r) {
        Sheet resumen = nuevaHoja(wb, "Reservas");
        int rn = titulo(resumen, e, "Reporte de reservas");
        header(resumen, e, rn++, "Concepto", "Valor");
        rn = filaEntero(resumen, e, rn, "Total reservas", r.totalReservas());
        rn = filaEntero(resumen, e, rn, "Total canceladas", r.totalCanceladas());
        rn = filaPct(resumen, e, rn, "Tasa de cancelación", r.tasaCancelacion());
        rn = filaNumero(resumen, e, rn, "Estancia promedio (noches)", r.estanciaPromedioNoches());
        rn = filaMoneda(resumen, e, rn, "Ingresos perdidos (cancel./no-show)", r.ingresosPerdidosCancelaciones());

        Sheet estados = nuevaHoja(wb, "Reservas - Estados");
        int er = titulo(estados, e, "Reservas por estado");
        header(estados, e, er++, "Estado", "Cantidad", "Porcentaje");
        for (ReservationsReportResponse.ReservationsByStatus s : r.porEstado()) {
            Row row = estados.createRow(er++);
            texto(row, 0, s.estado());
            entero(e, row, 1, s.cantidad());
            pct(e, row, 2, s.porcentaje());
        }

        Sheet tipos = nuevaHoja(wb, "Reservas - Tipos");
        int tr = titulo(tipos, e, "Reservas por tipo de habitación");
        header(tipos, e, tr++, "Tipo de habitación", "Cantidad", "Ingresos");
        for (ReservationsReportResponse.ReservationsByRoomType t : r.porTipoHabitacion()) {
            Row row = tipos.createRow(tr++);
            texto(row, 0, t.tipoHabitacion());
            entero(e, row, 1, t.cantidad());
            moneda(e, row, 2, t.ingresos());
        }

        Sheet canales = nuevaHoja(wb, "Reservas - Canales");
        int cr = titulo(canales, e, "Rendimiento por canal de venta");
        header(canales, e, cr++, "Canal", "Reservas", "Ingresos", "Canceladas", "Tasa cancelación");
        for (ReservationsReportResponse.ReservationsByChannel c : r.porCanal()) {
            Row row = canales.createRow(cr++);
            texto(row, 0, c.canal());
            entero(e, row, 1, c.reservas());
            moneda(e, row, 2, c.ingresos());
            entero(e, row, 3, c.canceladas());
            pct(e, row, 4, c.tasaCancelacion());
        }

        Sheet serie = nuevaHoja(wb, "Reservas - Serie");
        int sr = titulo(serie, e, "Serie diaria de reservas");
        header(serie, e, sr++, "Fecha", "Nuevas", "Canceladas", "Check-ins", "Check-outs");
        for (ReservationsReportResponse.ReservationsPoint p : r.serie()) {
            Row row = serie.createRow(sr++);
            texto(row, 0, p.fecha());
            entero(e, row, 1, p.nuevas());
            entero(e, row, 2, p.canceladas());
            entero(e, row, 3, p.checkIns());
            entero(e, row, 4, p.checkOuts());
        }
    }

    private void hojaOcupacion(Workbook wb, Estilos e, OccupancyReportResponse r) {
        Sheet resumen = nuevaHoja(wb, "Ocupación");
        int rn = titulo(resumen, e, "Reporte de ocupación");
        header(resumen, e, rn++, "Indicador", "Valor");
        rn = filaPct(resumen, e, rn, "Ocupación promedio", r.ocupacionPromedio());
        rn = filaMoneda(resumen, e, rn, "ADR promedio", r.adrPromedio());
        rn = filaMoneda(resumen, e, rn, "RevPAR promedio", r.revparPromedio());

        Sheet serie = nuevaHoja(wb, "Ocupación - Serie");
        int sr = titulo(serie, e, "Serie diaria de ocupación");
        header(serie, e, sr++, "Fecha", "Habitaciones", "Ocupadas", "% Ocupación");
        for (OccupancyReportResponse.OccupancyPoint p : r.serie()) {
            Row row = serie.createRow(sr++);
            texto(row, 0, p.fecha());
            entero(e, row, 1, p.habitacionesTotal());
            entero(e, row, 2, p.habitacionesOcupadas());
            pct(e, row, 3, p.porcentajeOcupacion());
        }

        Sheet tipos = nuevaHoja(wb, "Ocupación - Tipos");
        int tr = titulo(tipos, e, "Ocupación por tipo de habitación");
        header(tipos, e, tr++, "Tipo", "Habitaciones", "Noches disp.", "Noches ocup.", "% Ocupación", "ADR", "RevPAR");
        for (OccupancyReportResponse.OccupancyByRoomType t : r.porTipoHabitacion()) {
            Row row = tipos.createRow(tr++);
            texto(row, 0, t.tipoHabitacion());
            entero(e, row, 1, t.habitacionesTotal());
            entero(e, row, 2, t.nochesDisponibles());
            entero(e, row, 3, t.nochesOcupadas());
            pct(e, row, 4, t.porcentajeOcupacion());
            moneda(e, row, 5, t.adr());
            moneda(e, row, 6, t.revpar());
        }
    }

    // ---------------------------------------------------------------
    // Helpers de escritura
    // ---------------------------------------------------------------

    private Sheet nuevaHoja(Workbook wb, String nombre) {
        Sheet s = wb.createSheet(nombre);
        s.setDefaultColumnWidth(20);
        return s;
    }

    private int titulo(Sheet s, Estilos e, String texto) {
        Row row = s.createRow(0);
        Cell c = row.createCell(0);
        c.setCellValue(texto);
        c.setCellStyle(e.titulo);
        return 2; // deja una fila en blanco tras el título
    }

    private void header(Sheet s, Estilos e, int rowNum, String... titulos) {
        Row row = s.createRow(rowNum);
        for (int i = 0; i < titulos.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(titulos[i]);
            c.setCellStyle(e.header);
        }
    }

    private int filaMoneda(Sheet s, Estilos e, int rowNum, String label, BigDecimal valor) {
        Row row = s.createRow(rowNum);
        texto(row, 0, label);
        moneda(e, row, 1, valor);
        return rowNum + 1;
    }

    private int filaPct(Sheet s, Estilos e, int rowNum, String label, BigDecimal valor) {
        Row row = s.createRow(rowNum);
        texto(row, 0, label);
        pct(e, row, 1, valor);
        return rowNum + 1;
    }

    private int filaNumero(Sheet s, Estilos e, int rowNum, String label, BigDecimal valor) {
        Row row = s.createRow(rowNum);
        texto(row, 0, label);
        Cell c = row.createCell(1);
        if (valor != null) c.setCellValue(valor.doubleValue());
        return rowNum + 1;
    }

    private int filaEntero(Sheet s, Estilos e, int rowNum, String label, long valor) {
        Row row = s.createRow(rowNum);
        texto(row, 0, label);
        entero(e, row, 1, valor);
        return rowNum + 1;
    }

    private void texto(Row row, int col, Object valor) {
        row.createCell(col).setCellValue(valor == null ? "" : valor.toString());
    }

    private void moneda(Estilos e, Row row, int col, BigDecimal valor) {
        Cell c = row.createCell(col);
        c.setCellValue(valor == null ? 0d : valor.doubleValue());
        c.setCellStyle(e.moneda);
    }

    private void pct(Estilos e, Row row, int col, BigDecimal valor) {
        Cell c = row.createCell(col);
        c.setCellValue(valor == null ? 0d : valor.doubleValue());
        c.setCellStyle(e.porcentaje);
    }

    private void entero(Estilos e, Row row, int col, long valor) {
        Cell c = row.createCell(col);
        c.setCellValue(valor);
        c.setCellStyle(e.entero);
    }

    private byte[] toBytes(Workbook wb) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            wb.write(out);
            return out.toByteArray();
        }
    }
}
