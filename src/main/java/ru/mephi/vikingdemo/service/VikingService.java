package ru.mephi.vikingdemo.service;

import org.springframework.stereotype.Service;
import ru.mephi.vikingdemo.model.Viking;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import ru.mephi.vikingdemo.repository.VikingStorage;

@Service
public class VikingService {
    // каждый раз при изменении создаётся новая копия списка 

    private final VikingFactory vikingFactory;
    private final VikingStorage vikingStorage;
    
    
    @Autowired
    public VikingService(
            VikingFactory vikingFactory,
            VikingStorage vikingStorage
    ) {
        this.vikingFactory = vikingFactory;
        this.vikingStorage = vikingStorage;
    }
    
    public List<Viking> findAll() {
        return vikingStorage.findAll();
    }

    public Viking create(Viking viking) {
        return vikingStorage.save(viking);
    }

    public Viking createRandomViking() {
        Viking viking = vikingFactory.createRandomViking();
        return vikingStorage.save(viking);
    }

    public boolean deleteById(int id) {
        return vikingStorage.deleteById(id);
    }

    public Optional<Viking> update(int id, Viking viking) {
        return vikingStorage.update(id, viking);
    }
}
