package com.chitu.bigdata.sdp.config;

import com.chitu.bigdata.sdp.utils.TripleDesCipher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zouchangzhen
 * @date 2022/4/28
 */
@Configuration
public class TripleDesCipherConfig {

    private Logger LOG = LoggerFactory.getLogger(TripleDesCipherConfig.class);

    /**
     * cipherKey定下来就不要随意修改f
     */
    @Value("${sys.cipher.key:11111111111111111111111111111111111111}")
    private String cipherKey;

    @Bean("tripleDesCipher")
    public TripleDesCipher tripleDesCipher() throws Exception {
        TripleDesCipher tripleDesCipher = null;
        try {
            tripleDesCipher = new TripleDesCipher(cipherKey);
        } catch (Exception e) {
            LOG.error("加解密工具初始化失败", e);
            System.exit(1);
        }
        return tripleDesCipher;
    }
}
