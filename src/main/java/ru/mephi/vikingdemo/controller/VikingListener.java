/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ru.mephi.vikingdemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mephi.vikingdemo.gui.VikingDesktopFrame;
import ru.mephi.vikingdemo.model.Viking;
import ru.mephi.vikingdemo.service.VikingService;

import javax.swing.SwingUtilities;

/**
 *
 * @author test2023
 */
@Component
public class VikingListener {
    private final VikingService service;
    private VikingDesktopFrame gui;

    @Autowired
    public VikingListener(VikingService service) {
        this.service = service;
    }
    
    public void setGui(VikingDesktopFrame gui){
        this.gui = gui;
    }

    public Viking createRandomViking() {
        Viking viking = service.createRandomViking();
        addViking(viking);
        return viking;
    }

    public void addViking(Viking viking) {
        if (gui == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> gui.addNewViking(viking));
    }

    public void removeViking(int id) {
        if (gui == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> gui.removeViking(id));
    }

    public void updateViking(Viking viking) {
        if (gui == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> gui.updateViking(viking));
    }

    void testAdd() {
        createRandomViking();
    }
}
