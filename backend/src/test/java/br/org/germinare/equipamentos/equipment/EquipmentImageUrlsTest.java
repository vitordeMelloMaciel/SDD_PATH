package br.org.germinare.equipamentos.equipment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/** Spec 003, T006 · CA-04: composição da URL pública das fotos. */
class EquipmentImageUrlsTest {
    private static final String BASE = "https://projeto.supabase.co/storage/v1/object/public/fotos";

    @Test
    void ca04ComposesPublicUrlForEachOfTheTwelveKeys() {
        var urls = new EquipmentImageUrls(BASE);

        for (int index = 1; index <= 12; index++) {
            String asset = "TI-%04d".formatted(index);
            assertThat(urls.urlFor("equipment/" + asset + ".jpg"))
                .isEqualTo(BASE + "/equipment/" + asset + ".jpg");
        }
    }

    @Test
    void ca04ReturnsNullWithoutBaseUrl() {
        assertThat(new EquipmentImageUrls("").urlFor("equipment/TI-0001.jpg")).isNull();
        assertThat(new EquipmentImageUrls("   ").urlFor("equipment/TI-0001.jpg")).isNull();
        assertThat(new EquipmentImageUrls(null).urlFor("equipment/TI-0001.jpg")).isNull();
    }

    @Test
    void ca04ReturnsNullWithoutObjectKey() {
        var urls = new EquipmentImageUrls(BASE);

        assertThat(urls.urlFor(null)).isNull();
        assertThat(urls.urlFor("")).isNull();
    }

    @Test
    void ca04IgnoresTrailingSlashInBaseUrl() {
        assertThat(new EquipmentImageUrls(BASE + "/").urlFor("equipment/TI-0001.jpg"))
            .isEqualTo(BASE + "/equipment/TI-0001.jpg");
    }

    @Test
    void ca04ReadsBaseUrlFromEnvironmentVariable() {
        new ApplicationContextRunner()
            .withUserConfiguration(EquipmentImageUrls.class)
            .withPropertyValues("EQUIPMENT_IMAGE_BASE_URL=" + BASE)
            .run(context -> assertThat(context.getBean(EquipmentImageUrls.class).urlFor("equipment/TI-0012.jpg"))
                .isEqualTo(BASE + "/equipment/TI-0012.jpg"));

        new ApplicationContextRunner()
            .withUserConfiguration(EquipmentImageUrls.class)
            .run(context -> assertThat(context.getBean(EquipmentImageUrls.class).urlFor("equipment/TI-0012.jpg"))
                .isNull());
    }
}
