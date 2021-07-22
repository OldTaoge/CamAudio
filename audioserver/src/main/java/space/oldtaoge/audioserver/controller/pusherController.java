package space.oldtaoge.audioserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import space.oldtaoge.audioserver.entity.PlayerEntity;
import space.oldtaoge.audioserver.service.AudioSaveService;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("pusher")
public class pusherController {

    public static final String SAVE_DIR = "/tmp/";

    AudioSaveService audioSaveService = AudioSaveService.getInstance();
    PlayerEntity playerEntity = PlayerEntity.getInstance();
    Map<String, PlayerEntity.Client> clientReg = playerEntity.getCliRegister();

    @ResponseBody
    @RequestMapping(value = "audio/{token}/", method = RequestMethod.POST)
    public String clientPush(@RequestBody MultipartFile file, @PathVariable("token") String token)
    {
        if (file.isEmpty())
        {
            return "上传失败，请选择文件";
        }

        PlayerEntity.Client client = clientReg.get(token);
        if (client != null)
        {
            try {
                String Ofilename = file.getOriginalFilename();
                File saveFile = new File(SAVE_DIR + UUID.randomUUID() + Ofilename.substring(Ofilename.lastIndexOf(".") ));
                file.transferTo(saveFile);
                audioSaveService.save(token, saveFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return "0";
    }

    @RequestMapping(value = "clistat/{token}")
    @ResponseBody
    String checkStatus(@PathVariable(name = "token") String token) {
        PlayerEntity.Client client = clientReg.get(token);
        if (client != null)
        {
            if (client.isAreConnect())
            {
                return "{\"status\": \"online\"}";
            }
        }
        return "{\"status\": \"offline\"}";
    }

}
