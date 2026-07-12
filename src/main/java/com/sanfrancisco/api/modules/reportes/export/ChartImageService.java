package com.sanfrancisco.api.modules.reportes.export;

import com.sanfrancisco.api.modules.reportes.dto.response.OccupancyReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.ReservationsReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.RevenueReportResponse;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

/**
 * Genera gráficos con JFreeChart y los devuelve como data-URI PNG (base64)
 * para incrustarlos en el HTML que OpenHTMLtoPDF convierte a PDF. Server-side,
 * sin Chart.js ni navegador headless. Paleta alineada al dorado del hotel.
 */
@Component
public class ChartImageService {

    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM");
    private static final Font FUENTE_ETIQUETA = new Font("SansSerif", Font.PLAIN, 10);

    private static final Color ORO = new Color(166, 124, 61);
    private static final Color AZUL = new Color(91, 124, 153);
    /** Paleta coordinada para tortas/donas (dorados, azul, oliva, terracota, ciruela, ámbar). */
    private static final Color[] PALETA = {
            new Color(166, 124, 61),
            new Color(91, 124, 153),
            new Color(138, 154, 91),
            new Color(181, 101, 29),
            new Color(122, 106, 138),
            new Color(201, 162, 39),
    };

    /**
     * Curva de ocupación diaria (% por día) — serie de tiempo con eje de fecha.
     * El DateAxis elige automáticamente cuántas fechas mostrar (horizontales,
     * legibles), evitando el amontonamiento de un eje de categorías con 30 días.
     */
    public String ocupacionDiaria(List<OccupancyReportResponse.OccupancyPoint> serie) {
        TimeSeries ts = new TimeSeries("% Ocupación");
        for (OccupancyReportResponse.OccupancyPoint p : serie) {
            LocalDate d = p.fecha();
            ts.addOrUpdate(new Day(d.getDayOfMonth(), d.getMonthValue(), d.getYear()),
                    p.porcentajeOcupacion());
        }
        TimeSeriesCollection ds = new TimeSeriesCollection(ts);
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                null, null, "% Ocupación", ds, false, false, false);
        chart.setBackgroundPaint(Color.WHITE);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(250, 248, 244));
        plot.setRangeGridlinePaint(new Color(220, 213, 198));
        plot.setDomainGridlinesVisible(false);
        plot.setOutlineVisible(false);
        plot.getRenderer().setSeriesPaint(0, ORO);
        DateAxis eje = (DateAxis) plot.getDomainAxis();
        eje.setDateFormatOverride(new SimpleDateFormat("dd/MM"));
        eje.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 9));
        return toDataUri(chart, 560, 220);
    }

    /** Ingresos por método de pago — dona. */
    public String metodoPago(List<RevenueReportResponse.RevenueByMethod> metodos) {
        DefaultPieDataset<String> ds = new DefaultPieDataset<>();
        for (RevenueReportResponse.RevenueByMethod m : metodos) {
            ds.setValue(m.metodoPago(), m.monto());
        }
        return dona(ds, 300, 220);
    }

    /** Reservas por estado — dona. */
    public String reservasPorEstado(List<ReservationsReportResponse.ReservationsByStatus> estados) {
        DefaultPieDataset<String> ds = new DefaultPieDataset<>();
        for (ReservationsReportResponse.ReservationsByStatus s : estados) {
            ds.setValue(s.estado(), s.cantidad());
        }
        return dona(ds, 300, 220);
    }

    /** % de ocupación por tipo de habitación — barras (solo porcentajes). */
    public String ocupacionPctPorTipo(List<OccupancyReportResponse.OccupancyByRoomType> tipos) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (OccupancyReportResponse.OccupancyByRoomType t : tipos) {
            ds.addValue(t.porcentajeOcupacion(), "% Ocupación", t.tipoHabitacion());
        }
        JFreeChart chart = barras(ds, "% Ocupación", ORO);
        return toDataUri(chart, 300, 220);
    }

    /** ADR y RevPAR por tipo de habitación — barras (solo montos en S/). */
    public String tarifasPorTipo(List<OccupancyReportResponse.OccupancyByRoomType> tipos) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (OccupancyReportResponse.OccupancyByRoomType t : tipos) {
            ds.addValue(t.adr(), "ADR", t.tipoHabitacion());
            ds.addValue(t.revpar(), "RevPAR", t.tipoHabitacion());
        }
        JFreeChart chart = barras(ds, "S/", ORO, AZUL);
        return toDataUri(chart, 300, 220);
    }

    /** Ingresos diarios (anticipos vs saldos) — barras por día. */
    public String ingresosDiarios(List<RevenueReportResponse.RevenuePoint> serie) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (RevenueReportResponse.RevenuePoint p : serie) {
            String dia = p.fecha().format(DIA_MES);
            ds.addValue(p.ingresosAnticipos(), "Anticipos", dia);
            ds.addValue(p.ingresosSaldos(), "Saldos", dia);
        }
        JFreeChart chart = barras(ds, "S/", ORO, AZUL);
        chart.getCategoryPlot().getDomainAxis()
                .setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        return toDataUri(chart, 520, 220);
    }

    /** Reservas por tipo de habitación (cantidad) — dona. */
    public String reservasPorTipo(List<ReservationsReportResponse.ReservationsByRoomType> tipos) {
        DefaultPieDataset<String> ds = new DefaultPieDataset<>();
        for (ReservationsReportResponse.ReservationsByRoomType t : tipos) {
            ds.setValue(t.tipoHabitacion(), t.cantidad());
        }
        return dona(ds, 300, 220);
    }

    // ---------------------------------------------------------------

    private String dona(DefaultPieDataset<String> ds, int w, int h) {
        RingPlot plot = new RingPlot(ds);
        plot.setSectionDepth(0.38);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);
        plot.setSeparatorsVisible(false);
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})"));
        plot.setLabelFont(FUENTE_ETIQUETA);
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        int i = 0;
        for (Object key : ds.getKeys()) {
            plot.setSectionPaint((Comparable<?>) key, PALETA[i % PALETA.length]);
            i++;
        }
        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        chart.setBackgroundPaint(Color.WHITE);
        return toDataUri(chart, w, h);
    }

    private JFreeChart barras(DefaultCategoryDataset ds, String ejeY, Color... coloresSerie) {
        JFreeChart chart = ChartFactory.createBarChart(
                null, null, ejeY, ds, PlotOrientation.VERTICAL, coloresSerie.length > 1, false, false);
        estiloCategoria(chart);
        BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
        renderer.setBarPainter(new StandardBarPainter()); // sin degradado
        renderer.setShadowVisible(false);
        for (int i = 0; i < coloresSerie.length; i++) {
            renderer.setSeriesPaint(i, coloresSerie[i]);
        }
        return chart;
    }

    private void estiloCategoria(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(250, 248, 244));
        plot.setRangeGridlinePaint(new Color(220, 213, 198));
        plot.setOutlineVisible(false);
    }

    private String toDataUri(JFreeChart chart, int width, int height) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ChartUtils.writeChartAsPNG(baos, chart, width, height);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException("Error generando gráfico del reporte", e);
        }
    }
}
