package space.oldtaoge.audioserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import space.oldtaoge.audioserver.entity.PlayerEntity;
import space.oldtaoge.audioserver.service.AudioSaveService;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("player")
public class playerController {

    AudioSaveService audioSaveService = AudioSaveService.getInstance();
    PlayerEntity playerEntity = PlayerEntity.getInstance();
    Map<String, PlayerEntity.Client> clientReg = playerEntity.getCliRegister();

    @ResponseBody
    @RequestMapping("{cli}")
    public String get(HttpServletResponse response, @PathVariable("cli") String cli)
    {
        File file = audioSaveService.get(cli);

        if (file == null) {
            return "No file to download";
        }

        response.setContentType("audio/mp4;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;fileName=" +   java.net.URLEncoder.encode(file.getName(), StandardCharsets.UTF_8));

        byte[] buffer = new byte[1024];
        FileInputStream fis = null; //文件输入流
        BufferedInputStream bis = null;

        OutputStream os = null; //输出流
        try {
            os = response.getOutputStream();
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            int i = bis.read(buffer);
            while(i != -1){
                os.write(buffer);
                i = bis.read(buffer);
            }
            bis.close();
            fis.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            file.delete();
        }

        return null;
    }

    @ResponseBody
    @RequestMapping(path = "ka/{id}")
    String KA(@PathVariable(name = "id") String id)
    {
        PlayerEntity.Client client = clientReg.get(id);
        if (client != null) {
            client.setLastKA(LocalDateTime.now());
            client.setAreConnect(true);
            return "{\"status\": \"success\"}";
        }
        return "{\"status\": \"failure\"}";
    }
}
