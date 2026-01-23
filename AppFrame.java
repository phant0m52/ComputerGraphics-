package app.ui;

import app.image.ImageProcessorSwing;
import app.model.ModelRepository;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

public class AppFrame extends JFrame {

    private final ModelRepository repo = new ModelRepository();

    private final DefaultListModel<String> modelsListModel = new DefaultListModel<>();
    private final JList<String> modelsList = new JList<>(modelsListModel);

    private final JLabel imageLabel = new JLabel("Открой изображение", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel("Готово");

    private final JCheckBox cbGrayscale = new JCheckBox("Ч/Б");
    private final JCheckBox cbEdges = new JCheckBox("Контуры");

    private final JSlider slBrightness = new JSlider(-50, 50, 0);  // -0.5..0.5
    private final JSlider slContrast = new JSlider(50, 150, 100);  // 0.5..1.5

    private BufferedImage originalImage;
    private BufferedImage currentImage;

    private final Deque<BufferedImage> undo = new ArrayDeque<>();
    private final Deque<BufferedImage> redo = new ArrayDeque<>();

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
        wireEvents();
    }

    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setPreferredSize(new Dimension(280, 10));

        JLabel title = new JLabel("Модели");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        p.add(title, BorderLayout.NORTH);

        modelsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(245, 245, 245));
        imageLabel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        p.add(new JScrollPane(imageLabel), BorderLayout.CENTER);

        return p;
    }

    private JPanel buildRightPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        p.setPreferredSize(new Dimension(320, 10));

        JLabel title = new JLabel("Преобразования");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        p.add(title);
        p.add(Box.createVerticalStrut(10));

        p.add(cbGrayscale);
        p.add(cbEdges);
        p.add(Box.createVerticalStrut(10));

        p.add(new JLabel("Яркость"));
        slBrightness.setMajorTickSpacing(25);
        slBrightness.setPaintTicks(true);
        p.add(slBrightness);

        p.add(Box.createVerticalStrut(10));
        p.add(new JLabel("Контраст"));
        slContrast.setMajorTickSpacing(25);
        slContrast.setPaintTicks(true);
        p.add(slContrast);

        p.add(Box.createVerticalStrut(14));
        JPanel undoRedo = new JPanel(new GridLayout(1, 2, 8, 8));
        JButton btnUndo = new JButton("Undo");
        btnUndo.addActionListener(e -> onUndo());
        JButton btnRedo = new JButton("Redo");
        btnRedo.addActionListener(e -> onRedo());
        undoRedo.add(btnUndo);
        undoRedo.add(btnRedo);
        p.add(undoRedo);

        p.add(Box.createVerticalGlue());
        JLabel hint = new JLabel("<html><body style='width:280px'>Эффекты применяются при отпускании ползунка или клике чекбокса.</body></html>");
        hint.setForeground(new Color(90, 90, 90));
        p.add(hint);

        return p;
    }

    private JPanel buildBottomPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(8, 10, 8, 10));
        p.add(statusLabel, BorderLayout.CENTER);
        return p;
    }

    private void wireEvents() {
        cbGrayscale.addActionListener(e -> applyEffectsPushUndo());
        cbEdges.addActionListener(e -> applyEffectsPushUndo());

        slBrightness.addMouseListener(new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) { applyEffectsPushUndo(); }
        });
        slContrast.addMouseListener(new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) { applyEffectsPushUndo(); }
        });

        slBrightness.addChangeListener(e -> {
            if (!slBrightness.getValueIsAdjusting()) applyEffectsPushUndo();
        });
        slContrast.addChangeListener(e -> {
            if (!slContrast.getValueIsAdjusting()) applyEffectsPushUndo();
        });
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
            undo.clear(); redo.clear();
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

    private void applyEffectsPushUndo() {
        if (originalImage == null) return;

        if (currentImage != null) undo.push(deepCopy(currentImage));
        redo.clear();

        try {
            float brightness = slBrightness.getValue() / 100f; // -0.5..0.5
            float contrast = slContrast.getValue() / 100f;     // 0.5..1.5

            currentImage = ImageProcessorSwing.apply(
                    originalImage,
                    cbGrayscale.isSelected(),
                    cbEdges.isSelected(),
                    brightness,
                    contrast
            );
            renderImage(currentImage);
            status("Эффект применён");
        } catch (Exception ex) {
            showError("Ошибка обработки изображения", ex);
        }
    }

    private void onUndo() {
        if (undo.isEmpty()) return;
        redo.push(deepCopy(currentImage));
        currentImage = undo.pop();
        renderImage(currentImage);
        status("Undo");
    }

    private void onRedo() {
        if (redo.isEmpty()) return;
        undo.push(deepCopy(currentImage));
        currentImage = redo.pop();
        renderImage(currentImage);
        status("Redo");
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
