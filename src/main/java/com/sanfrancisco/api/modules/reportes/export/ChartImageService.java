package com.sanfrancisco.api.modules.reportes.export;

import com.sanfrancisco.api.modules.reportes.dto.response.OccupancyReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.ReservationsReportResponse;
import com.sanfrancisco.api.modules.reportes.dto.response.RevenueReportResponse;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
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

    /** Curva de ocupación diaria (% por día) — líneas. */
    public String ocupacionDiaria(List<OccupancyReportResponse.OccupancyPoint> serie) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        for (OccupancyReportResponse.OccupancyPoint p : serie) {
            ds.addValue(p.porcentajeOcupacion(), "% Ocupación", p.fecha().format(DIA_MES));
        }
        JFreeChart chart = ChartFactory.createLineChart(
                null, null, "% Ocupación", ds, PlotOrientation.VERTICAL, false, false, false);
        estiloCategoria(chart);
        chart.getCategoryPlot().getDomainAxis()
                .setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        chart.getCategoryPlot().getRenderer().setSeriesPaint(0, ORO);
        return toDataUri(chart, 520, 200);
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
