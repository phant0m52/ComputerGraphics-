package app.ui;

import app.image.ImageProcessorSwing;
import app.model.ModelRepository;
import engine.Mesh;
import engine.ModelInstance;
import engine.ObjLoader;
import math.Vec3;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

public class AppFrame extends JFrame {

    private final ModelRepository repo = new ModelRepository();

    private final DefaultListModel<String> modelsListModel = new DefaultListModel<>();
    private final JList<String> modelsList = new JList<>(modelsListModel);

    private final JLabel imageLabel = new JLabel("Открой изображение", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel("Готово");




    // панель для 3D модели
    private final ModelPanel modelPanel = new ModelPanel();

    // текущая модель + оригинал (чтобы можно было откатить)
    private ModelInstance currentModel;
    private Mesh originalMesh;

    // UI для трансформации модели
    private final JSlider trX = new JSlider(-500, 500, 0); // -5..5
    private final JSlider trY = new JSlider(-500, 500, 0);
    private final JSlider trZ = new JSlider(-500, 500, 0);

    private final JSlider rtX = new JSlider(-180, 180, 0); // градусы
    private final JSlider rtY = new JSlider(-180, 180, 0);
    private final JSlider rtZ = new JSlider(-180, 180, 0);

    private final JSlider scX = new JSlider(10, 500, 100); // 0.10..5.00
    private final JSlider scY = new JSlider(10, 500, 100);
    private final JSlider scZ = new JSlider(10, 500, 100);

    private final JLabel trVal = new JLabel("T: (0.00, 0.00, 0.00)");
    private final JLabel rtVal = new JLabel("R: (0°, 0°, 0°)");
    private final JLabel scVal = new JLabel("S: (1.00, 1.00, 1.00)");

    private final JButton btnResetTf = new JButton("Сбросить (Transform)");
    private final JButton btnBakeTf  = new JButton("Запечь (Bake)");
    private final JButton btnRestore = new JButton("Вернуть оригинал");

    private BufferedImage originalImage;
    private BufferedImage currentImage;



    public AppFrame() {
        super("Swing Image + Model Lab (Demo)");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildRightPanel(), BorderLayout.EAST);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        refreshModelsSafe();
        wireModelSelection();
        wireModelTransformUi();
        setModelTransformUiEnabled(false);
    }

