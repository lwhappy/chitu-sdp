package com.chitu.bigdata.sdp.flink.submit.util;

import com.chitu.bigdata.sdp.flink.common.util.CommandUtils;
import com.chitu.bigdata.sdp.flink.submit.constant.FlinkConstant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author sutao
 * @create 2021-10-22 18:27
 */
@Slf4j
public class FlinkUtils {

    public static String flinkVersion = null;

    public static String getFlinkVersion() {
        String flinkHome = FlinkConstant.FLINK_HOME;
        String libPath = flinkHome.concat("/lib");
        File[] distJar = new File(libPath).listFiles(x -> x.getName().matches("flink-dist.*\\.jar"));
        if (distJar == null || distJar.length == 0) {
            throw new IllegalArgumentException("[SDP] can no found flink-dist jar in " + libPath);
        }
        if (distJar.length > 1) {
            throw new IllegalArgumentException("[SDP] found multiple flink-dist jar in " + libPath);
        }
        List<String> cmd = Arrays.asList(
                "cd ".concat(flinkHome),
                String.format(
                        "java -classpath %s org.apache.flink.client.cli.CliFrontend --version",
                        distJar[0].getAbsolutePath()
                )
        );
        CommandUtils.execute(cmd, versionInfo -> {
            Matcher matcher = FlinkConstant.FLINK_VERSION_PATTERN.matcher(versionInfo);
            if (matcher.find()) {
                log.info("Flink version: {}", versionInfo);
                flinkVersion = matcher.group(1);
            }
        });
        return flinkVersion;
    }


    @SneakyThrows
    public static String getFlinkDefaultConfig() {
        String flinkLocalHome = FlinkConstant.FLINK_HOME;
        assert flinkLocalHome != null;
        File yaml = new File(flinkLocalHome.concat("/conf/flink-conf.yaml"));
        assert yaml.exists();
        return FileUtils.readFileToString(yaml);
    }

}
