## Lombok使用

官方文档：https://projectlombok.org/features/all
### 添加依赖
```xml
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.12</version>
            <scope>provided</scope>
        </dependency>
```

### IDEA中安装Lombok插件
不安装的话，无法在编写代码时使用，会提示出错。
一定要勾选 Enable annotation processing，否则无法通过编译

### 常用注解
```
@Setter ：注解在属性上；为属性提供 setting 方法
@Setter ：注解在属性上；为属性提供 getting 方法
@Log4j2 ：注解在类上；为类提供一个 属性名为log 的 log4j2 日志对象
@NoArgsConstructor ：注解在类上；为类提供一个无参的构造方法
@AllArgsConstructor ：注解在类上；为类提供一个全参的构造方法
@Builder ： 被注解的类加个构造者模式
@NonNull : 如果给参数加个这个注解 参数为null会抛出空指针异常
@Cleanup : 可以关闭流
```

### 使用案例
```java
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Log4j2   //前提是已经引入log4j2
@ToString
public class StudyLombok {

    @Builder.Default  //使用无参构造器时，设置默认值
    private int var1 =10;

    private String var2;

    public void logLombok(){
        log.warn("lombok log warn:"+toString());
    }

}
```

生成的class文件
```class
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.alipay;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StudyLombok {
    private static final Logger log = LogManager.getLogger(StudyLombok.class);
    private int var1;
    private String var2;

    public void logLombok() {
        log.warn("lombok log warn:" + this.toString());
    }

    private static int $default$var1() {
        return 10;
    }

    public static StudyLombok.StudyLombokBuilder builder() {
        return new StudyLombok.StudyLombokBuilder();
    }

    public StudyLombok() {
        this.var1 = $default$var1();
    }

    public StudyLombok(int var1, String var2) {
        this.var1 = var1;
        this.var2 = var2;
    }

    public void setVar1(int var1) {
        this.var1 = var1;
    }

    public void setVar2(String var2) {
        this.var2 = var2;
    }

    public int getVar1() {
        return this.var1;
    }

    public String getVar2() {
        return this.var2;
    }

    public String toString() {
        return "StudyLombok(var1=" + this.getVar1() + ", var2=" + this.getVar2() + ")";
    }

    public static class StudyLombokBuilder {
        private boolean var1$set;
        private int var1$value;
        private String var2;

        StudyLombokBuilder() {
        }

        public StudyLombok.StudyLombokBuilder var1(int var1) {
            this.var1$value = var1;
            this.var1$set = true;
            return this;
        }

        public StudyLombok.StudyLombokBuilder var2(String var2) {
            this.var2 = var2;
            return this;
        }

        public StudyLombok build() {
            int var1$value = this.var1$value;
            if (!this.var1$set) {
                var1$value = StudyLombok.$default$var1();
            }

            return new StudyLombok(var1$value, this.var2);
        }

        public String toString() {
            return "StudyLombok.StudyLombokBuilder(var1$value=" + this.var1$value + ", var2=" + this.var2 + ")";
        }
    }
}

```
### 测试类

```java
    public static void main(String[] args) {

        //使用无参构造器创造对象，默认参数有指定
        StudyLombok studyLombok = StudyLombok.builder().build();
        studyLombok.logLombok();
        StudyLombok studyLombok1 = new StudyLombok();
        studyLombok1.logLombok();

        //使用全参构造器创造对象
        StudyLombok studyLombok2 = StudyLombok.builder().var1(111).var2("111").build();
        studyLombok2.logLombok();
        StudyLombok studyLombok3 = new StudyLombok(111,"111");
        studyLombok3.logLombok();

        //使用部分参数构造器创造对象，利用建造者模式
        StudyLombok studyLombok4 = StudyLombok.builder().var2("111").build();
        studyLombok4.logLombok();


     }
```
控制台日志输出
```
14:59:40.657 [main] WARN  com.alipay.StudyLombok - lombok log warn:StudyLombok(var1=10, var2=null)
14:59:40.662 [main] WARN  com.alipay.StudyLombok - lombok log warn:StudyLombok(var1=10, var2=null)
14:59:40.663 [main] WARN  com.alipay.StudyLombok - lombok log warn:StudyLombok(var1=111, var2=111)
14:59:40.663 [main] WARN  com.alipay.StudyLombok - lombok log warn:StudyLombok(var1=111, var2=111)
14:59:40.663 [main] WARN  com.alipay.StudyLombok - lombok log warn:StudyLombok(var1=10, var2=111)
```


### 注意事项
1、最好不要使用@Data，因为涉及到继承关系时，用起来比较复杂。
2、@NonNull : 如果给参数加个这个注解 参数为null会抛出空指针异常
3、@Builder.Default设置建造者模式下的默认值