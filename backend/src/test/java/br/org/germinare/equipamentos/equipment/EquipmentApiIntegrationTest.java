package br.org.germinare.equipamentos.equipment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import br.org.germinare.equipamentos.ApiIntegrationTestSupport;

/** Spec 004, T001 · CA-07 e CA-01 (API): catálogo com seis campos públicos. */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "EQUIPMENT_IMAGE_BASE_URL=https://projeto.supabase.co/storage/v1/object/public/fotos"
)
class EquipmentApiIntegrationTest extends ApiIntegrationTestSupport {
    private static final String BASE = "https://projeto.supabase.co/storage/v1/object/public/fotos";
    private static final List<String> FIELDS = List.of("id", "assetCode", "name", "category", "status", "imageUrl");

    @BeforeEach
    void setUp() {
        resetData();
    }

    @Test
    void ca01ListsTheWholeCatalogByAssetCodeWithSixFieldsAndPhotoUrl() {
        createUser("a@example.org", "Pessoa A", "colaborador");
        String session = login("a@example.org");

        var response = get("/api/equipment", session, Map[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> items = items(response.getBody());
        assertThat(items).hasSize(12);
        for (int index = 0; index < 12; index++) {
            String asset = "TI-%04d".formatted(index + 1);
            assertThat(items.get(index).keySet()).containsExactlyInAnyOrderElementsOf(FIELDS);
            assertThat(items.get(index).get("assetCode")).isEqualTo(asset);
            assertThat(items.get(index).get("imageUrl")).isEqualTo(BASE + "/equipment/" + asset + ".jpg");
        }
        assertThat(items).extracting(item -> item.get("status"))
            .filteredOn("manutencao"::equals).hasSize(3);
        assertThat(items).extracting(item -> item.get("status"))
            .filteredOn("disponivel"::equals).hasSize(9);
    }

    @Test
    void ca01ImageUrlIsNullWhenTheItemHasNoPhotoKey() {
        jdbc.update("update equipment set image_object_key = null where asset_code = 'TI-0003'");
        try {
            createUser("a@example.org", "Pessoa A", "colaborador");
            var items = items(get("/api/equipment", login("a@example.org"), Map[].class).getBody());

            assertThat(items.get(2).get("assetCode")).isEqualTo("TI-0003");
            assertThat(items.get(2)).containsEntry("imageUrl", null);
        } finally {
            jdbc.update("update equipment set image_object_key = 'equipment/TI-0003.jpg' where asset_code = 'TI-0003'");
        }
    }

    @Test
    void ca07BorrowedItemIsTheSameObjectForTheHolderAndForAnotherAccount() {
        UUID holderId = createUser("titular@example.org", "Pessoa Titular", "colaborador");
        createUser("outra@example.org", "Outra Pessoa", "colaborador");
        UUID equipment = equipmentId("TI-0001");
        UUID loanId = jdbc.queryForObject(
            "insert into loan (borrower_id, equipment_id) values (?, ?) returning id", UUID.class, holderId, equipment);
        var dates = jdbc.queryForMap("select loaned_at, due_at from loan where id = ?", loanId);

        String holderSession = login("titular@example.org");
        String otherSession = login("outra@example.org");
        var holderResponse = get("/api/equipment", holderSession, String.class);
        var otherResponse = get("/api/equipment", otherSession, String.class);
        Map<String, Object> holderItem = items(get("/api/equipment", holderSession, Map[].class).getBody()).get(0);
        Map<String, Object> otherItem = items(get("/api/equipment", otherSession, Map[].class).getBody()).get(0);

        assertThat(holderItem).isEqualTo(otherItem);
        assertThat(holderItem.keySet()).containsExactlyInAnyOrderElementsOf(FIELDS);
        assertThat(holderItem).containsEntry("assetCode", "TI-0001").containsEntry("status", "emprestado");
        assertThat(holderResponse.getBody()).isEqualTo(otherResponse.getBody());
        for (String body : List.of(holderResponse.getBody(), otherResponse.getBody())) {
            assertThat(body).doesNotContain(
                "titular@example.org", "Pessoa Titular", holderId.toString(), loanId.toString(),
                datePart(dates.get("loaned_at")), datePart(dates.get("due_at")));
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> items(Map<?, ?>[] body) {
        return java.util.Arrays.stream(body).map(item -> (Map<String, Object>) item).toList();
    }

    private static String datePart(Object value) {
        return value.toString().substring(0, 10);
    }
}
