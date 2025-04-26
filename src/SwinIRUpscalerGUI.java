import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;

public class SwinIRUpscalerGUI extends JFrame {
    private JLabel inputImageLabel;
    private JLabel outputImageLabel;
    private JLabel statusLabel;
    

    private JButton browseButton;
    private JButton processButton;

    private JPanel modelTypeUpscalePanel;
    private JPanel optionsPanel;
    private JPanel noiseLevelPanel;

    private File selectedInputFile = null;
    private File currentOutputFile = null;
    private BufferedImage outputBufferedImage = null;
    private JFileChooser fileChooser;  

    private JRadioButton classicalSrRadio;
    private JRadioButton realGanSrRadio;

    private ButtonGroup modelTypeUpscaleGroup;
    private ButtonGroup taskGroup;
    private ButtonGroup noiseLevelGroup;

    private JRadioButton upscaleTaskRadio;
    private JRadioButton denoiseTaskRadio;
    private JRadioButton noise15Radio;
    private JRadioButton noise25Radio;
    private JRadioButton noise50Radio;

    private static final String PYTHON_EXECUTABLE = "python_backend/venv/Scripts/python.exe"; 
    private static final String MODELS_DIR = "python_backend/models";
    private static final String SCRIPT_NAME = "upscale_swinir.py";

