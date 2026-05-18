package ru.mephi.vikingdemo.gui;

import ru.mephi.vikingdemo.model.BeardStyle;
import ru.mephi.vikingdemo.model.EquipmentItem;
import ru.mephi.vikingdemo.model.HairColor;
import ru.mephi.vikingdemo.model.Viking;
import ru.mephi.vikingdemo.service.VikingService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class VikingDesktopFrame extends JFrame {

    private final VikingService vikingService;
    private final VikingTableModel tableModel = new VikingTableModel();
    private final JTable vikingTable = new JTable(tableModel);

    public VikingDesktopFrame(VikingService vikingService) {
        this.vikingService = vikingService;

        setTitle("Viking Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(new Dimension(1000, 420));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel header = new JLabel("Viking Demo", SwingConstants.CENTER);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 18f));
        add(header, BorderLayout.NORTH);

        vikingTable.setRowHeight(28);
        add(new JScrollPane(vikingTable), BorderLayout.CENTER);

        JButton createButton = new JButton("Create random viking");
        createButton.addActionListener(event -> onCreateViking());
        JButton addButton = new JButton("Add viking");
        addButton.addActionListener(event -> onAddViking());
        JButton updateButton = new JButton("Update selected");
        updateButton.addActionListener(event -> onUpdateSelectedViking());
        JButton deleteButton = new JButton("Delete selected");
        deleteButton.addActionListener(event -> onDeleteSelectedViking());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(createButton);
        bottomPanel.add(addButton);
        bottomPanel.add(updateButton);
        bottomPanel.add(deleteButton);
        add(bottomPanel, BorderLayout.SOUTH);
        
        onInit();
    }

    private void onCreateViking() {
        Viking viking = vikingService.createRandomViking();
        tableModel.addViking(viking);
    }

    private void onAddViking() {
        showVikingDialog(null).ifPresent(viking -> {
            Viking created = vikingService.create(viking);
            tableModel.addViking(created);
        });
    }

    private void onUpdateSelectedViking() {
        Viking selected = getSelectedViking();
        if (selected == null) {
            return;
        }

        showVikingDialog(selected).ifPresent(viking -> vikingService.update(selected.id(), viking)
                .ifPresentOrElse(
                        tableModel::updateViking,
                        () -> showMessage("Selected viking was not found")
                ));
    }

    private void onDeleteSelectedViking() {
        Viking selected = getSelectedViking();
        if (selected == null) {
            return;
        }

        boolean deleted = vikingService.deleteById(selected.id());
        if (deleted) {
            tableModel.removeVikingById(selected.id());
        } else {
            showMessage("Selected viking was not found");
        }
    }

    private Viking getSelectedViking() {
        int selectedRow = vikingTable.getSelectedRow();
        if (selectedRow < 0) {
            showMessage("Select a viking in the table");
            return null;
        }

        int modelRow = vikingTable.convertRowIndexToModel(selectedRow);
        return tableModel.getVikingAt(modelRow);
    }

    private Optional<Viking> showVikingDialog(Viking source) {
        VikingForm form = new VikingForm(source);

        while (true) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    form.panel(),
                    source == null ? "Add viking" : "Update viking",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return Optional.empty();
            }

            try {
                return Optional.of(form.toViking(source == null ? null : source.id()));
            } catch (IllegalArgumentException exception) {
                showMessage(exception.getMessage());
            }
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
    
    public void addNewViking(Viking viking){
        tableModel.addViking(viking);
    }

    public void removeViking(int id) {
        tableModel.removeVikingById(id);
    }

    public void updateViking(Viking viking) {
        tableModel.updateViking(viking);
    }

    private void onInit() {
        List<Viking> all = vikingService.findAll();
        if (!all.isEmpty()){
            for (Viking viking : all) {
                tableModel.addViking(viking);
            }
        }
    }

    private static class VikingForm {

        private final JPanel panel = new JPanel(new GridBagLayout());
        private final JTextField nameField = new JTextField(18);
        private final JTextField ageField = new JTextField(18);
        private final JTextField heightField = new JTextField(18);
        private final JComboBox<HairColor> hairColorBox = new JComboBox<>(HairColor.values());
        private final JComboBox<BeardStyle> beardStyleBox = new JComboBox<>(BeardStyle.values());
        private final JTextArea equipmentArea = new JTextArea(5, 24);

        VikingForm(Viking viking) {
            if (viking == null) {
                ageField.setText("30");
                heightField.setText("180");
            } else {
                nameField.setText(viking.name());
                ageField.setText(String.valueOf(viking.age()));
                heightField.setText(String.valueOf(viking.heightCm()));
                hairColorBox.setSelectedItem(viking.hairColor());
                beardStyleBox.setSelectedItem(viking.beardStyle());
                equipmentArea.setText(equipmentToText(viking.equipment()));
            }

            addRow(0, "Name", nameField);
            addRow(1, "Age", ageField);
            addRow(2, "Height (cm)", heightField);
            addRow(3, "Hair color", hairColorBox);
            addRow(4, "Beard style", beardStyleBox);
            addRow(5, "Equipment", new JScrollPane(equipmentArea));
        }

        JPanel panel() {
            return panel;
        }

        Viking toViking(Integer id) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }

            int age = parsePositiveInt(ageField.getText(), "Age");
            int height = parsePositiveInt(heightField.getText(), "Height");

            return new Viking(
                    id,
                    name,
                    age,
                    height,
                    (HairColor) hairColorBox.getSelectedItem(),
                    (BeardStyle) beardStyleBox.getSelectedItem(),
                    parseEquipment(equipmentArea.getText())
            );
        }

        private void addRow(int row, String label, java.awt.Component component) {
            GridBagConstraints labelConstraints = new GridBagConstraints();
            labelConstraints.gridx = 0;
            labelConstraints.gridy = row;
            labelConstraints.anchor = GridBagConstraints.WEST;
            labelConstraints.insets = new Insets(4, 4, 4, 10);
            panel.add(new JLabel(label), labelConstraints);

            GridBagConstraints fieldConstraints = new GridBagConstraints();
            fieldConstraints.gridx = 1;
            fieldConstraints.gridy = row;
            fieldConstraints.weightx = 1.0;
            fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
            fieldConstraints.insets = new Insets(4, 4, 4, 4);
            panel.add(component, fieldConstraints);
        }

        private static int parsePositiveInt(String value, String fieldName) {
            try {
                int number = Integer.parseInt(value.trim());
                if (number <= 0) {
                    throw new NumberFormatException();
                }

                return number;
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException(fieldName + " must be a positive number");
            }
        }

        private static List<EquipmentItem> parseEquipment(String text) {
            String[] lines = text.split("\\R");
            List<EquipmentItem> equipment = new ArrayList<>();

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                String[] parts = trimmed.split(",", 2);
                if (parts.length < 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
                    throw new IllegalArgumentException("Equipment lines must use: name, quality");
                }

                equipment.add(new EquipmentItem(parts[0].trim(), parts[1].trim()));
            }

            return equipment;
        }

        private static String equipmentToText(List<EquipmentItem> equipment) {
            if (equipment == null) {
                return "";
            }

            return equipment.stream()
                    .map(item -> item.name() + ", " + item.quality())
                    .collect(Collectors.joining(System.lineSeparator()));
        }
    }
}