    private void wireModelSelection() {
        modelsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        modelsList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            String sel = modelsList.getSelectedValue();
            if (sel != null) loadAndShowModel(sel);
        });
    }

    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setPreferredSize(new Dimension(280, 10));

        JLabel title = new JLabel("Модели");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        p.add(title, BorderLayout.NORTH);

        p.add(new JScrollPane(modelsList), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 8, 8));

        JButton btnImport = new JButton("Импорт");
        btnImport.addActionListener(e -> onImportModel());
        JButton btnDelete = new JButton("Удалить");
        btnDelete.addActionListener(e -> onDeleteModel());
        JButton btnDeletePart = new JButton("Удалить часть...");
        btnDeletePart.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Удаление части модели (по g/o в OBJ) делается после подключения ObjReader.",
                "Информация", JOptionPane.INFORMATION_MESSAGE));

        buttons.add(btnImport);
        buttons.add(btnDelete);
        buttons.add(btnDeletePart);

        JPanel imageBtns = new JPanel(new GridLayout(1, 2, 8, 8));
        JButton btnOpenImg = new JButton("Открыть");
        btnOpenImg.addActionListener(e -> onOpenImage());
        JButton btnSaveImg = new JButton("Сохранить");
        btnSaveImg.addActionListener(e -> onSaveImage());
        imageBtns.add(btnOpenImg);
        imageBtns.add(btnSaveImg);

        JPanel south = new JPanel(new BorderLayout(8, 8));
        south.add(buttons, BorderLayout.NORTH);
        south.add(new JSeparator(), BorderLayout.CENTER);
        south.add(imageBtns, BorderLayout.SOUTH);

        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildCenterPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Левая часть: картинка
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(245, 245, 245));
        imageLabel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        JScrollPane imgScroll = new JScrollPane(imageLabel);

        // Правая часть: модель
        JPanel modelWrap = new JPanel(new BorderLayout());
        modelWrap.add(modelPanel, BorderLayout.CENTER);
        modelWrap.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imgScroll, modelWrap);
        split.setResizeWeight(0.6);

        p.add(split, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildRightPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setPreferredSize(new Dimension(340, 10));

        // -------- Image effects --------
        JLabel title = new JLabel("Преобразования изображения");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        p.add(title);
        p.add(Box.createVerticalStrut(10));


        p.add(Box.createVerticalStrut(10));



        p.add(Box.createVerticalStrut(10));


        p.add(Box.createVerticalStrut(14));


        p.add(Box.createVerticalStrut(14));
        p.add(new JSeparator());
        p.add(Box.createVerticalStrut(14));

        // -------- Model transform --------
        JLabel mt = new JLabel("Трансформация модели (Math)");
        mt.setFont(mt.getFont().deriveFont(Font.BOLD));
        p.add(mt);
        p.add(Box.createVerticalStrut(8));

        p.add(buildTransformPanel());
        p.add(Box.createVerticalStrut(10));

        JLabel hint = new JLabel("<html><body style='width:300px'>"
                + "Камера: ПКМ+мышь — поворот, WASD — движение, Space/Ctrl — вверх/вниз, Shift — быстрее.<br>"
                + "<b>Bake</b> «запекает» Transform в вершины (local→world), а Transform сбрасывает."
                + "</body></html>");
        hint.setForeground(new Color(90, 90, 90));
        p.add(hint);

        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildTransformPanel() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new TitledBorder("Позиция / Поворот / Масштаб"));

        // настройка tick'ов
        setupSlider(trX, 250);
        setupSlider(trY, 250);
        setupSlider(trZ, 250);

        setupSlider(rtX, 90);
        setupSlider(rtY, 90);
        setupSlider(rtZ, 90);

        setupSlider(scX, 100);
        setupSlider(scY, 100);
        setupSlider(scZ, 100);

        box.add(trVal);
        box.add(row("Tx", trX));
        box.add(row("Ty", trY));
        box.add(row("Tz", trZ));

        box.add(Box.createVerticalStrut(8));
        box.add(rtVal);
        box.add(row("Rx", rtX));
        box.add(row("Ry", rtY));
        box.add(row("Rz", rtZ));

        box.add(Box.createVerticalStrut(8));
        box.add(scVal);
        box.add(row("Sx", scX));
        box.add(row("Sy", scY));
        box.add(row("Sz", scZ));

        box.add(Box.createVerticalStrut(10));

        JPanel btns = new JPanel(new GridLayout(0, 1, 8, 8));
        btns.add(btnResetTf);
        btns.add(btnBakeTf);
        btns.add(btnRestore);
        box.add(btns);

        return box;
    }

    private static JPanel row(String name, JSlider s) {
        JPanel r = new JPanel(new BorderLayout(8, 0));
        r.add(new JLabel(name), BorderLayout.WEST);
        r.add(s, BorderLayout.CENTER);
        return r;
    }

    private static void setupSlider(JSlider s, int majorTick) {
        s.setMajorTickSpacing(majorTick);
        s.setPaintTicks(true);
    }

    private JPanel buildBottomPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(8, 10, 8, 10));
        p.add(statusLabel, BorderLayout.CENTER);
        return p;
    }


    private void wireModelTransformUi() {
        var changeListener = (javax.swing.event.ChangeListener) e -> applyTransformFromUi();

        trX.addChangeListener(changeListener);
        trY.addChangeListener(changeListener);
        trZ.addChangeListener(changeListener);

        rtX.addChangeListener(changeListener);
        rtY.addChangeListener(changeListener);
        rtZ.addChangeListener(changeListener);

        scX.addChangeListener(changeListener);
        scY.addChangeListener(changeListener);
        scZ.addChangeListener(changeListener);

        btnResetTf.addActionListener(e -> {
            if (currentModel == null) return;
            currentModel.getTransform().reset();
            syncUiFromTransform();
            modelPanel.repaint();
            status("Transform сброшен");
        });

        btnBakeTf.addActionListener(e -> {
            if (currentModel == null) return;
            // запекаем в геометрию (local->world), Transform сбрасывается
            currentModel = currentModel.baked();
            modelPanel.setInstance(currentModel);
            syncUiFromTransform();
            status("Transform запечён (Bake)");
        });

        btnRestore.addActionListener(e -> {
            if (originalMesh == null) return;
            currentModel = new ModelInstance(originalMesh);
            modelPanel.setInstance(currentModel);
            syncUiFromTransform();
            status("Оригинальная модель восстановлена");
        });
    }

    private void setModelTransformUiEnabled(boolean enabled) {
        trX.setEnabled(enabled); trY.setEnabled(enabled); trZ.setEnabled(enabled);
        rtX.setEnabled(enabled); rtY.setEnabled(enabled); rtZ.setEnabled(enabled);
        scX.setEnabled(enabled); scY.setEnabled(enabled); scZ.setEnabled(enabled);
        btnResetTf.setEnabled(enabled);
        btnBakeTf.setEnabled(enabled);
        btnRestore.setEnabled(enabled);
    }

    private void syncUiFromTransform() {
        if (currentModel == null) return;
        var t = currentModel.getTransform();

        trX.setValue((int) Math.round(t.getPosition().x * 100.0));
        trY.setValue((int) Math.round(t.getPosition().y * 100.0));
        trZ.setValue((int) Math.round(t.getPosition().z * 100.0));

        rtX.setValue((int) Math.round(Math.toDegrees(t.getRotation().x)));
        rtY.setValue((int) Math.round(Math.toDegrees(t.getRotation().y)));
        rtZ.setValue((int) Math.round(Math.toDegrees(t.getRotation().z)));

        scX.setValue((int) Math.round(t.getScale().x * 100.0));
        scY.setValue((int) Math.round(t.getScale().y * 100.0));
        scZ.setValue((int) Math.round(t.getScale().z * 100.0));

        updateTransformLabels();
    }

    private void applyTransformFromUi() {
        if (currentModel == null) {
            updateTransformLabels();
            return;
        }

        double px = trX.getValue() / 100.0;
        double py = trY.getValue() / 100.0;
        double pz = trZ.getValue() / 100.0;

        double rx = Math.toRadians(rtX.getValue());
        double ry = Math.toRadians(rtY.getValue());
        double rz = Math.toRadians(rtZ.getValue());

        double sx = Math.max(0.01, scX.getValue() / 100.0);
        double sy = Math.max(0.01, scY.getValue() / 100.0);
        double sz = Math.max(0.01, scZ.getValue() / 100.0);

        currentModel.getTransform().setPosition(new Vec3(px, py, pz));
        currentModel.getTransform().setRotation(new Vec3(rx, ry, rz));
        currentModel.getTransform().setScale(new Vec3(sx, sy, sz));

        updateTransformLabels();
        modelPanel.repaint();
    }

    private void updateTransformLabels() {
        trVal.setText(String.format("T: (%.2f, %.2f, %.2f)",
                trX.getValue()/100.0, trY.getValue()/100.0, trZ.getValue()/100.0));
        rtVal.setText(String.format("R: (%d°, %d°, %d°)",
                rtX.getValue(), rtY.getValue(), rtZ.getValue()));
        scVal.setText(String.format("S: (%.2f, %.2f, %.2f)",
                scX.getValue()/100.0, scY.getValue()/100.0, scZ.getValue()/100.0));
    }

    private void refreshModelsSafe() {
        try {
            modelsListModel.clear();
            for (String m : repo.listModels()) modelsListModel.addElement(m);
        } catch (Exception ex) {
            showError("Не удалось загрузить список моделей", ex);
        }
    }

    private void onImportModel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Выберите OBJ файл модели");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("OBJ", "obj"));

        int res = fc.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File f = fc.getSelectedFile();
        try {
            String name = repo.importModel(Path.of(f.getAbsolutePath()));
            refreshModelsSafe();
            modelsList.setSelectedValue(name, true);
            status("Импортировано: " + name);
        } catch (Exception ex) {
            showError("Ошибка импорта модели", ex);
        }
    }

    private void onDeleteModel() {
        String sel = modelsList.getSelectedValue();
        if (sel == null) return;

        int ok = JOptionPane.showConfirmDialog(this,
                "Удалить модель '" + sel + "'?",
                "Подтверждение",
                JOptionPane.YES_NO_OPTION);

        if (ok != JOptionPane.YES_OPTION) return;

        try {
            repo.deleteModel(sel);
            refreshModelsSafe();
            modelPanel.setInstance(null);
            currentModel = null;
            originalMesh = null;
            setModelTransformUiEnabled(false);
            status("Удалено: " + sel);
        } catch (Exception ex) {
            showError("Ошибка удаления модели", ex);
        }
    }

    private void onOpenImage() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Открыть изображение");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Images", "png", "jpg", "jpeg"));

        int res = fc.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        try {
            File f = fc.getSelectedFile();
            originalImage = ImageIO.read(f);
            if (originalImage == null) throw new IllegalArgumentException("Невозможно прочитать изображение");
            currentImage = originalImage;
            renderImage(currentImage);
            status("Изображение загружено: " + f.getName());
        } catch (Exception ex) {
            showError("Ошибка открытия изображения", ex);
        }
    }

    private void onSaveImage() {
        if (currentImage == null) return;

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Сохранить изображение");
        fc.setSelectedFile(new File("output.png"));

        int res = fc.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        try {
            File f = fc.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".png")) {
                f = new File(f.getParentFile(), f.getName() + ".png");
            }
            ImageIO.write(currentImage, "png", f);
            status("Сохранено: " + f.getName());
        } catch (Exception ex) {
            showError("Ошибка сохранения", ex);
        }
    }





    private void loadAndShowModel(String modelName) {
        try {
            Path dir = Path.of("models").resolve(modelName);

            Path obj;
            try (var s = Files.list(dir)) {
                obj = s.filter(p -> p.toString().toLowerCase().endsWith(".obj"))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("В папке модели нет .obj"));
            }

            var mesh = ObjLoader.load(obj);

            // запоминаем оригинал, чтобы можно было "Вернуть оригинал"
            originalMesh = mesh;

            currentModel = new ModelInstance(mesh);
            modelPanel.setInstance(currentModel);

            setModelTransformUiEnabled(true);
            syncUiFromTransform();

            status("Показана модель: " + modelName);

        } catch (Exception ex) {
            showError("Не удалось открыть модель: " + modelName, ex);
        }
    }

    private void renderImage(BufferedImage img) {
        if (img == null) {
            imageLabel.setIcon(null);
            imageLabel.setText("Открой изображение");
            return;
        }
        imageLabel.setText(null);

        int maxW = Math.max(1, imageLabel.getWidth());
        int maxH = Math.max(1, imageLabel.getHeight());

        if (maxW < 50 || maxH < 50) {
            maxW = 800;
            maxH = 600;
        }

        Image scaled = getScaledToFit(img, maxW - 20, maxH - 20);
        imageLabel.setIcon(new ImageIcon(scaled));
    }

    private static Image getScaledToFit(BufferedImage img, int maxW, int maxH) {
        double scale = Math.min((double) maxW / img.getWidth(), (double) maxH / img.getHeight());
        scale = Math.min(1.0, Math.max(0.05, scale));
        int w = (int) Math.round(img.getWidth() * scale);
        int h = (int) Math.round(img.getHeight() * scale);
        return img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }

    private void status(String s) { statusLabel.setText(s); }

    private void showError(String msg, Exception ex) {
        status(msg + ": " + ex.getMessage());
        JOptionPane.showMessageDialog(this,
                msg + "\n\n" + ex.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
    }

    private static BufferedImage deepCopy(BufferedImage bi) {
        if (bi == null) return null;
        BufferedImage copy = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = copy.createGraphics();
        g2.drawImage(bi, 0, 0, null);
        g2.dispose();
        return copy;
    }
}
