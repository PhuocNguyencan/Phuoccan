# Phần Mềm Xử Lý Ảnh SwinIR (Java GUI + Python Backend)

Phần mềm này cung cấp một giao diện đồ họa người dùng (GUI) viết bằng Java Swing để dễ dàng thực hiện hai tác vụ khôi phục ảnh chính bằng mô hình trí tuệ nhân tạo SwinIR:

1.  **Upscale (Siêu phân giải):** Tăng độ phân giải của hình ảnh (ví dụ: x2, x4) với lựa chọn giữa các loại model tối ưu cho chất lượng chung (Classical) hoặc ảnh thực tế (RealGAN).
2.  **Color Denoise (Khử nhiễu Màu):** Loại bỏ nhiễu Gaussian khỏi ảnh màu ở các mức độ khác nhau (nhẹ, trung bình, nặng).

Phần xử lý ảnh nặng được thực hiện bởi backend Python sử dụng thư viện PyTorch và mô hình SwinIR, có hỗ trợ tăng tốc bằng GPU NVIDIA CUDA nếu được cài đặt đúng cách.

## Tính năng

*   Giao diện đồ họa trực quan, dễ sử dụng.
*   Hỗ trợ kéo thả hoặc chọn file ảnh đầu vào (JPG, PNG, BMP).
*   Cho phép chọn giữa hai nhiệm vụ chính: Upscale hoặc Khử nhiễu Màu.
*   **Upscale:**
    *   Chọn tỉ lệ: x2, x3, x4, x8 (tùy thuộc vào model Classical).
    *   Chọn loại model: Chất lượng Chung (Classical - cho ảnh sạch/tổng quát) hoặc Ảnh Thực Tế (RealGAN - cho ảnh chụp thực tế, chỉ hỗ trợ x2, x4).
*   **Khử nhiễu Màu:**
    *   Chọn mức độ nhiễu cần xử lý: 15 (Nhẹ), 25 (Trung bình), 50 (Nặng).
*   Sử dụng GPU NVIDIA CUDA để tăng tốc xử lý nếu có và được cấu hình đúng.
*   Hiển thị ảnh gốc và ảnh kết quả song song.
*   Hỗ trợ cuộn ảnh kết quả nếu kích thước vượt quá khung nhìn.
*   Cho phép lưu ảnh kết quả dưới dạng file PNG.
*   Sử dụng kỹ thuật Tiling để xử lý ảnh lớn, giảm yêu cầu bộ nhớ GPU.

## Yêu cầu hệ thống

*   **Hệ điều hành:** Windows 10/11 (64-bit)
*   **Phần cứng:**
    *   **Card đồ họa (GPU) NVIDIA tương thích với CUDA:** Khuyến nghị mạnh mẽ (dòng GeForce GTX 10xx / RTX trở lên). Phần mềm sẽ chạy rất chậm trên CPU.
    *   RAM hệ thống: Tối thiểu 8GB, khuyến nghị 16GB+.
    *   Dung lượng ổ cứng trống: Khoảng 15-20GB (cho các công cụ, thư viện và models).

## Hướng dẫn Cài đặt Môi trường

Vui lòng thực hiện **tuần tự và chính xác** các bước sau:

### Bước 1: Cài đặt/Cập nhật Driver NVIDIA

Driver card đồ họa là cần thiết để hệ thống nhận diện và sử dụng GPU.

