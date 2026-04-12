package com.nurun.config;

import com.nurun.enumlist.ProviderMode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "provider")
public class ProviderModeProperties {


    private ProviderMode groqMode =  ProviderMode.MANUAL;
    private ProviderMode geminiMode = ProviderMode.MANUAL;
    private ProviderMode ollamaMode = ProviderMode.SPRING;
    private ProviderMode openrouterMode =  ProviderMode.SPRING;


}
