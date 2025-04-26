import argparse
import cv2
import torch
import os
import numpy as np
from collections import OrderedDict
import math

try:
    from models.network_swinir import SwinIR as net
except ImportError as e:
    print("\n!!! Lỗi Quan Trọng !!!")
    print("Không thể import 'models.network_swinir'.")
    print("Hãy đảm bảo:")
    print("1. Thư mục 'models' (từ repo SwinIR gốc) nằm trong cùng thư mục với script 'upscale_swinir.py'.")
    print(f"   (Script hiện tại đang chạy từ: {os.path.dirname(os.path.abspath(__file__))})")
    print("2. Thư mục 'models' chứa file 'network_swinir.py'.")
    print("3. Bạn đang chạy script này từ môi trường Python đã cài đặt đầy đủ thư viện (torch, timm, etc.).")
    print(f"\nLỗi gốc: {e}")
    exit(1)

MODEL_PARAMS = {
    'classical_M': {
        'upscale': -1,
        'in_chans': 3, 
        'img_size': 64, 
        'window_size': 8,
        'img_range': 1., 
        'depths': [6, 6, 6, 6, 6, 6], 
        'embed_dim': 180, 
        'num_heads': [6, 6, 6, 6, 6, 6],
        'mlp_ratio': 2, 
        'upsampler': 'pixelshuffle', 
        'resi_connection': '1conv'
    },
    'realgan_M': {
        'upscale': -1,
        'in_chans': 3, 
        'img_size': 64,
        'window_size': 8,
        'img_range': 1., 
        'depths': [6, 6, 6, 6, 6, 6], 
        'embed_dim': 180, 
        'num_heads': [6, 6, 6, 6, 6, 6],
        'mlp_ratio': 2, 
        'upsampler': 'nearest+conv',
        'resi_connection': '1conv'
    },
    'colordenoise_M': { 
        'upscale': 1,  
        'in_chans': 3, 
        'img_size': 128, 
        'window_size': 8,
        'img_range': 1., 
        'depths': [6, 6, 6, 6, 6, 6],
        'embed_dim': 180,
        'num_heads': [6, 6, 6, 6, 6, 6],
        'mlp_ratio': 2,
        'upsampler': '',            
        'resi_connection': '1conv'
    },
    
}

