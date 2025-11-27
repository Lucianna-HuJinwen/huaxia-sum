# 指定了基础镜像为openjdk:17.0.2，即使⽤OpenJDK 17.0.2版本的Java环境作为构建的基础。
FROM openjdk:17.0.2

# 拷⻉jar包到容器中
ADD ./huaxia-gateway.jar ./huaxia-gateway.jar

# 运⾏jar包 ⽤ Java 运⾏容器内的 huaxia-gateway.jar 应⽤程序
CMD ["java", "-jar", "huaxia-gateway.jar"]