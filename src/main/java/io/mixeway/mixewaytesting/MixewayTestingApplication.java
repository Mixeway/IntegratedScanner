package io.mixeway.mixewaytesting;

import io.mixeway.mixewaytesting.testing.Testing;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@SpringBootApplication
public class MixewayTestingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MixewayTestingApplication.class, args);
    }

}
@Component
class MixewayTestingApplicationRunner implements CommandLineRunner {
    private final Testing testing;

    public MixewayTestingApplicationRunner(Testing testing){
        this.testing = testing;
    }

    @Override
    public void run(String...args) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, InterruptedException {
        testing.performTest();

    }
}