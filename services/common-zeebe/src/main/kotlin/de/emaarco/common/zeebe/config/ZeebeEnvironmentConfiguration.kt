package de.emaarco.common.zeebe.config

import org.springframework.context.annotation.PropertySource
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource as EnvPropertySource
import org.springframework.core.io.support.EncodedResource
import org.springframework.core.io.support.PropertySourceFactory
import java.util.*

@PropertySource(
    value = ["classpath:zeebe-application.yaml"],
    factory = ZeebeEnvironmentConfiguration.YamlPropertySourceFactory::class
)
class ZeebeEnvironmentConfiguration {
    class YamlPropertySourceFactory : PropertySourceFactory {
        override fun createPropertySource(name: String?, resource: EncodedResource): EnvPropertySource<*> {
            val factory = YamlPropertiesFactoryBean()
            factory.setResources(resource.resource)
            val properties: Properties = factory.getObject() ?: Properties()
            return PropertiesPropertySource(name ?: resource.resource.filename!!, properties)
        }
    }
}