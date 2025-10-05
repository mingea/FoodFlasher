package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Value("${sky.upload.local}")
    private String uploadPath;
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}", file);
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }
        try {
            // 处理相对路径
            String absolutePath = Paths.get(uploadPath).toAbsolutePath().toString();

            // 创建目录
            File directory = new File(absolutePath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 生成唯一文件名
            // 获取源文件名
            String originalFilename = file.getOriginalFilename();
            // 获取文件后缀
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 生成文件名(当前时间戳+uuid+后缀)
            String fileName = System.currentTimeMillis() + "_" +
                    UUID.randomUUID().toString().replace("-", "") + extension;

            // 保存文件
            String filePath = absolutePath + File.separator + fileName;
            file.transferTo(new File(filePath));

            log.info("文件上传成功，保存路径：{}", filePath);
            // 返回完整路径
            return Result.success(filePath);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }

    }
}
