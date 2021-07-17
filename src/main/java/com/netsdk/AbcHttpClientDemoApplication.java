package com.netsdk;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;

import javax.servlet.MultipartConfigElement;

/**
 * 启动类
 *
 * @author JustryDeng
 * @date 2019/9/18 17:52
 */
@SpringBootApplication
public class AbcHttpClientDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(AbcHttpClientDemoApplication.class, args);
	}

}
