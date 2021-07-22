package space.oldtaoge.audioserver.service;

import org.springframework.lang.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AudioSaveService {
    private static final AudioSaveService instance = new AudioSaveService();
    private static final Map<String, List<File>> fileManager = new HashMap<>();

    public void save(@NonNull String cli, File file)
    {
        List<File> fileManagerTab = fileManager.get(cli);
        if (fileManagerTab == null) {
            fileManagerTab = new LinkedList<>();
            fileManager.put(cli, fileManagerTab);
        }
        fileManagerTab.add(file);
    }

    public File get(@NonNull String cli)
    {
        List<File> fileManagerTab = fileManager.get(cli);
        if (fileManagerTab != null && !fileManagerTab.isEmpty()) {
            File file = fileManagerTab.get(0);
            fileManagerTab.remove(0);
            return file;

        }
        return null;
    }


    public static AudioSaveService getInstance()
    {
        return instance;
    }

}
