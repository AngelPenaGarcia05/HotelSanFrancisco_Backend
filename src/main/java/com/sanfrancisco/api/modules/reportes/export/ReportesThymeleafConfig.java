package com.sanfrancisco.api.modules.reportes.export;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Motor Thymeleaf standalone dedicado a los reportes. Se configura a mano
 * (no vía el starter MVC) para renderizar plantillas a String sin acoplar
 * la generación de PDF al view-resolver web de Spring. Resuelve plantillas
 * en classpath:/templates/reportes/*.html.
 */
@Configuration
public class ReportesThymeleafConfig {

    @Bean
    public TemplateEngine reportesTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        // Sin caché: permite ajustar la plantilla sin reiniciar durante el
        // desarrollo. Para producción puede activarse (true).
        resolver.setCacheable(false);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