1.  Truy cập trang tải driver NVIDIA: [https://www.nvidia.com/Download/index.aspx](https://www.nvidia.com/Download/index.aspx)
2.  Chọn đúng thông tin card đồ họa và hệ điều hành Windows của bạn.
3.  Tải về phiên bản **Game Ready Driver (GRD)** hoặc **Studio Driver (SD)** mới nhất.
4.  Chạy file cài đặt. Nên chọn **"Custom (Advanced)"** và đánh dấu **"Perform a clean installation"** để đảm bảo cài đặt sạch.
5.  **Khởi động lại máy tính** sau khi cài đặt xong.

### Bước 2: Cài đặt NVIDIA CUDA Toolkit

Cung cấp môi trường tính toán song song trên GPU cho PyTorch.

1.  **Xác định phiên bản CUDA:**
    *   Truy cập trang cài đặt PyTorch ([https://pytorch.org/get-started/locally/](https://pytorch.org/get-started/locally/)) để xem phiên bản PyTorch Stable hiện tại hỗ trợ những phiên bản CUDA nào (ví dụ: 11.8, 12.1). **Ghi nhớ một phiên bản CUDA được hỗ trợ**.
    *   Đảm bảo Driver NVIDIA bạn cài ở Bước 1 hỗ trợ phiên bản CUDA này.
2.  **Tải CUDA Toolkit:**
    *   Truy cập kho lưu trữ CUDA Toolkit: [https://developer.nvidia.com/cuda-toolkit-archive](https://developer.nvidia.com/cuda-toolkit-archive)
    *   Tìm và chọn **đúng phiên bản CUDA** bạn đã xác định ở trên (ví dụ: 11.8.0).
    *   Chọn: **Windows, x86_64, Windows 10/11, exe (local)**.
    *   Tải file cài đặt về (dung lượng lớn).
3.  **Cài đặt CUDA Toolkit:**
    *   Chạy file cài đặt.
    *   Chọn **"Express (Recommended)"**.
    *   Hoàn tất các bước cài đặt.
4.  **Kiểm tra (Tùy chọn):** Mở Command Prompt (CMD), gõ `nvcc --version`. Nếu thành công sẽ hiển thị phiên bản CUDA.

### Bước 3: Cài đặt Python

Backend xử lý ảnh được viết bằng Python.

1.  **Tải Python:**
    *   Truy cập: [https://www.python.org/downloads/windows/](https://www.python.org/downloads/windows/)
    *   **Khuyến nghị:** Chọn phiên bản **Stable Release** như **Python 3.11.x**. Tránh các bản quá mới (3.13+) hoặc quá cũ.
    *   Tải về **Windows installer (64-bit)**.
2.  **Cài đặt Python:**
    *   Chạy file cài đặt.
    *   **QUAN TRỌNG:** Ở màn hình đầu tiên, **đánh dấu tích** vào ô **"Add Python X.X to PATH"**.
    *   Nhấn **"Install Now"**.
3.  **Kiểm tra:** Mở CMD mới, gõ `python --version`.

### Bước 4: Cài đặt Java Development Kit (JDK)

Giao diện người dùng cần môi trường Java.

1.  **Tải JDK (Khuyến nghị Adoptium Temurin):**
    *   Truy cập: [https://adoptium.net/temurin/releases/](https://adoptium.net/temurin/releases/)
    *   Chọn phiên bản LTS như **JDK 17** hoặc **JDK 21**.
    *   Chọn **Windows, x64**.
    *   Tải về file **.msi**.
2.  **Cài đặt JDK:**
    *   Chạy file `.msi`.
    *   Đảm bảo các tùy chọn **"Set JAVA_HOME variable"** và **"Add to PATH"** được chọn trong quá trình cài đặt.
3.  **Kiểm tra:** Mở CMD mới, gõ `javac -version` và `java -version`. Cả hai phải hoạt động.

### Bước 5: Thiết lập Dự án Phần Mềm

1.  **Tải Mã Nguồn:** Tải về hoặc clone thư mục dự án `SwinIR_Upscaler_Project` này vào máy tính của bạn (ví dụ: `D:\SwinIR_Upscaler_Project`).
2.  **Kiểm tra Cấu Trúc Thư Mục:** Đảm bảo cấu trúc đúng như sau:
    ```
    SwinIR_Upscaler_Project/
    ├── src/
    │   └── SwinIRUpscalerGUI.java   <-- Code Java GUI
    ├── python_backend/
    │   ├── swinir_inference.py    <-- Code Python Backend (hoặc upscale_swinir.py)
    │   ├── models/                  <-- Thư mục chứa models
    │   │   ├── network_swinir.py  <-- Code định nghĩa mạng SwinIR (QUAN TRỌNG)
    │   │   ├── __init__.py        <-- File trống (QUAN TRỌNG)
    │   │   ├── model_x2_classical.pth  <-- Các file model .pth đã đổi tên
    │   │   ├── model_x4_classical.pth
    │   │   ├── model_x8_classical.pth
    │   │   ├── model_x2_realgan.pth
    │   │   ├── model_x4_realgan.pth
    │   │   ├── model_colordn_noise15.pth
    │   │   ├── model_colordn_noise25.pth
    │   │   └── model_colordn_noise50.pth
    │   └── (venv sẽ được tạo ở đây)
    ├── bin/                         <-- Chứa file .class sau khi biên dịch
    ├── output/                      <-- Chứa ảnh kết quả
    └── README.md                    <-- File bạn đang đọc
    ```
3.  **Chuẩn bị File Models:**
    *   Tải file `network_swinir.py` từ [Repo SwinIR gốc](https://github.com/JingyunLiang/SwinIR/tree/main/models) và đặt vào `python_backend/models/`.
    *   Tạo một file **trống** tên là `__init__.py` trong `python_backend/models/`.
    *   Tải các file model `.pth` cần thiết từ [Mục Releases của SwinIR](https://github.com/JingyunLiang/SwinIR/releases) (hoặc nguồn khác).
    *   **Đổi tên** các file `.pth` đã tải về theo đúng quy ước như trong cấu trúc thư mục ở trên (ví dụ: `model_x4_classical.pth`, `model_colordn_noise25.pth`,...).
    *   Đặt tất cả các file `.pth` đã đổi tên vào thư mục `python_backend/models/`.

### Bước 6: Tạo và Kích hoạt Môi trường ảo Python (venv)

1.  **Mở Command Prompt (CMD).**
2.  **Đi đến thư mục `python_backend`:**
    ```cmd
    cd /d D:\SwinIR_Upscaler_Project\python_backend 
    ```
    *(Thay bằng đường dẫn của bạn).*
3.  **Tạo môi trường ảo `venv`:** (Dùng `python` đã cài ở Bước 3)
    ```cmd
    python -m venv venv
    ```
4.  **Kích hoạt môi trường ảo:**
    ```cmd
    venv\Scripts\activate.bat
    ```
    *   **Quan trọng:** Dấu nhắc lệnh phải có `(venv)` ở đầu. Nếu không, kiểm tra lại các bước.

### Bước 7: Cài đặt Thư viện Python (Trong venv)

**Đảm bảo `(venv)` đang được kích hoạt!**

1.  **Nâng cấp Pip:**
    ```cmd
    python -m pip install --upgrade pip
    ```
2.  **Cài đặt PyTorch + CUDA:**
    *   Truy cập: [https://pytorch.org/get-started/locally/](https://pytorch.org/get-started/locally/)
    *   Chọn: **Stable, Windows, Pip, Python, CUDA [phiên bản bạn cài ở Bước 2]**.
    *   **Sao chép chính xác lệnh `pip install ... --index-url ...`**.
    *   Dán và chạy lệnh đó. Ví dụ cho CUDA 11.8:
        ```cmd
        pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118
        ```
3.  **Cài đặt các thư viện khác:**
    ```cmd
    pip install timm numpy opencv-python-headless
    ```
4.  **Kiểm tra PyTorch CUDA:**
    ```cmd
    python -c "import torch; print('PyTorch:', torch.__version__, '- CUDA Available:', torch.cuda.is_available())"
    ```
    *   `CUDA Available:` **phải** là `True`. Nếu `False`, xem lại Bước 2 và Bước 7.2.

### Bước 8: Biên dịch Mã Nguồn Java

1.  **Mở Command Prompt (CMD) mới.**
2.  **Đi đến thư mục gốc dự án:**
    ```cmd
    cd /d D:\SwinIR_Upscaler_Project
    ```
3.  **Chạy lệnh biên dịch:**
    ```cmd
    javac -d bin src/SwinIRUpscalerGUI.java
    ```
    *(Sẽ tạo file `.class` trong thư mục `bin`).*

## Chạy Ứng Dụng

1.  **Mở Command Prompt (CMD).**
2.  **Đi đến thư mục gốc dự án:**
    ```cmd
    cd /d D:\SwinIR_Upscaler_Project
    ```
3.  **Chạy ứng dụng:**
    ```cmd
    java -cp bin SwinIRUpscalerGUI
    ```
4.  Giao diện phần mềm sẽ xuất hiện.

## Hướng dẫn Sử dụng

1.  Nhấn nút **"Chọn Ảnh..."** hoặc **kéo thả** một file ảnh (JPG, PNG, BMP) vào khung bên trái.
2.  Chọn **"Nhiệm Vụ"** bạn muốn thực hiện: "Upscale" hoặc "Khử nhiễu Màu".
3.  Dựa trên nhiệm vụ đã chọn, các tùy chọn tương ứng sẽ hiện ra:
    *   **Upscale:** Chọn "Loại Model Upscale" (Classical hoặc RealGAN) và sau đó chọn "Tỉ lệ upscale" từ hộp thoại pop-up (chỉ x2, x4 cho RealGAN).
    *   **Khử nhiễu Màu:** Chọn "Mức Độ Nhiễu" (15, 25, hoặc 50).
4.  Nhấn nút **"Xử Lý Ảnh"**.
5.  Chờ quá trình xử lý hoàn tất (theo dõi thanh trạng thái phía dưới). Ảnh kết quả sẽ hiển thị ở khung bên phải. Thanh cuộn sẽ xuất hiện nếu ảnh kết quả lớn.
6.  Nhấn nút **"Lưu kết quả..."** để lưu ảnh đã xử lý (mặc định là PNG).

## Xử lý sự cố (Troubleshooting)

*   **Không kích hoạt được `venv`:** Đảm bảo bạn ở đúng thư mục (`python_backend`) và dùng đúng lệnh (`venv\Scripts\activate.bat` cho CMD). Thử tạo lại `venv` (xóa thư mục `venv` cũ trước).
*   **Lỗi `ModuleNotFoundError: No module named '...'`:** Bạn quên kích hoạt `venv` trước khi chạy ứng dụng Java, hoặc chưa cài thư viện đó vào `venv`. Kích hoạt `venv` và chạy `pip install <tên_thư_viện>`.
*   **Lỗi `ImportError: cannot import name ... models.network_swinir`:** Thiếu file `python_backend/models/network_swinir.py` hoặc `__init__.py`, hoặc thư viện `timm` chưa được cài (`pip install timm` trong venv).
*   **Lỗi `RuntimeError: Error(s) in loading state_dict...`:** Tham số kiến trúc trong `MODEL_PARAMS` của file Python không khớp với file `.pth` đang load. **Kiểm tra kỹ các giá trị `upsampler`, `depths`, `embed_dim`...** cho loại model tương ứng (đặc biệt là RealGAN và Denoise). Đảm bảo bạn đã tải đúng file `.pth`.
*   **Lỗi `CUDA out of memory`:** GPU hết bộ nhớ. Đảm bảo tùy chọn `--tile` đang được sử dụng (code Java nên có `--tile 256 --tile_overlap 32`). Thử giảm giá trị tile (vd: 192, 128). Đóng bớt các ứng dụng khác dùng GPU. Khởi động lại máy. Model loại L rất dễ gặp lỗi này trên GPU VRAM thấp.
*   **Ứng dụng chạy rất chậm (dùng CPU):** `torch.cuda.is_available()` trả về `False`. Xem lại Bước 2 (CUDA Toolkit), Bước 7.2 (Cài PyTorch CUDA), Bước 7.4 (Kiểm tra). Đảm bảo `PYTHON_EXECUTABLE` trong Java trỏ đúng vào `python.exe` của `venv`.
*   **Lỗi Python `[Errno 2] No such file or directory`:** Java không tìm thấy file script Python. Kiểm tra hằng số `SCRIPT_NAME` (hoặc tên file trong `ProcessBuilder`) trong Java có khớp với tên file Python thực tế không (`upscale_swinir.py` hoặc `swinir_inference.py`).
*   **Lỗi Python `unrecognized arguments`:** File Python mà Java gọi là phiên bản cũ, chưa được cập nhật để nhận các tham số mới (`--task`, `--noise_level`). Đảm bảo bạn đang dùng đúng file Python đã cập nhật.
*   **Lỗi Font hoặc Encoding trong Output:** Đảm bảo Java đặt `PYTHONIOENCODING=utf-8` và đọc stream bằng `StandardCharsets.UTF_8`.

---