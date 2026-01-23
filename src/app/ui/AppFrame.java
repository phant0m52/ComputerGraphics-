package app.ui;

import app.image.ImageProcessorSwing;
import app.model.ModelRepository;
import engine.Camera;
import engine.Mesh;
import engine.ModelInstance;
import engine.ObjLoader;
import engine.Texture;
import math.Vec3;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AppFrame extends JFrame {

    private final ModelRepository repo = new ModelRepository();

    private final DefaultListModel<String> modelsListModel = new DefaultListModel<>();
    private final JList<String> modelsList = new JList<>(modelsListModel);

    private final JLabel imageLabel = new JLabel("Открой изображение", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel("Готово");

    // ---- NEW: render mode UI ----
    private final JCheckBox cbWireframe = new JCheckBox("Рисовать полигональную сетку", true);
    private final JCheckBox cbTexture   = new JCheckBox("Использовать текстуру", false);
    private final JCheckBox cbLighting  = new JCheckBox("Использовать освещение", false);

    private final JButton btnPickColor   = new JButton("Выбрать цвет модели...");
    private final JButton btnLoadTexture = new JButton("Загрузить текстуру...");

    // ---- NEW: cameras UI ----
    private final DefaultListModel<String> camerasListModel = new DefaultListModel<>();
    private final JList<String> camerasList = new JList<>(camerasListModel);
    private final JButton btnAddCamera = new JButton("+ Камера");
    private final JButton btnRemoveCamera = new JButton("- Камера");

    // панель для 3D модели (у тебя она уже есть, но мы её расширили)
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
        super("Computer Graphics — Software Rasterizer");
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
        wireRenderSettingsUi();
        wireCameraUi();

        refreshCamerasUi();
        setControlsEnabled(false);
    }

    // ========================= LEFT =========================

    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setPreferredSize(new Dimension(280, 10));

        JLabel title = new JLabel("Модели (models/<name>/file.obj)");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        p.add(title, BorderLayout.NORTH);

        p.add(new JScrollPane(modelsList), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 8, 8));

        JButton btnImport = new JButton("Импорт OBJ");
        btnImport.addActionListener(e -> onImportModel());

        JButton btnDelete = new JButton("Удалить");
        btnDelete.addActionListener(e -> onDeleteModel());

        buttons.add(btnImport);
        buttons.add(btnDelete);

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

    // ========================= CENTER =========================

    private JPanel buildCenterPanel() {
        JPanel p = new JPanel(new GridLayout(1, 2, 10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel imgPanel = new JPanel(new BorderLayout());
        imgPanel.setBorder(new TitledBorder("Изображение"));
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(25, 25, 25));
        imageLabel.setForeground(new Color(200, 200, 200));
        imgPanel.add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        JPanel modelWrap = new JPanel(new BorderLayout());
        modelWrap.setBorder(new TitledBorder("3D"));
        modelWrap.add(modelPanel, BorderLayout.CENTER);

        p.add(imgPanel);
        p.add(modelWrap);
        return p;
    }

    // ========================= RIGHT =========================

    private JPanel buildRightPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setPreferredSize(new Dimension(360, 10));

        // -------- Transform --------
        JLabel mt = new JLabel("Трансформация модели (Math)");
        mt.setFont(mt.getFont().deriveFont(Font.BOLD));
        p.add(mt);
        p.add(Box.createVerticalStrut(8));
        p.add(buildTransformPanel());

        p.add(Box.createVerticalStrut(12));
        p.add(new JSeparator());
        p.add(Box.createVerticalStrut(12));

        // -------- Render modes --------
        JLabel rm = new JLabel("Режимы отрисовки");
        rm.setFont(rm.getFont().deriveFont(Font.BOLD));
        p.add(rm);
        p.add(Box.createVerticalStrut(6));

        JPanel modes = new JPanel();
        modes.setLayout(new BoxLayout(modes, BoxLayout.Y_AXIS));
        modes.setBorder(new TitledBorder("Опции"));
        modes.add(cbWireframe);
        modes.add(cbTexture);
        modes.add(cbLighting);
        modes.add(Box.createVerticalStrut(6));
        modes.add(btnPickColor);
        modes.add(Box.createVerticalStrut(4));
        modes.add(btnLoadTexture);

        p.add(modes);

        p.add(Box.createVerticalStrut(12));
        p.add(new JSeparator());
        p.add(Box.createVerticalStrut(12));

        // -------- Cameras --------
        JLabel cm = new JLabel("Камеры");
        cm.setFont(cm.getFont().deriveFont(Font.BOLD));
        p.add(cm);
        p.add(Box.createVerticalStrut(6));

        JPanel camBox = new JPanel(new BorderLayout(6, 6));
        camBox.setBorder(new TitledBorder("Список камер"));
        camerasList.setVisibleRowCount(4);
        camBox.add(new JScrollPane(camerasList), BorderLayout.CENTER);

        JPanel camBtns = new JPanel(new GridLayout(1, 2, 6, 6));
        camBtns.add(btnAddCamera);
        camBtns.add(btnRemoveCamera);
        camBox.add(camBtns, BorderLayout.SOUTH);

        p.add(camBox);

        p.add(Box.createVerticalStrut(12));
        JLabel hint = new JLabel("<html><body style='width:320px;color:#666'>"
                + "Камера: ПКМ+мышь — поворот, WASD — движение, Space/Ctrl — вверх/вниз, Shift — быстрее."
                + "</body></html>");
        p.add(hint);

        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildTransformPanel() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new TitledBorder("Позиция / Поворот / Масштаб"));

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

        box.add(Box.createVerticalStrut(8));
        JPanel btns = new JPanel(new GridLayout(1, 3, 6, 6));
        btns.add(btnResetTf);
        btns.add(btnBakeTf);
        btns.add(btnRestore);
        box.add(btns);

        btnResetTf.addActionListener(e -> {
            if (currentModel == null) return;
            currentModel.getTransform().reset();
            syncUiFromTransform();
            modelPanel.repaint();
            status("Transform сброшен");
        });

        btnBakeTf.addActionListener(e -> {
            if (currentModel == null) return;
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

        return box;
    }

    private JPanel row(String name, JSlider s) {
        JPanel r = new JPanel(new BorderLayout(6, 0));
        r.add(new JLabel(name), BorderLayout.WEST);
        r.add(s, BorderLayout.CENTER);
        return r;
    }

    private void setupSlider(JSlider s, int majorTick) {
        s.setMajorTickSpacing(majorTick);
        s.setPaintTicks(true);
        s.setPaintLabels(false);
    }

    // ========================= BOTTOM =========================

    private JPanel buildBottomPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(6, 10, 6, 10));
        p.add(statusLabel, BorderLayout.CENTER);
        return p;
    }

    // ========================= WIRING =========================

    private void wireModelSelection() {
        modelsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modelsList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            String sel = modelsList.getSelectedValue();
            if (sel != null) loadAndShowModel(sel);
        });
    }

    private void wireModelTransformUi() {
        var cl = (javax.swing.event.ChangeListener) e -> applyTransformFromUi();

        trX.addChangeListener(cl);
        trY.addChangeListener(cl);
        trZ.addChangeListener(cl);

        rtX.addChangeListener(cl);
        rtY.addChangeListener(cl);
        rtZ.addChangeListener(cl);

        scX.addChangeListener(cl);
        scY.addChangeListener(cl);
        scZ.addChangeListener(cl);
    }

    private void wireRenderSettingsUi() {
        cbWireframe.addActionListener(e -> {
            modelPanel.getRenderSettings().drawWireframe = cbWireframe.isSelected();
            modelPanel.repaint();
            modelPanel.requestFocusInWindow();
        });

        cbTexture.addActionListener(e -> {
            modelPanel.getRenderSettings().useTexture = cbTexture.isSelected();
            modelPanel.repaint();
            modelPanel.requestFocusInWindow();
        });

        cbLighting.addActionListener(e -> {
            modelPanel.getRenderSettings().useLighting = cbLighting.isSelected();
            modelPanel.repaint();
            modelPanel.requestFocusInWindow();
        });

        btnPickColor.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Цвет модели", modelPanel.getRenderSettings().baseColor);
            if (c != null) {
                modelPanel.getRenderSettings().baseColor = c;
                modelPanel.repaint();
                modelPanel.requestFocusInWindow();
            }
        });

        btnLoadTexture.addActionListener(e -> onLoadTexture());
    }

    private void wireCameraUi() {
        camerasList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        camerasList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int idx = camerasList.getSelectedIndex();
            if (idx >= 0) {
                modelPanel.getScene().setActiveIndex(idx);
                modelPanel.repaint();
                modelPanel.requestFocusInWindow();
            }
        });

        btnAddCamera.addActionListener(e -> {
            Camera base = modelPanel.getScene().getActiveCamera();
            if (base == null) return;

            Camera c = new Camera(
                    base.getPosition().add(new Vec3(0.5, 0.2, 0.5)),
                    base.getYaw(),
                    base.getPitch()
            );
            modelPanel.getScene().addCamera(c);
            refreshCamerasUi();
            camerasList.setSelectedIndex(modelPanel.getScene().getCameras().size() - 1);
            modelPanel.requestFocusInWindow();
        });

        btnRemoveCamera.addActionListener(e -> {
            int idx = camerasList.getSelectedIndex();
            if (idx < 0) idx = modelPanel.getScene().getActiveIndex();

            if (modelPanel.getScene().getCameras().size() <= 1) {
                JOptionPane.showMessageDialog(this,
                        "Нельзя удалить последнюю камеру.",
                        "Информация",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            modelPanel.getScene().removeCamera(idx);
            refreshCamerasUi();
            camerasList.setSelectedIndex(modelPanel.getScene().getActiveIndex());
            modelPanel.requestFocusInWindow();
        });
    }

    // ========================= MODELS =========================

    private void refreshModelsSafe() {
        modelsListModel.clear();
        try {
            List<String> names = repo.listModels(); // <--- твой метод
            for (String n : names) modelsListModel.addElement(n);
        } catch (Exception ex) {
            ex.printStackTrace();
            status("Ошибка чтения models/: " + ex.getMessage());
        }
    }

    private void loadAndShowModel(String modelName) {
        try {
            Path objPath = findObjInModelDir(modelName);
            if (objPath == null) {
                throw new IllegalArgumentException("В папке models/" + modelName + " не найден .obj файл");
            }

            Mesh mesh = ObjLoader.load(objPath); // <--- наш ObjLoader (триангуляция + пересчёт нормалей)
            originalMesh = mesh;
            currentModel = new ModelInstance(mesh);

            modelPanel.setInstance(currentModel);
            setControlsEnabled(true);
            syncUiFromTransform();

            status("Загружено: " + modelName +
                    "   file=" + objPath.getFileName() +
                    "   tris=" + mesh.triangleCount() +
                    "   verts=" + mesh.vertexCount());
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Ошибка загрузки модели: " + modelName, ex);
        }
    }

    private Path findObjInModelDir(String modelName) throws Exception {
        Path dir = Path.of("models").resolve(modelName);
        if (!Files.exists(dir) || !Files.isDirectory(dir)) return null;

        try (var s = Files.list(dir)) {
            return s.filter(p -> p.getFileName().toString().toLowerCase().endsWith(".obj"))
                    .findFirst()
                    .orElse(null);
        }
    }

    private void onImportModel() {
        JFileChooser ch = new JFileChooser();
        ch.setDialogTitle("Импорт OBJ");
        int res = ch.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File f = ch.getSelectedFile();
        try {
            String modelName = repo.importModel(f.toPath()); // <--- твой метод (создаёт папку models/<name>/)
            refreshModelsSafe();
            modelsList.setSelectedValue(modelName, true);
            status("Импортировано: " + modelName);
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Ошибка импорта", ex);
        }
    }

    private void onDeleteModel() {
        String sel = modelsList.getSelectedValue();
        if (sel == null) return;

        int ok = JOptionPane.showConfirmDialog(this,
                "Удалить модель '" + sel + "' ?",
                "Подтверждение",
                JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            repo.deleteModel(sel); // <--- твой метод (удаляет папку)
            refreshModelsSafe();
            status("Удалено: " + sel);

            if (currentModel != null) {
                // сбрасываем отображение
                currentModel = null;
                originalMesh = null;
                modelPanel.setInstance(null);
                setControlsEnabled(false);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Ошибка удаления", ex);
        }
    }

    // ========================= TEXTURE =========================

    private void onLoadTexture() {
        JFileChooser ch = new JFileChooser();
        ch.setDialogTitle("Выберите картинку-текстуру (png/jpg)");
        int res = ch.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File f = ch.getSelectedFile();
        try {
            BufferedImage img = ImageIO.read(f);
            if (img == null) throw new IllegalArgumentException("Не получилось прочитать изображение");

            modelPanel.getRenderSettings().texture = new Texture(img);
            modelPanel.getRenderSettings().useTexture = true;
            cbTexture.setSelected(true);

            status("Текстура загружена: " + f.getName());
            modelPanel.repaint();
            modelPanel.requestFocusInWindow();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Ошибка загрузки текстуры:\n" + ex.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ========================= CAMERAS UI =========================

    private void refreshCamerasUi() {
        camerasListModel.clear();
        int n = modelPanel.getScene().getCameras().size();
        for (int i = 0; i < n; i++) camerasListModel.addElement("Camera #" + (i + 1));
        camerasList.setSelectedIndex(modelPanel.getScene().getActiveIndex());
    }

    // ========================= TRANSFORM =========================

    private void setControlsEnabled(boolean enabled) {
        trX.setEnabled(enabled); trY.setEnabled(enabled); trZ.setEnabled(enabled);
        rtX.setEnabled(enabled); rtY.setEnabled(enabled); rtZ.setEnabled(enabled);
        scX.setEnabled(enabled); scY.setEnabled(enabled); scZ.setEnabled(enabled);

        btnResetTf.setEnabled(enabled);
        btnBakeTf.setEnabled(enabled);
        btnRestore.setEnabled(enabled);

        cbWireframe.setEnabled(enabled);
        cbTexture.setEnabled(enabled);
        cbLighting.setEnabled(enabled);
        btnPickColor.setEnabled(enabled);
        btnLoadTexture.setEnabled(enabled);
    }

    private void applyTransformFromUi() {
        if (currentModel == null) return;

        double tx = trX.getValue() / 100.0;
        double ty = trY.getValue() / 100.0;
        double tz = trZ.getValue() / 100.0;

        double rx = Math.toRadians(rtX.getValue());
        double ry = Math.toRadians(rtY.getValue());
        double rz = Math.toRadians(rtZ.getValue());

        double sx = scX.getValue() / 100.0;
        double sy = scY.getValue() / 100.0;
        double sz = scZ.getValue() / 100.0;

        currentModel.getTransform().setPosition(new Vec3(tx, ty, tz));
        currentModel.getTransform().setRotation(new Vec3(rx, ry, rz));
        currentModel.getTransform().setScale(new Vec3(sx, sy, sz));

        syncLabels();
        modelPanel.repaint();
    }

    private void syncUiFromTransform() {
        if (currentModel == null) return;

        Vec3 t = currentModel.getTransform().getPosition();
        Vec3 r = currentModel.getTransform().getRotation();
        Vec3 s = currentModel.getTransform().getScale();

        trX.setValue((int) Math.round(t.x * 100));
        trY.setValue((int) Math.round(t.y * 100));
        trZ.setValue((int) Math.round(t.z * 100));

        rtX.setValue((int) Math.round(Math.toDegrees(r.x)));
        rtY.setValue((int) Math.round(Math.toDegrees(r.y)));
        rtZ.setValue((int) Math.round(Math.toDegrees(r.z)));

        scX.setValue((int) Math.round(s.x * 100));
        scY.setValue((int) Math.round(s.y * 100));
        scZ.setValue((int) Math.round(s.z * 100));

        syncLabels();
    }

    private void syncLabels() {
        if (currentModel == null) return;
        Vec3 t = currentModel.getTransform().getPosition();
        Vec3 r = currentModel.getTransform().getRotation();
        Vec3 s = currentModel.getTransform().getScale();

        trVal.setText(String.format("T: (%.2f, %.2f, %.2f)", t.x, t.y, t.z));
        rtVal.setText(String.format("R: (%.0f°, %.0f°, %.0f°)",
                Math.toDegrees(r.x), Math.toDegrees(r.y), Math.toDegrees(r.z)));
        scVal.setText(String.format("S: (%.2f, %.2f, %.2f)", s.x, s.y, s.z));
    }

    // ========================= IMAGES =========================

    private void onOpenImage() {
        JFileChooser ch = new JFileChooser();
        ch.setDialogTitle("Открыть изображение");
        int res = ch.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File f = ch.getSelectedFile();
        try {
            originalImage = ImageIO.read(f);
            currentImage = copyImage(originalImage); // <--- вместо ImageProcessorSwing.copy(...)
            imageLabel.setIcon(new ImageIcon(currentImage));
            imageLabel.setText("");
            status("Открыто изображение: " + f.getName());

            // пример: можно применить фильтры так:
            // currentImage = ImageProcessorSwing.apply(originalImage, false,false,0f,1f);

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Ошибка открытия изображения", ex);
        }
    }

    private void onSaveImage() {
        if (currentImage == null) return;

        JFileChooser ch = new JFileChooser();
        ch.setDialogTitle("Сохранить изображение");
        int res = ch.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File f = ch.getSelectedFile();
        try {
            ImageIO.write(currentImage, "png", f);
            status("Сохранено: " + f.getName());
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Ошибка сохранения", ex);
        }
    }

    private static BufferedImage copyImage(BufferedImage src) {
        if (src == null) return null;
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return dst;
    }

    // ========================= UTILS =========================

    private void status(String s) { statusLabel.setText(s); }

    private void showError(String msg, Exception ex) {
        JOptionPane.showMessageDialog(this,
                msg + "\n" + ex.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
    }
}
