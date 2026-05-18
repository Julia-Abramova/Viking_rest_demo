
package ru.mephi.vikingdemo.service;

import java.util.List;
import java.util.Random;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import ru.mephi.vikingdemo.model.BeardStyle;
import ru.mephi.vikingdemo.model.EquipmentItem;
import ru.mephi.vikingdemo.model.HairColor;
import ru.mephi.vikingdemo.model.Viking;
import java.util.Locale;

@Component
public class VikingFactory {

    private final Faker faker = new Faker(Locale.of("nor"));
    private final Random random = new Random();

    public Viking createRandomViking() {
        return new Viking(
                null,
                faker.name().firstName(),
                18 + random.nextInt(43),
                160 + random.nextInt(41),
                HairColor.values()[random.nextInt(HairColor.values().length)],
                BeardStyle.values()[random.nextInt(BeardStyle.values().length)],
                createRandomEquipment()
        );
    }

    private List<EquipmentItem> createRandomEquipment() {
        return List.of(
                EquipmentFactory.createItem(),
                EquipmentFactory.createItem()
        );
    }
}
