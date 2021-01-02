package com.mqr.community.controller;

import com.mqr.community.annotation.LoginRequired;
import com.mqr.community.dao.UserMapper;
import com.mqr.community.entity.HostHolder;
import com.mqr.community.entity.User;
import com.mqr.community.service.LikeService;
import com.mqr.community.service.UserService;
import com.mqr.community.utils.CommunityUtil;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Resource
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Value("${community.upload.path}")
    private String uploadPath;

    @Value("${community.domain.path}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${quniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @RequestMapping(value = "/setting",method = RequestMethod.GET)
    public String settingPage(Model model) {
        // 上传文件名称
        String fileName = CommunityUtil.getUUID();
        // 设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "setting";
    }

    // 更新头像路径
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "文件名不能为空!");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }

    //废弃
    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    public String uploadHeaderImage(MultipartFile multipartFile, Model model) {
        if (multipartFile == null) {
            model.addAttribute("error", "请选择图片");
            return "setting";
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (suffix == null || suffix.length() == 0 || suffix.trim().length() == 0) {
            model.addAttribute("error", "文件格式不对");
            return "setting";
        }

        //重新生成文件名
        String fileName = CommunityUtil.getUUID() + suffix;
        //确定文件存放位置
        File dest = new File(uploadPath + "/" + fileName);
        //传输文件
        try {
            multipartFile.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传失败",e.getMessage());
            throw new RuntimeException(e);
        }

        //修改用户url
        User user = hostHolder.getUser();
        String path = domain + contextPath + "user/header/" + fileName;
        userService.updateHeader(user.getId(), path);

        return "redirect:/";
    }

    //废弃
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }


}
