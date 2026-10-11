spring:
  datasource:
    driver-class-name: ${r'${SMARTSERVICE_DB_DRIVER:com.mysql.cj.jdbc.Driver}'}
    url: ${r'${SMARTSERVICE_DB_URL}'}
    username: ${r'${SMARTSERVICE_DB_USERNAME}'}
    password: ${r'${SMARTSERVICE_DB_PASSWORD}'}

mybatis-plus:
  mapper-locations: classpath*:com/applet/mpinterface/mapper/**/*.xml
  type-aliases-package: com.applet.mpinterface.domain.pojo
  configuration:
    map-underscore-to-camel-case: true

server:
  port: ${portNum?c}

gp:
  rpc:
    servicePort: ${r'${SMARTSERVICE_RPC_PORT:20880}'}
    registerType: 0
    registryAddress: ${r'${SMARTSERVICE_REGISTRY_ADDRESS:127.0.0.1:2181}'}

management:
  endpoints:
    web:
      exposure:
        include: health
