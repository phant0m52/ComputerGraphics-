package app.ui;

import app.image.ImageProcessor;
import app.model.ModelRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

public class MainController {

    @FXML private ListView<String> modelsList;
    @FXML private ImageView imageView;
    @FXML private Label statusLabel;

    @FXML private CheckBox cbGrayscale;
    @FXML private CheckBox cbEdges;
    @FXML private Slider slBrightness;
    @FXML private Slider slContrast;

    private final ModelRepository repo = new ModelRepository();
    private Image originalImage;
    private Image currentImage;

    private final Deque<Image> undo = new ArrayDeque<>();
    private final Deque<Image> redo = new ArrayDeque<>();

    @FXML
    public void initialize() {
        refreshModels();
        slContrast.setValue(1.0);
        status("Готово");
    }

    private void refreshModels() {
        try {
            modelsList.getItems().setAll(repo.listModels());
        } catch (Exception e) {
            showError("Не удалось загрузить список моделей", e);
        }
    }

    @FXML
    private void onImportModel() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Выберите OBJ файл модели");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("OBJ", "*.obj"));
        File f = fc.showOpenDialog(modelsList.getScene().getWindow());
        if (f == null) return;

        try {
            String name = repo.importModel(Path.of(f.getAbsolutePath()));
            refreshModels();
            status("Импортировано: " + name);
        } catch (Exception e) {
            showError("Ошибка импорта модели", e);
        }
    }

    @FXML
    private void onDeleteModel() {
        String sel = modelsList.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        if (confirm("Удалить модель '" + sel + "'?")) {
            try {
                repo.deleteModel(sel);
                refreshModels();
                status("Удалено: " + sel);
            } catch (Exception e) {
                showError("Ошибка удаления модели", e);
            }
        }
    }

    @FXML
    private void onDeletePart() {
        info("Удаление части модели делается по группам OBJ (g/o).\n" +
                "Для этого нужно подключить ваш ObjReader и структуру частей модели.");
    }

    @FXML
    private void onOpenImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Открыть изображение");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File f = fc.showOpenDialog(imageView.getScene().getWindow());
        if (f == null) return;

        originalImage = new Image(f.toURI().toString());
        currentImage = originalImage;
        imageView.setImage(currentImage);
        undo.clear(); redo.clear();
        status("Изображение загружено");
    }

    @FXML
    private void onSaveImage() {
        if (currentImage == null) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Сохранить изображение");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        File f = fc.showSaveDialog(imageView.getScene().getWindow());
        if (f == null) return;

        try {
            var bi = SwingFXUtils.fromFXImage(currentImage, null);
            ImageIO.write(bi, "png", ensurePng(f));
            status("Сохранено: " + f.getName());
        } catch (IOException e) {
            showError("Ошибка сохранения", e);
        }
    }

    private File ensurePng(File f) {
        String name = f.getName().toLowerCase();
        if (!name.endsWith(".png")) return new File(f.getParentFile(), f.getName() + ".png");
        return f;
    }

    @FXML
    private void onApplyEffects() {
        if (originalImage == null) return;

        if (currentImage != null) undo.push(currentImage);
        redo.clear();

        try {
            currentImage = ImageProcessor.apply(
                    originalImage,
                    cbGrayscale.isSelected(),
                    cbEdges.isSelected(),
                    slBrightness.getValue(),
                    slContrast.getValue()
            );
            imageView.setImage(currentImage);
            status("Эффект применён");
        } catch (Exception e) {
            showError("Ошибка обработки изображения", e);
        }
    }

    @FXML
    private void onUndo() {
        if (undo.isEmpty()) return;
        redo.push(currentImage);
        currentImage = undo.pop();
        imageView.setImage(currentImage);
        status("Undo");
    }

    @FXML
    private void onRedo() {
        if (redo.isEmpty()) return;
        undo.push(currentImage);
        currentImage = redo.pop();
        imageView.setImage(currentImage);
        status("Redo");
    }

    private void status(String s) { statusLabel.setText(s); }

    private void showError(String msg, Exception e) {
        status(msg + ": " + e.getMessage());
        Alert a = new Alert(Alert.AlertType.ERROR, msg + "\n\n" + e.getMessage(), ButtonType.OK);
        a.setHeaderText("Ошибка");
        a.showAndWait();
    }

    private void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText("Информация");
        a.showAndWait();
    }

    private boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setHeaderText("Подтверждение");
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }
}
