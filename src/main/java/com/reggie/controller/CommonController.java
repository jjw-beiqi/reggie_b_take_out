package com.reggie.controller;

import com.reggie.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传下载
 */
@RestController
@RequestMapping("/common")
public class CommonController {

    // 在 application.yml 中配置的基本路径，通过 ${} 的形式拿到配置的路径
    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file : 形参的名字必须跟前端传来的 name 属性一致
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        // file是一个临时文件，需要转存到指定位置，否则本次请求完成后，临时文件就会删除

        // 获得原始文件名
        String originalFilename = file.getOriginalFilename();

        // 拿到文件的后缀名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 使用 uuid 重新生成文件名，防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID().toString() + suffix;

        //---------------------------------------------------------

        // 创建一个目录对象
        File dir = new File(basePath);
        // 判断当前目录是否存在，防止存图片时，配置的文件路径不存在
        if (!dir.exists()) {
            dir.mkdir();
        }

        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 返回文件名
        return R.success(fileName);

    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){

        try {
            // 输入流，通过输入流来读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            // 输出流，将文件写入响应流中，在浏览器展示图片
            ServletOutputStream outputStream = response.getOutputStream();

            // 设置响应文件类型，固定的
            response.setContentType("image/jpeg");

            // 读取文件
            int len = 0;
            byte[] bytes = new byte[1024];
            while ( (len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            // 关闭流
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