def main():
    parser = argparse.ArgumentParser(description='SwinIR Image Upscaling Script for Java GUI')
    parser.add_argument('--input', type=str, required=True, help='Đường dẫn đến ảnh đầu vào.')
    parser.add_argument('--output', type=str, required=True, help='Đường dẫn để lưu ảnh đầu ra.')
    parser.add_argument('--scale', type=int, default=4, choices=[2, 3, 4, 8], help='Hệ số upscale (chỉ dùng hiệu quả cho task upscale).')
    parser.add_argument('--model_path', type=str, required=True, help='Đường dẫn đến file model SwinIR .pth.')
    parser.add_argument('--task', type=str, required=True, choices=['upscale', 'denoise_color'], help='Nhiệm vụ cần thực hiện: upscale hoặc denoise_color.')
    parser.add_argument('--noise_level', type=int, default=25, choices=[15, 25, 50], help='Mức độ nhiễu của model (chỉ dùng cho task denoise_color).')
    parser.add_argument('--model_type', type=str, default='classical_M', choices=MODEL_PARAMS.keys(), help='Loại kiến trúc model SwinIR cần sử dụng.')
    parser.add_argument('--tile', type=int, default=None, help='Kích thước tile để xử lý ảnh lớn (ví dụ: 256). None để tắt tiling.')
    parser.add_argument('--tile_overlap', type=int, default=32, help='Độ chồng lấp giữa các tile khi bật tiling.')

    args = parser.parse_args()

    print(f"--- Bắt đầu SwinIR Upscaling ---")
    print(f"Ảnh đầu vào   : {args.input}")
    print(f"Ảnh đầu ra    : {args.output}")
    print(f"Tỉ lệ upscale  : x{args.scale}")
    print(f"Model path     : {args.model_path}")

    model_type_to_use = args.model_type
    output_scale = 1

    if args.task == 'upscale':
        output_scale = args.scale
        print(f"Tỉ lệ upscale   : x{output_scale}")
        print(f"Loại Model SR  : {model_type_to_use}")
    elif args.task == 'denoise_color':
        print(f"Mức nhiễu      : {args.noise_level}")
        if model_type_to_use != 'colordenoise_M': 
            print(f"Cảnh báo: Model type '{model_type_to_use}' không phù hợp cho denoise. Sử dụng 'colordenoise_M'.")
            model_type_to_use = 'colordenoise_M'
    else:
        print(f"!!! Lỗi: Nhiệm vụ không hợp lệ '{args.task}'")
        exit(1)
        
    if args.tile:
        print(f"Tiling         : Bật (Kích thước: {args.tile}, Chồng lấp: {args.tile_overlap})")
    else:
        print(f"Tiling         : Tắt")

    # Thiết lập device (CPU hoặc GPU)
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print(f"Thiết bị sử dụng: {device}")

    try:
        print(f"Đang tải model SwinIR (Loại cấu hình: {model_type_to_use})...")
        
        if model_type_to_use not in MODEL_PARAMS:
            print(f"\n!!! Lỗi: Không tìm thấy cấu hình cho model_type '{model_type_to_use}'.")
            exit(1)
            
        params = MODEL_PARAMS[model_type_to_use].copy()
        params['upscale'] = output_scale

        if args.task == 'denoise_color' and 'upsampler' in params and not params['upsampler']:
            print("Thông tin: Bỏ qua tham số upsampler rỗng cho tác vụ denoising.")
            del params['upsampler'] 
        
        print("Sử dụng tham số kiến trúc:")
        for key, value in params.items():
             print(f"  {key}: {value}")

        model = net(**params)
        
        if not os.path.isfile(args.model_path):
            print(f"\n!!! Lỗi: Không tìm thấy file model tại: {args.model_path}")
            exit(1)
            
        pretrained_model = torch.load(args.model_path, map_location=lambda storage, loc: storage)
        
        param_key = None
        if 'params' in pretrained_model:
            param_key = 'params'
        elif 'params_ema' in pretrained_model:
             param_key = 'params_ema'
        
        if param_key:
             print(f"Đang load trọng số từ key: '{param_key}'")
             model_state_dict = pretrained_model[param_key]
        else:
             print("Cảnh báo: Không tìm thấy key 'params' hoặc 'params_ema'. Giả định state_dict nằm ở cấp cao nhất.")
             model_state_dict = pretrained_model

        new_state_dict = OrderedDict()
        needs_stripping = any(k.startswith('module.') for k in model_state_dict.keys())
        if needs_stripping:
            print("Phát hiện tiền tố 'module.', đang loại bỏ...")
        for k, v in model_state_dict.items():
            name = k[7:] if needs_stripping and k.startswith('module.') else k 
            new_state_dict[name] = v
        
        load_status = model.load_state_dict(new_state_dict, strict=True) 
        print(f"Load trọng số thành công. Trạng thái: {load_status}")

        model.eval()
        model = model.to(device)
        print("Model đã được chuyển sang chế độ đánh giá và đặt trên thiết bị.")

        print("Đang tải và tiền xử lý ảnh đầu vào...")
        img_lq = cv2.imread(args.input, cv2.IMREAD_COLOR) 
        if img_lq is None:
             print(f"\n!!! Lỗi: Không thể đọc file ảnh đầu vào: {args.input}. File có thể bị hỏng hoặc không phải định dạng ảnh được hỗ trợ.")
             exit(1)
        
        img_lq = img_lq.astype(np.float32) / 255.
        img_lq = cv2.cvtColor(img_lq, cv2.COLOR_BGR2RGB)
        img_lq_tensor = torch.from_numpy(np.transpose(img_lq, (2, 0, 1))).float()
        img_lq_tensor = img_lq_tensor.unsqueeze(0).to(device)
        print(f"Kích thước tensor đầu vào: {img_lq_tensor.shape}")

        print(f"Đang thực hiện {args.task}...")
        with torch.no_grad():
            if args.tile is None:
                print("Chạy model trên toàn bộ ảnh...")
                output_tensor = model(img_lq_tensor)
            else:
                print(f"Áp dụng tiling với kích thước {args.tile}x{args.tile} và chồng lấp {args.tile_overlap}px...")
                b, c, h, w = img_lq_tensor.size()
                tile = min(args.tile, h, w) 
                print(f"Kích thước tile thực tế: {tile}x{tile}")
                stride = tile - args.tile_overlap
                h_idx_list = list(range(0, h - tile, stride)) + [max(0, h - tile)] 
                w_idx_list = list(range(0, w - tile, stride)) + [max(0, w - tile)] 

                E = torch.zeros(b, c, h * output_scale, w * output_scale, dtype=img_lq_tensor.dtype, device=device)
                W = torch.zeros_like(E) 

                total_tiles = len(h_idx_list) * len(w_idx_list)
                processed_tiles = 0
                
                for h_idx in h_idx_list:
                    for w_idx in w_idx_list:
                        processed_tiles += 1
                        print(f"\r  Đang xử lý tile {processed_tiles}/{total_tiles} tại (h={h_idx}, w={w_idx})...", end='')
                        in_patch = img_lq_tensor[..., h_idx:h_idx + tile, w_idx:w_idx + tile]
                        out_patch = model(in_patch)
                        out_patch_mask = torch.ones_like(out_patch)

                        h_start_out, w_start_out = h_idx * output_scale, w_idx * output_scale
                        h_end_out, w_end_out = (h_idx + tile) * output_scale, (w_idx + tile) * output_scale
                        
                        E[..., h_start_out:h_end_out, w_start_out:w_end_out].add_(out_patch)
                        W[..., h_start_out:h_end_out, w_start_out:w_end_out].add_(out_patch_mask)
                
                print("\n  Hoàn tất xử lý các tile.")
                output_tensor = E.div_(W) 

        print(f"{args.task.capitalize()} hoàn tất.")

        print("Đang hậu xử lý và lưu ảnh kết quả...")
        output_img = output_tensor.data.squeeze().float().cpu().clamp_(0, 1).numpy()
        if output_img.ndim == 3:
            output_img = np.transpose(output_img, (1, 2, 0)) 
        elif output_img.ndim == 2:
             print("Cảnh báo: Output là ảnh xám.")
        else:
            print(f"!!! Lỗi: Không rõ định dạng output tensor: {output_img.shape}")
            exit(1)

        output_img = (output_img * 255.0).round().astype(np.uint8)
        
        try:
            output_img_bgr = cv2.cvtColor(output_img, cv2.COLOR_RGB2BGR)
        except cv2.error as e:
             print(f"\n!!! Lỗi khi chuyển đổi RGB sang BGR: {e}")
             print("Output image shape:", output_img.shape, "dtype:", output_img.dtype)
             exit(1)

        print(f"Đang lưu ảnh vào: {args.output}")
        success = cv2.imwrite(args.output, output_img_bgr)
        if not success:
             print(f"\n!!! Lỗi: Không thể ghi file ảnh đầu ra tới: {args.output}")
             print("Kiểm tra quyền ghi và đường dẫn.")
             exit(1)

        print(f"--- Ảnh kết quả đã được lưu thành công! ---")
        exit(0)

    except Exception as e:
        print(f"\n--- !!! Đã xảy ra lỗi nghiêm trọng trong quá trình upscale !!! ---")
        import traceback
        print(f"Loại lỗi: {type(e).__name__}")
        print(f"Thông điệp lỗi: {e}")
        print("\n--- Chi tiết lỗi (Traceback) ---")
        traceback.print_exc()
        print("---------------------------------\n")
        exit(1)

if __name__ == '__main__':
    main()