# Phần Mềm Upscale Ảnh SwinIR (Java GUI + Python Backend)

Phần mềm này cung cấp một giao diện đồ họa người dùng (GUI) viết bằng Java Swing để dễ dàng upscale (tăng độ phân giải) hình ảnh bằng mô hình trí tuệ nhân tạo SwinIR chạy trên backend Python.

## Tính năng

*   Giao diện kéo thả hoặc chọn file ảnh đầu vào.
*   Cho phép chọn các tỉ lệ upscale (ví dụ: x2, x4) dựa trên các model SwinIR có sẵn.
*   Sử dụng GPU NVIDIA (nếu có và được cài đặt đúng cách) để tăng tốc quá trình upscale.
*   Hiển thị ảnh kết quả trong giao diện.
*   Cho phép lưu ảnh đã upscale.

## Yêu cầu hệ thống

*   **Hệ điều hành:** Windows 10/11 (64-bit)
*   **Phần cứng:**
    *   Card đồ họa (GPU) NVIDIA tương thích với CUDA (Khuyến nghị dòng GeForce GTX 10xx / RTX trở lên). **GPU là rất cần thiết để có tốc độ xử lý hợp lý.**
    *   RAM hệ thống: Tối thiểu 8GB, khuyến nghị 16GB+.
    *   Dung lượng ổ cứng trống: Khoảng 15-20GB (cho Python, Java, CUDA Toolkit, thư viện và models).

## Hướng dẫn Cài đặt Môi trường

Vui lòng thực hiện tuần tự các bước sau để đảm bảo phần mềm hoạt động chính xác.

### Bước 1: Cài đặt/Cập nhật Driver NVIDIA

Đảm bảo bạn có driver mới nhất cho card đồ họa NVIDIA.

