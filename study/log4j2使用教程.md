#### log4j2使用教程


官网：https://logging.apache.org/log4j/2.x/manual/configuration.html
### 添加依赖
```xml
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-api</artifactId>
            <version>2.13.3</version>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>2.13.3</version>
        </dependency>
```

### 典型的log4j2.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">

    <!--输出类型-->
    <Appenders>
        <!--控制台输出-->
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>

        <!--文件输出-->
        <RollingFile name="RollingFile" fileName="D:/logs/web.log"
                     filePattern="logs/$${date:yyyy-MM}/web-%d{MM-dd-yyyy}-%i.log.gz">
            <PatternLayout pattern="%d{yyyy-MM-dd 'at' HH:mm:ss z} %-5level %class{36} %L %M - %msg%xEx%n"/>
            <SizeBasedTriggeringPolicy size="2MB"/>
        </RollingFile>

    </Appenders>

    <!--只有配置这个才会输出-->
    <Loggers>
        <!--输出到控制台-->
        <Root level="warn">
            <AppenderRef ref="Console"/>
        </Root>
        <!--指定某个类输出到指定文件-->
        <Logger name="logTest" level="warn" additivity="true">
            <AppenderRef ref="RollingFile" />
        </Logger>
    </Loggers>

</Configuration>
```

### 测试
```java
public class logTest {

    private static final Logger logger = LogManager.getLogger(logTest.class);

    public static void main(String[] args) {
        logger.warn("测试日志输入warn");
    }
}
```

### 控制台输出
13:57:41.569 [main] WARN  logTest - 测试日志输入warn
### 文件输出D:/logs/web.log
2020-08-02 at 13:57:41 CST WARN  logTest 9 main - 测试日志输入warn


