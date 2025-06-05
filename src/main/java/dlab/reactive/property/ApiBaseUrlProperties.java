package dlab.reactive.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "api.base-url")
@Data
public class ApiBaseUrlProperties {
    private Map<String, String> urls;
}
