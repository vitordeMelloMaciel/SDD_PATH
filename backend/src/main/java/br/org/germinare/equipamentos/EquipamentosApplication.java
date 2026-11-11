package br.org.germinare.equipamentos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import br.org.germinare.equipamentos.user.UserProvisioning;

@SpringBootApplication
public class EquipamentosApplication {
    public static void main(String[] args) {
        if (args.length == 1 && "--provision-user".equals(args[0])) {
            System.exit(provisionUser());
        }
        SpringApplication.run(EquipamentosApplication.class, args);
    }

    private static int provisionUser() {
        try (var context = new SpringApplicationBuilder(EquipamentosApplication.class)
            .web(WebApplicationType.NONE)
            .properties("spring.main.banner-mode=off", "logging.level.root=OFF")
            .run()) {
            return context.getBean(UserProvisioning.class).run(System.getenv(), System.out);
        } catch (RuntimeException exception) {
            System.err.println("Nao foi possivel iniciar o provisionamento. Confira a conexao e o schema.");
            return 1;
        }
    }
}
