package org.skellig.performance.runner.service

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.thymeleaf.spring5.SpringTemplateEngine
import org.springframework.web.servlet.ViewResolver
import org.thymeleaf.spring5.view.ThymeleafViewResolver
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry




@Configuration
open class HtmlTemplateResolverConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        if (!registry.hasMappingForPattern("/assets/**")) {
            registry.addResourceHandler("/assets/**").addResourceLocations("classpath:/assets/")
        }
    }

    @Bean
    @Description("Thymeleaf template resolver serving HTML")
    open fun templateResolver(): ClassLoaderTemplateResolver {
        val templateResolver = ClassLoaderTemplateResolver()
        templateResolver.prefix = "templates/"
        templateResolver.isCacheable = false
        templateResolver.suffix = ".html"
        templateResolver.setTemplateMode("HTML")
        templateResolver.characterEncoding = "UTF-8"
        return templateResolver
    }

    @Bean
    @Description("Thymeleaf template engine with Spring integration")
    open fun templateEngine(): SpringTemplateEngine {
        val templateEngine = SpringTemplateEngine()
        templateEngine.setTemplateResolver(templateResolver())
        return templateEngine
    }

    @Bean
    @Description("Thymeleaf view resolver")
    open fun viewResolver(): ViewResolver {
        val viewResolver = ThymeleafViewResolver()
        viewResolver.templateEngine = templateEngine()
        viewResolver.characterEncoding = "UTF-8"
        return viewResolver
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/").setViewName("index")
    }
}