1.  Truy cập: [https://www.nvidia.com/Download/index.aspx](https://www.nvidia.com/Download/index.aspx)
2.  Chọn đúng thông tin card đồ họa và hệ điều hành của bạn.
3.  Tải về và cài đặt phiên bản **Game Ready Driver (GRD)** hoặc **Studio Driver (SD)**.
4.  Trong quá trình cài đặt, nên chọn **"Custom (Advanced)"** và đánh dấu **"Perform a clean installation"**.
5.  Khởi động lại máy tính sau khi cài đặt.

### Bước 2: Cài đặt NVIDIA CUDA Toolkit

Cung cấp môi trường để PyTorch chạy trên GPU.

1.  **Xác định phiên bản CUDA tương thích:**
    *   Truy cập trang cài đặt PyTorch ([https://pytorch.org/get-started/locally/](https://pytorch.org/get-started/locally/)) để xem phiên bản PyTorch Stable hiện tại hỗ trợ những phiên bản CUDA nào (ví dụ: 11.8, 12.1).
    *   **Chọn MỘT phiên bản CUDA** được PyTorch hỗ trợ và đảm bảo Driver NVIDIA bạn vừa cài cũng hỗ trợ phiên bản đó.
2.  **Tải CUDA Toolkit:**
    *   Truy cập kho lưu trữ CUDA Toolkit: [https://developer.nvidia.com/cuda-toolkit-archive](https://developer.nvidia.com/cuda-toolkit-archive)
    *   Tìm và chọn đúng phiên bản CUDA bạn đã xác định (ví dụ: 11.8.0).
    *   Chọn các tùy chọn: **Windows, x86_64, Windows 10/11, exe (local)**.
    *   Tải file cài đặt về.
3.  **Cài đặt CUDA Toolkit:**
    *   Chạy file cài đặt.
    *   Chọn **"Express (Recommended)"**.
    *   Hoàn tất các bước cài đặt.
4.  **Kiểm tra (Tùy chọn):** Mở Command Prompt (CMD) và gõ `nvcc --version`. Nếu thành công, nó sẽ hiển thị phiên bản CUDA đã cài.

### Bước 3: Cài đặt Python

Backend xử lý ảnh cần Python.

1.  **Tải Python:**
    *   Truy cập: [https://www.python.org/downloads/windows/](https://www.python.org/downloads/windows/)
    *   **Khuyến nghị:** Chọn một phiên bản **Stable Release** như **Python 3.11.x**. Tránh các phiên bản quá mới (vd: 3.13+) có thể chưa tương thích hoàn toàn với tất cả thư viện.
    *   Tải về **Windows installer (64-bit)**.
2.  **Cài đặt Python:**
    *   Chạy file cài đặt.
    *   **QUAN TRỌNG:** Đánh dấu tích vào ô **"Add Python X.X to PATH"**.
    *   Nhấn **"Install Now"**.
3.  **Kiểm tra:** Mở CMD mới và gõ `python --version`.

### Bước 4: Cài đặt Java Development Kit (JDK)

Giao diện người dùng cần Java.

1.  **Tải JDK (Khuyến nghị Adoptium Temurin):**
    *   Truy cập: [https://adoptium.net/temurin/releases/](https://adoptium.net/temurin/releases/)
    *   Chọn phiên bản LTS như **JDK 17** hoặc **JDK 21**.
    *   Chọn **Windows, x64**.
    *   Tải về file **.msi**.
2.  **Cài đặt JDK:**
    *   Chạy file `.msi`.
    *   Trong quá trình cài đặt, đảm bảo các tùy chọn **"Set JAVA_HOME variable"** và **"Add to PATH"** được chọn.
3.  **Kiểm tra:** Mở CMD mới, gõ `javac -version` và `java -version`. Cả hai phải hoạt động và hiển thị đúng phiên bản.

### Bước 5: Thiết lập Dự án Phần Mềm

1.  **Tải hoặc Clone Code:** Đặt thư mục dự án `SwinIR_Upscaler_Project` vào vị trí bạn muốn (ví dụ: `D:\`).
2.  **Kiểm tra Cấu trúc:** Đảm bảo cấu trúc thư mục như sau:
    ```
    SwinIR_Upscaler_Project/
    ├── src/
    │   └── SwinIRUpscalerGUI.java
    ├── python_backend/
    │   ├── upscale_swinir.py
    │   ├── models/
    │   │   ├── network_swinir.py   <-- Phải có
    │   │   ├── __init__.py         <-- Nên có (file trống)
    │   │   ├── model_x2.pth        <-- Model cho x2 (ví dụ)
    │   │   └── model_x4.pth        <-- Model cho x4 (ví dụ)
    │   └── (Thư mục venv sẽ tạo ở đây)
    ├── bin/
    ├── output/
    └── README.md
    ```
    *   **Quan trọng:** Đảm bảo bạn đã có các file model `.pth` cần thiết đặt trong thư mục `python_backend/models/`. Bạn có thể cần tải chúng từ các nguồn cung cấp mô hình SwinIR. Đảm bảo file `network_swinir.py` cũng có trong đó (lấy từ repo SwinIR gốc).

### Bước 6: Tạo và Kích hoạt Môi trường ảo Python (venv)

Điều này cô lập các thư viện Python của dự án.

1.  **Mở Command Prompt (CMD).**
2.  **Đi đến thư mục `python_backend`:**
    ```cmd
    cd /d D:\SwinIR_Upscaler_Project\python_backend 
    ```
    *(Thay `D:\SwinIR_Upscaler_Project` bằng đường dẫn của bạn).*
3.  **Tạo môi trường ảo `venv`:**
    ```cmd
    python -m venv venv
    ```
    *(Sẽ tạo thư mục `venv` bên trong `python_backend`).*
4.  **Kích hoạt môi trường ảo:**
    ```cmd
    venv\Scripts\activate.bat
    ```
    *   **Quan trọng:** Dấu nhắc lệnh **phải** thay đổi và có `(venv)` ở đầu. Nếu không, xem lại các bước trước hoặc phần xử lý sự cố.

### Bước 7: Cài đặt Thư viện Python (Trong venv)

**Đảm bảo `(venv)` đang hiển thị ở đầu dấu nhắc lệnh!**

1.  **Nâng cấp Pip:**
    ```cmd
    python -m pip install --upgrade pip
    ```
2.  **Cài đặt PyTorch + CUDA:**
    *   Truy cập: [https://pytorch.org/get-started/locally/](https://pytorch.org/get-started/locally/)
    *   Chọn: **Stable, Windows, Pip, Python, CUDA [phiên bản bạn cài ở Bước 2]**.
    *   Sao chép **chính xác** lệnh `pip install ... --index-url ...`.
    *   Dán và chạy lệnh đó. Ví dụ cho CUDA 11.8:
        ```cmd
        pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118
        ```
3.  **Cài đặt các thư viện khác:**
    ```cmd
    pip install timm
    pip install numpy
    pip install opencv-python-headless
    ```
4.  **Kiểm tra PyTorch CUDA:** (Vẫn trong venv)
    ```cmd
    python -c "import torch; print('PyTorch:', torch.__version__, 'CUDA Available:', torch.cuda.is_available(), 'CUDA Version:', torch.version.cuda)"
    ```
    *   Dòng `CUDA Available:` **phải** là `True`.

### Bước 8: Biên dịch Mã Nguồn Java

1.  **Mở Command Prompt (CMD) mới** (không cần kích hoạt venv).
2.  **Đi đến thư mục gốc của dự án:**
    ```cmd
    cd /d D:\SwinIR_Upscaler_Project
    ```
3.  **Chạy lệnh biên dịch:**
    ```cmd
    javac -d bin src/SwinIRUpscalerGUI.java
    ```
    *(Sẽ tạo các file `.class` trong thư mục `bin`).*

## Chạy Ứng Dụng

1.  **Mở Command Prompt (CMD).**
2.  **Đi đến thư mục gốc của dự án:**
    ```cmd
    cd /d D:\SwinIR_Upscaler_Project
    ```
3.  **Chạy ứng dụng Java:**
    ```cmd
    java -cp bin SwinIRUpscalerGUI
    ```
4.  Giao diện phần mềm sẽ xuất hiện.

## Xử lý sự cố thường gặp

*   **Không kích hoạt được `venv` (không thấy `(venv)`):**
    *   Đảm bảo bạn đang dùng đúng lệnh cho CMD (`activate.bat` hoặc `activate`).
    *   Kiểm tra xem thư mục `venv` và `venv\Scripts` có tồn tại đúng vị trí không.
    *   Thử tạo lại `venv` (xóa thư mục `venv` cũ, chạy lại `python -m venv venv`).
*   **Lỗi `ModuleNotFoundError: No module named '...'` (torch, cv2, timm):**
    *   Bạn **chưa kích hoạt `venv`** trước khi chạy ứng dụng Java, hoặc bạn **quên cài đặt thư viện** đó vào `venv` bằng `pip install`.
    *   Kích hoạt `venv` và chạy `pip install <tên_thư_viện_thiếu>`.
*   **Lỗi `ImportError: cannot import name ... from models.network_swinir`:**
    *   Kiểm tra file `python_backend/models/network_swinir.py` có tồn tại không.
    *   Đảm bảo thư viện `timm` đã được cài đặt trong `venv` (`pip install timm`).
    *   Kiểm tra xem có file trống `python_backend/models/__init__.py` không (nên có).
*   **Lỗi `CUDA out of memory`:**
    *   GPU không đủ bộ nhớ VRAM.
    *   Đảm bảo code Java đang truyền tham số `--tile <kích_thước>` (ví dụ: `--tile 256`) và `--tile_overlap <số>` (ví dụ: `--tile_overlap 32`) cho script Python (kiểm tra phần `ProcessBuilder` trong `SwinIRUpscalerGUI.java` và biên dịch lại nếu cần sửa).
    *   Thử giảm kích thước tile (ví dụ: `--tile 192` hoặc `--tile 128`).
    *   Đóng các ứng dụng khác đang sử dụng nhiều VRAM (game, trình duyệt...).
    *   Khởi động lại máy tính.
*   **Ứng dụng vẫn dùng CPU (rất chậm, Task Manager không thấy CUDA hoạt động):**
    *   Chạy lại Bước 7.4 để kiểm tra `torch.cuda.is_available()` **trong venv**. Nếu là `False`, PyTorch CUDA chưa được cài đúng -> làm lại Bước 7.2.
    *   Kiểm tra hằng số `PYTHON_EXECUTABLE` trong `SwinIRUpscalerGUI.java` đảm bảo nó trỏ đến `python.exe` **bên trong `venv`**.

Chúc bạn thành công!