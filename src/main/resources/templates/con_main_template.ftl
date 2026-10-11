package com.${packageName}.mpinterface;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"spring.reference","com.${packageName}.mpinterface.controller","spring.annotation"})

public class MpInterfaceApplication {

public static void main(String[] args) {
SpringApplication.run(MpInterfaceApplication.class, args);
}

}