    public SwinIRUpscalerGUI() {
        super("SwinIR Image Upscaler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        setupLayout();
        setupStateChangeListeners();
        setupDragAndDrop();
        setupActions();

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Ảnh (JPG, PNG, BMP)", "jpg", "jpeg", "png", "bmp"));
        fileChooser.setAcceptAllFileFilterUsed(false);

        setVisible(true);
    }

    private void initComponents() {
        inputImageLabel = createImageLabel("Kéo thả ảnh vào đây hoặc nhấn 'Chọn Ảnh'");
        outputImageLabel = createImageLabel("Ảnh kết quả sẽ hiển thị ở đây");
        browseButton = new JButton("Chọn Ảnh...");
        processButton = new JButton("Xử Lý Ảnh");
        processButton.setEnabled(false);
        statusLabel = new JLabel("Sẵn sàng.", SwingConstants.CENTER);

        upscaleTaskRadio = new JRadioButton("Upscale (Tăng độ phân giải)");
        upscaleTaskRadio.setActionCommand("upscale");
        upscaleTaskRadio.setSelected(true);

        denoiseTaskRadio = new JRadioButton("Khử nhiễu Màu (Color Denoise)");
        denoiseTaskRadio.setActionCommand("denoise_color");

        taskGroup = new ButtonGroup();
        taskGroup.add(upscaleTaskRadio);
        taskGroup.add(denoiseTaskRadio);

        classicalSrRadio = new JRadioButton("Chất lượng Chung (Classical)");
        classicalSrRadio.setActionCommand("classical_M");
        classicalSrRadio.setSelected(true);

        realGanSrRadio = new JRadioButton("Ảnh Thực Tế (RealGAN)");
        realGanSrRadio.setActionCommand("realgan_M");

        modelTypeUpscaleGroup  = new ButtonGroup();
        modelTypeUpscaleGroup.add(classicalSrRadio);
        modelTypeUpscaleGroup.add(realGanSrRadio);

        modelTypeUpscalePanel = new JPanel();
        modelTypeUpscalePanel.setLayout(new BoxLayout(modelTypeUpscalePanel, BoxLayout.Y_AXIS));
        modelTypeUpscalePanel.setBorder(BorderFactory.createTitledBorder("Loại Model Upscale"));
        modelTypeUpscalePanel.add(classicalSrRadio);
        modelTypeUpscalePanel.add(realGanSrRadio);

        noise15Radio = new JRadioButton("Mức nhiễu 15 (Nhẹ)");
        noise15Radio.setActionCommand("15");
        noise15Radio.setSelected(true); 

        noise25Radio = new JRadioButton("Mức nhiễu 25 (TB)");
        noise25Radio.setActionCommand("25");

        noise50Radio = new JRadioButton("Mức nhiễu 50 (Nặng)");
        noise50Radio.setActionCommand("50");

        noiseLevelGroup = new ButtonGroup();
        noiseLevelGroup.add(noise15Radio);
        noiseLevelGroup.add(noise25Radio);
        noiseLevelGroup.add(noise50Radio);

        noiseLevelPanel = new JPanel();
        noiseLevelPanel.setLayout(new BoxLayout(noiseLevelPanel, BoxLayout.Y_AXIS));
        noiseLevelPanel.setBorder(BorderFactory.createTitledBorder("Mức Độ Nhiễu"));
        noiseLevelPanel.add(noise15Radio);
        noiseLevelPanel.add(noise25Radio);
        noiseLevelPanel.add(noise50Radio);

        optionsPanel = new JPanel(new CardLayout()); 
        optionsPanel.add(modelTypeUpscalePanel, "upscale_options");
        optionsPanel.add(noiseLevelPanel, "denoise_options");
    }

    private JLabel createImageLabel(String initialText) {
        JLabel label = new JLabel(initialText, SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(350, 350));
        label.setBorder(BorderFactory.createEtchedBorder());
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private void setupLayout() {
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(inputImageLabel, BorderLayout.CENTER);
        inputPanel.add(browseButton, BorderLayout.SOUTH);

        JScrollPane outputScrollPane = new JScrollPane(outputImageLabel);

        JPanel outputDisplayPanel = new JPanel(new BorderLayout());
        outputDisplayPanel.add(outputScrollPane, BorderLayout.CENTER);

        JPanel rightSidePanel = new JPanel(new GridBagLayout());
        rightSidePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 5)); 
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel taskPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        taskPanel.setBorder(BorderFactory.createTitledBorder("Chọn Nhiệm Vụ"));
        taskPanel.add(upscaleTaskRadio);
        taskPanel.add(denoiseTaskRadio);
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.anchor = GridBagConstraints.NORTHWEST; gbc.insets = new Insets(0, 0, 10, 0); 
        rightSidePanel.add(taskPanel, gbc);

        gbc.gridy = 1; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH; 
        gbc.anchor = GridBagConstraints.CENTER; gbc.insets = new Insets(0, 0, 10, 0); 
        rightSidePanel.add(optionsPanel, gbc);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        controlPanel.add(processButton); 
        JButton saveButton = new JButton("Lưu kết quả...");
        saveButton.addActionListener(e -> saveOutputFile());
        controlPanel.add(saveButton);
        gbc.gridy = 2; gbc.weighty = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; 
        gbc.anchor = GridBagConstraints.SOUTH; gbc.insets = new Insets(0, 0, 0, 0); 
        rightSidePanel.add(controlPanel, gbc);

        JPanel outputControlPanel = new JPanel(new BorderLayout(10, 5));
        outputControlPanel.add(outputDisplayPanel, BorderLayout.CENTER);
        outputControlPanel.add(rightSidePanel, BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, outputControlPanel);
        splitPane.setResizeWeight(0.3); 
        splitPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(splitPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    private void setupStateChangeListeners() {
        ActionListener taskListener = e -> updateComponentVisibility();
        upscaleTaskRadio.addActionListener(taskListener);
        denoiseTaskRadio.addActionListener(taskListener);
    }
   
   private void updateComponentVisibility() {
       CardLayout cl = (CardLayout) (optionsPanel.getLayout()); 
       if (upscaleTaskRadio.isSelected()) {
           cl.show(optionsPanel, "upscale_options"); 
       }
       else if (denoiseTaskRadio.isSelected()) {
           cl.show(optionsPanel, "denoise_options"); 
       }
       optionsPanel.revalidate();
       optionsPanel.repaint();
   }

    private void setupDragAndDrop() {
        inputImageLabel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (files.size() == 1) {
                        File file = files.get(0);
                        String name = file.getName().toLowerCase();
                        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".bmp")) {
                            displayInputImage(file);
                            return true;
                        } else {
                             JOptionPane.showMessageDialog(SwinIRUpscalerGUI.this,
                                    "Chỉ hỗ trợ file ảnh JPG, PNG, BMP.",
                                    "Lỗi Định Dạng File", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    }
                } catch (Exception e) {
                    statusLabel.setText("Lỗi kéo thả: " + e.getMessage());
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    private void setupActions() {
        browseButton.addActionListener(e -> browseForImage());
        processButton.addActionListener(e -> startProcessing());
    }

    private void browseForImage() {
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            displayInputImage(file);
        }
    }

    private void displayInputImage(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img != null) {
                selectedInputFile = file;
                ImageIcon icon = new ImageIcon(getScaledImage(img, inputImageLabel.getWidth(), inputImageLabel.getHeight()));
                inputImageLabel.setIcon(icon);
                inputImageLabel.setText("");
                processButton.setEnabled(true);
                outputImageLabel.setIcon(null);
                outputImageLabel.setText("Ảnh kết quả sẽ hiển thị ở đây");
                outputBufferedImage = null;
                currentOutputFile = null;
                statusLabel.setText("Đã chọn ảnh: " + file.getName());
            } else {
                throw new IOException("Không thể đọc file ảnh.");
            }
        } catch (IOException ex) {
            selectedInputFile = null;
            inputImageLabel.setIcon(null);
            inputImageLabel.setText("Lỗi đọc ảnh. Thử lại.");
            processButton.setEnabled(false);
            statusLabel.setText("Lỗi: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Không thể tải ảnh: " + ex.getMessage(), "Lỗi Tải Ảnh", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Image getScaledImage(Image srcImg, int w, int h){
        if (srcImg == null) return null;
        int originalWidth = srcImg.getWidth(null);
        int originalHeight = srcImg.getHeight(null);
        if (originalWidth <= 0 || originalHeight <= 0) return null;

        if (originalWidth <= w && originalHeight <= h) return srcImg;

        double widthRatio = (double) w / originalWidth;
        double heightRatio = (double) h / originalHeight;
        double scaleRatio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * scaleRatio);
        int newHeight = (int) (originalHeight * scaleRatio);

        if (newWidth < 1 || newHeight < 1) return srcImg;

        BufferedImage resizedImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, newWidth, newHeight, null);
        g2.dispose();

        return resizedImg;
    }


    private void startProcessing() { 
        if (selectedInputFile == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ảnh đầu vào trước.", "Chưa Chọn Ảnh", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String task = taskGroup.getSelection().getActionCommand();
        String modelFileName = null;
        String modelType = null;
        int scale = 1;
        int noiseLevel = -1;

        if ("upscale".equals(task)) {
            modelType = modelTypeUpscaleGroup.getSelection().getActionCommand();
            
            String[] scaleOptions;
             if (modelType.contains("realgan")) { 
                 scaleOptions = new String[]{"x2", "x4"}; 
             } else { 
                 scaleOptions = new String[]{"x2", "x3", "x4", "x8"}; 
             }

            String selectedScaleStr = (String) JOptionPane.showInputDialog(
                    this, "Chọn tỉ lệ upscale:",
                    "Chọn Tỉ Lệ (Model: " + modelType.replace("_M","") + ")", 
                    JOptionPane.QUESTION_MESSAGE, null, scaleOptions, scaleOptions[0]);

            if (selectedScaleStr == null || selectedScaleStr.isEmpty()) return;

            try {
                 scale = Integer.parseInt(selectedScaleStr.substring(1)); 
            } catch (NumberFormatException e) {
                 JOptionPane.showMessageDialog(this, "Lựa chọn tỉ lệ không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            
             String typeSuffix = modelType.replace("_M", "").replace("_S","").replace("_L",""); 
             modelFileName = String.format("model_x%d_%s.pth", scale, typeSuffix);

        } else if ("denoise_color".equals(task)) {
             try {
                  noiseLevel = Integer.parseInt(noiseLevelGroup.getSelection().getActionCommand());
             } catch (NumberFormatException e) {
                  JOptionPane.showMessageDialog(this, "Lựa chọn mức nhiễu không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                  return;
             }
             modelType = "colordenoise_M";
             modelFileName = String.format("model_colordn_noise%d.pth", noiseLevel);
             scale = 1;
        } else {
             JOptionPane.showMessageDialog(this, "Nhiệm vụ không xác định.", "Lỗi", JOptionPane.ERROR_MESSAGE);
             return;
        }

        if (modelFileName == null) { 
             JOptionPane.showMessageDialog(this, "Không thể xác định tên file model.", "Lỗi Logic", JOptionPane.ERROR_MESSAGE);
             return;
        }
        File modelFile = new File(MODELS_DIR, modelFileName);
        System.out.println("Đang kiểm tra model file: " + modelFile.getAbsolutePath()); 
        if (!modelFile.exists() || !modelFile.isFile()) {
             JOptionPane.showMessageDialog(this,
                     "Không tìm thấy file model yêu cầu tại:\n" + modelFile.getAbsolutePath() +
                      "\nVui lòng kiểm tra thư mục 'models' và đảm bảo đã tải/đổi tên file đúng.",
                     "Thiếu Model", JOptionPane.ERROR_MESSAGE);
             return;
         }

        performProcessing(task, selectedInputFile, scale, noiseLevel, modelFile.getAbsolutePath(), modelType); 

    }

    private void performProcessing(String task, File inputFile, int scale, int noiseLevel, String modelPath, String modelType) { 
        processButton.setEnabled(false);
        
        String statusText = String.format("Đang %s...", 
                             (task.equals("upscale") ? "upscale x" + scale + " (Model: " + modelType.replace("_M","") + ")" : "khử nhiễu màu (Noise: " + noiseLevel + ")"));
        statusLabel.setText(statusText); 
        outputImageLabel.setIcon(null);
        outputImageLabel.setText("Đang xử lý...");
        outputBufferedImage = null;
        currentOutputFile = null;

        String inputName = inputFile.getName();
        String baseName = inputName.substring(0, inputName.lastIndexOf('.'));
        String extension = inputName.substring(inputName.lastIndexOf('.'));
        File outputDir = new File("output"); 
        if (!outputDir.exists()) outputDir.mkdirs(); 
        
        String outputSuffix;
        if (task.equals("upscale")) {
            String typeSuffix = modelType.replace("_M", "").replace("_S","").replace("_L",""); 
            outputSuffix = String.format("_swinir_%s_x%d", typeSuffix, scale);
        } else {
            outputSuffix = String.format("_swinir_denoise%d", noiseLevel);
        }
        String outputFileName = baseName + outputSuffix + extension;
        File outputFile = new File(outputDir, outputFileName); 
        currentOutputFile = outputFile;

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            private String errorMessage = null;

            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    List<String> command = new ArrayList<>();
                    command.add(PYTHON_EXECUTABLE);
                    command.add(SCRIPT_NAME);
                    command.add("--task"); command.add(task);
                    command.add("--input"); command.add(inputFile.getAbsolutePath());
                    command.add("--output"); command.add(outputFile.getAbsolutePath());
                    command.add("--model_path"); command.add(modelPath);
                    
                    if (task.equals("upscale")) {
                        command.add("--scale"); command.add(String.valueOf(scale));
                        command.add("--model_type"); command.add(modelType);
                    } else {
                        command.add("--noise_level"); command.add(String.valueOf(noiseLevel));
                        command.add("--model_type"); command.add("colordenoise_M"); 
                    }
                    
                    command.add("--tile"); command.add("256"); 
                    command.add("--tile_overlap"); command.add("32");

                    ProcessBuilder pb = new ProcessBuilder(command);
                    pb.directory(new File("python_backend")); 

                    Map<String, String> env = pb.environment();
                    env.put("PYTHONIOENCODING", "utf-8");
                    pb.redirectErrorStream(true); 

                    System.out.println("Working Directory: " + pb.directory()); 
                    System.out.println("Đang chạy lệnh: " + String.join(" ", pb.command())); 

                    Process process = pb.start();

                    StringBuilder scriptOutput = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("Output script: " + line); 
                            scriptOutput.append(line).append("\n");
                        }
                    }
                    int exitCode = process.waitFor(); 

                    if (exitCode == 0 && outputFile.exists() && outputFile.length() > 0) {
                         return true;
                    } else {
                         errorMessage = "Script Python kết thúc với mã lỗi " + exitCode + ".";
                          if (!outputFile.exists() && exitCode == 0) {
                               errorMessage += " Không tìm thấy file output dù script báo thành công?";
                          } else if (!outputFile.exists()){
                               errorMessage += " Không tìm thấy file output.";
                          } else if (outputFile.length() == 0){
                               errorMessage += " File output rỗng.";
                          }
                         errorMessage += "\nOutput từ script:\n" + scriptOutput.toString();
                         System.err.println(errorMessage); 
                         return false;
                     }

                } catch (IOException | InterruptedException e) {
                     errorMessage = "Lỗi khi chạy/giao tiếp với tiến trình Python: " + e.getMessage();
                     e.printStackTrace();
                     return false;
                 } catch (Exception e) { 
                     errorMessage = "Lỗi không xác định trong doInBackground: " + e.getMessage();
                      e.printStackTrace();
                      return false;
                 }
            }

             @Override
            protected void done() {
                try {
                    boolean success = get(); 
                    if (success && currentOutputFile != null && currentOutputFile.exists()) {
                         String successMsg = String.format("%s thành công! Lưu tại: %s", 
                                             (task.equals("upscale") ? "Upscale" : "Khử nhiễu"),
                                             currentOutputFile.getName());
                        statusLabel.setText(successMsg);
                        displayOutputImage(currentOutputFile);
                    } else {
                         statusLabel.setText("Xử lý thất bại.");
                         outputImageLabel.setText("Lỗi xử lý.");
                         outputImageLabel.setIcon(null);
                         JOptionPane.showMessageDialog(SwinIRUpscalerGUI.this,
                                 "Đã xảy ra lỗi trong quá trình xử lý.\n" + (errorMessage != null ? errorMessage : "Lỗi không xác định."),
                                 "Lỗi Xử Lý", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException e) {
                     statusLabel.setText("Lỗi khi chờ kết quả xử lý.");
                     outputImageLabel.setText("Lỗi thực thi.");
                     outputImageLabel.setIcon(null);
                      JOptionPane.showMessageDialog(SwinIRUpscalerGUI.this,
                                 "Lỗi khi chờ tiến trình backend: " + e.getMessage(),
                                 "Lỗi Thực Thi", JOptionPane.ERROR_MESSAGE);
                     e.printStackTrace();
                 } finally {
                     processButton.setEnabled(selectedInputFile != null);
                 }
            }
        };
        worker.execute();
    }

    private void displayOutputImage(File file) {
         try {
            outputBufferedImage = ImageIO.read(file);
            if (outputBufferedImage != null) {
                ImageIcon icon = new ImageIcon(outputBufferedImage);

                outputImageLabel.setIcon(icon); 
                outputImageLabel.setText(null);
                
                Component parent = outputImageLabel.getParent();
                if (parent instanceof JViewport) {
                    Component grandParent = parent.getParent();
                    if (grandParent instanceof JScrollPane) {
                        grandParent.revalidate();
                        grandParent.repaint();
                         System.out.println("JScrollPane revalidated for output image.");
                    } else {
                         System.err.println("Grandparent of outputImageLabel is not a JScrollPane!");
                    }
                } else {
                     System.err.println("Parent of outputImageLabel is not a JViewport! JScrollPane might not work correctly.");
                }
            } else {
                throw new IOException("Không thể đọc file ảnh kết quả từ: " + file.getAbsolutePath());
            }
        } catch (IOException ex) {
            outputImageLabel.setIcon(null);
            outputImageLabel.setText("Lỗi hiển thị ảnh kết quả.");
            statusLabel.setText("Lỗi đọc/hiển thị ảnh kết quả: " + ex.getMessage());
            System.err.println("IOException in displayOutputImage: " + ex.getMessage());
        }
    }

    private void saveOutputFile() {
        if (outputBufferedImage == null || currentOutputFile == null) {
             JOptionPane.showMessageDialog(this, "Chưa có ảnh kết quả để lưu.", "Không Có Gì Để Lưu", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser saveChooser = new JFileChooser();
        saveChooser.setDialogTitle("Lưu ảnh kết quả");
        saveChooser.setSelectedFile(new File(currentOutputFile.getName())); 
        saveChooser.setFileFilter(new FileNameExtensionFilter("PNG Image (*.png)", "png"));
        saveChooser.setAcceptAllFileFilterUsed(false);


        int userSelection = saveChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = saveChooser.getSelectedFile();
             String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".png")) {
                fileToSave = new File(filePath + ".png");
            }

            try {
                if (fileToSave.exists()) {
                    int result = JOptionPane.showConfirmDialog(this,
                            "File đã tồn tại, bạn có muốn ghi đè?", "Xác nhận ghi đè",
                            JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.NO_OPTION) {
                        return;
                    }
                }
                
                boolean success = ImageIO.write(outputBufferedImage, "png", fileToSave);
                if (success) {
                     JOptionPane.showMessageDialog(this, "Đã lưu ảnh thành công!", "Thông Báo", JOptionPane.INFORMATION_MESSAGE);
                     statusLabel.setText("Đã lưu ảnh vào: " + fileToSave.getName());
                } else {
                     throw new IOException("ImageIO không thể ghi file PNG.");
                }
            } catch (IOException ex) {
                 JOptionPane.showMessageDialog(this, "Lỗi khi lưu ảnh: " + ex.getMessage(), "Lỗi Lưu File", JOptionPane.ERROR_MESSAGE);
                 statusLabel.setText("Lỗi lưu file.");
                 ex.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwinIRUpscalerGUI());
    }
}