package ru.mephi.vikingdemo.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.mephi.vikingdemo.model.EquipmentItem;
import ru.mephi.vikingdemo.model.EquipmentItemEntity;
import ru.mephi.vikingdemo.model.Viking;
import ru.mephi.vikingdemo.model.VikingEntity;


@Repository
public class VikingStorage {

    private final VikingRepository vikingRepository;
    private final EquipmentItemRepository equipmentItemRepository;
    private final VikingMapper vikingMapper;

    public VikingStorage(
            VikingRepository vikingRepository,
            EquipmentItemRepository equipmentItemRepository,
            VikingMapper vikingMapper
    ) {
        this.vikingRepository = vikingRepository;
        this.equipmentItemRepository = equipmentItemRepository;
        this.vikingMapper = vikingMapper;
    }

    @Transactional
    public Viking save(Viking viking) {
        Integer vikingId = vikingRepository.save(
                vikingMapper.toVikingEntity(viking)
        );

        List<EquipmentItem> equipment = normalizeEquipment(viking);
        for (EquipmentItem item : equipment) {
            equipmentItemRepository.save(
                    vikingMapper.toEquipmentItemEntity(vikingId, item)
            );
        }

        return new Viking(
                vikingId,
                viking.name(),
                viking.age(),
                viking.heightCm(),
                viking.hairColor(),
                viking.beardStyle(),
                equipment
        );
    }

    public List<Viking> findAll() {
        List<VikingEntity> vikingEntities = vikingRepository.findAll();
        List<EquipmentItemEntity> equipmentEntities = equipmentItemRepository.findAll();

        Map<Integer, List<EquipmentItemEntity>> equipmentByVikingId = equipmentEntities.stream()
                .collect(Collectors.groupingBy(EquipmentItemEntity::vikingId));

        return vikingEntities.stream()
                .map(vikingEntity -> vikingMapper.toViking(
                        vikingEntity,
                        equipmentByVikingId.getOrDefault(vikingEntity.id(), List.of())
                ))
                .toList();
    }

    public Optional<Viking> findById(int id) {
        return vikingRepository.findById(id)
                .map(vikingEntity -> vikingMapper.toViking(
                        vikingEntity,
                        equipmentItemRepository.findByVikingId(id)
                ));
    }

    @Transactional
    public boolean deleteById(int id) {
        return vikingRepository.deleteById(id);
    }

    @Transactional
    public Optional<Viking> update(int id, Viking viking) {
        if (vikingRepository.findById(id).isEmpty()) {
            return Optional.empty();
        }

        List<EquipmentItem> equipment = normalizeEquipment(viking);
        Viking vikingWithId = new Viking(
                id,
                viking.name(),
                viking.age(),
                viking.heightCm(),
                viking.hairColor(),
                viking.beardStyle(),
                equipment
        );

        vikingRepository.update(vikingMapper.toVikingEntity(vikingWithId));
        equipmentItemRepository.deleteByVikingId(id);

        for (EquipmentItem item : equipment) {
            equipmentItemRepository.save(
                    vikingMapper.toEquipmentItemEntity(id, item)
            );
        }

        return Optional.of(vikingWithId);
    }

    private List<EquipmentItem> normalizeEquipment(Viking viking) {
        if (viking.equipment() == null) {
            return List.of();
        }

        return viking.equipment();
    }
}
